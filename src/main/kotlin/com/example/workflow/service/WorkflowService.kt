package com.example.workflow.service

import com.example.workflow.api.*
import com.example.workflow.persistence.*
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

@Service
class WorkflowService(
    private val workflows: WorkflowRepository,
    private val steps: WorkflowStepRepository,
    private val mapper: ObjectMapper
) {
    @Transactional
    fun create(request: CreateWorkflowRequest, idempotencyKey: String): WorkflowEntity {
        workflows.findByIdempotencyKey(idempotencyKey)?.let { return it }
        validate(request)

        val entity = WorkflowEntity(
            id = UUID.randomUUID().toString(),
            definitionJson = mapper.writeValueAsString(request.steps),
            idempotencyKey = idempotencyKey
        )
        workflows.save(entity)

        request.steps.forEach {
            steps.save(
                WorkflowStepEntity(
                    workflowId = entity.id,
                    stepId = it.id,
                    type = it.type,
                    dependsOnJson = mapper.writeValueAsString(it.dependsOn)
                )
            )
        }

        return entity
    }

    @Async("workflowExecutor")
    fun executeAsync(workflowId: String): CompletableFuture<Void> {
        try {
            runWorkflow(workflowId)
        } catch (ex: Exception) {
            workflows.findById(workflowId).ifPresent {
                it.status = WorkflowEntity.Status.FAILED
                it.error = ex.message ?: "Unexpected workflow failure"
                it.completedAt = Instant.now()
                workflows.save(it)
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    @Transactional
    fun runWorkflow(workflowId: String) {
        val workflow = workflows.findById(workflowId).orElseThrow { NotFound("Workflow $workflowId not found") }
        workflow.status = WorkflowEntity.Status.RUNNING
        workflows.save(workflow)

        val all = steps.findByWorkflowIdOrderByPk(workflowId)
        val byId = all.associateBy { it.stepId }.toMutableMap()
        val remaining = all.map { it.stepId }.toMutableSet()

        while (remaining.isNotEmpty()) {
            val ready = remaining.filter { id ->
                val step = byId.getValue(id)
                val deps: List<String> = mapper.readValue(step.dependsOnJson, object : TypeReference<List<String>>() {})
                deps.all { byId.getValue(it).status == WorkflowStepEntity.StepStatus.SUCCEEDED }
            }

            if (ready.isEmpty()) {
                throw IllegalStateException("Workflow cannot make progress")
            }

            // Independent ready steps are processed in this bounded batch.
            // A production version can dispatch these to separate workers.
            ready.forEach { executeStep(byId.getValue(it)) }
            remaining.removeAll(ready.toSet())

            if (ready.any { byId.getValue(it).status == WorkflowStepEntity.StepStatus.FAILED }) {
                workflow.status = WorkflowEntity.Status.FAILED
                workflow.error = "One or more steps failed"
                workflow.completedAt = Instant.now()
                workflows.save(workflow)
                return
            }
        }

        workflow.status = WorkflowEntity.Status.SUCCEEDED
        workflow.completedAt = Instant.now()
        workflows.save(workflow)
    }

    private fun executeStep(step: WorkflowStepEntity) {
        step.status = WorkflowStepEntity.StepStatus.RUNNING
        step.attempts += 1
        steps.save(step)

        try {
            if (step.type.equals("fail", true)) {
                throw IllegalStateException("Simulated failure for step '${step.stepId}'")
            }
            Thread.sleep(100)
            step.status = WorkflowStepEntity.StepStatus.SUCCEEDED
        } catch (ex: Exception) {
            step.status = WorkflowStepEntity.StepStatus.FAILED
            step.error = ex.message
        }
        steps.save(step)
    }

    @Transactional(readOnly = true)
    fun get(id: String): WorkflowResponse {
        val workflow = workflows.findById(id).orElseThrow { NotFound("Workflow $id not found") }
        return WorkflowResponse(
            workflowId = workflow.id,
            status = workflow.status.name,
            steps = steps.findByWorkflowIdOrderByPk(id).map {
                StepResponse(it.stepId, it.status.name, it.attempts, it.error)
            },
            error = workflow.error,
            createdAt = workflow.createdAt.toString(),
            completedAt = workflow.completedAt?.toString()
        )
    }

    private fun validate(request: CreateWorkflowRequest) {
        val ids = request.steps.map { it.id }
        require(ids.size == ids.toSet().size) { "Step ids must be unique" }
        val idSet = ids.toSet()

        request.steps.forEach { step ->
            require(step.dependsOn.all { it in idSet }) { "Unknown dependency in step ${step.id}" }
            require(step.id !in step.dependsOn) { "Step ${step.id} cannot depend on itself" }
        }

        val graph = request.steps.associate { it.id to it.dependsOn }
        val visiting = mutableSetOf<String>()
        val visited = mutableSetOf<String>()

        fun dfs(id: String) {
            require(id !in visiting) { "Workflow contains a dependency cycle" }
            if (id in visited) return
            visiting += id
            graph[id].orEmpty().forEach(::dfs)
            visiting -= id
            visited += id
        }
        graph.keys.forEach(::dfs)
    }
}

class NotFound(message: String) : RuntimeException(message)
