package com.example.workflow.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface WorkflowRepository : JpaRepository<WorkflowEntity, String> {
    fun findByIdempotencyKey(idempotencyKey: String): WorkflowEntity?
}

interface WorkflowStepRepository : JpaRepository<WorkflowStepEntity, Long> {
    fun findByWorkflowIdOrderByPk(workflowId: String): List<WorkflowStepEntity>
}
