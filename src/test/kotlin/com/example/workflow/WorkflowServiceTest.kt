package com.example.workflow

import com.example.workflow.api.CreateWorkflowRequest
import com.example.workflow.api.StepRequest
import com.example.workflow.persistence.WorkflowRepository
import com.example.workflow.persistence.WorkflowStepRepository
import com.example.workflow.service.WorkflowService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.test.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.*

class WorkflowServiceTest {
    private lateinit var workflows: WorkflowRepository
    private lateinit var steps: WorkflowStepRepository
    private lateinit var service: WorkflowService

    @BeforeEach
    fun setup() {
        workflows = mock(WorkflowRepository::class.java)
        steps = mock(WorkflowStepRepository::class.java)
        service = WorkflowService(workflows, steps, ObjectMapper())
    }

    @Test
    fun `rejects duplicate ids`() {
        val request = CreateWorkflowRequest(
            listOf(StepRequest("a", "task"), StepRequest("a", "task"))
        )
        assertFailsWith<IllegalArgumentException> {
            service.create(request, "key-1")
        }
    }

    @Test
    fun `rejects cycles`() {
        val request = CreateWorkflowRequest(
            listOf(
                StepRequest("a", "task", listOf("b")),
                StepRequest("b", "task", listOf("a"))
            )
        )
        assertFailsWith<IllegalArgumentException> {
            service.create(request, "key-2")
        }
    }
}
