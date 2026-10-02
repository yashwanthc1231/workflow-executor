package com.example.workflow.api

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateWorkflowRequest(
    @field:NotEmpty
    @field:Valid
    val steps: List<StepRequest>
)

data class StepRequest(
    @field:NotBlank
    val id: String,
    @field:NotBlank
    val type: String,
    val dependsOn: List<String> = emptyList()
)

data class CreateWorkflowResponse(
    val workflowId: String,
    val status: String
)

data class StepResponse(
    val id: String,
    val status: String,
    val attempts: Int,
    val error: String?
)

data class WorkflowResponse(
    val workflowId: String,
    val status: String,
    val steps: List<StepResponse>,
    val error: String?,
    val createdAt: String,
    val completedAt: String?
)


