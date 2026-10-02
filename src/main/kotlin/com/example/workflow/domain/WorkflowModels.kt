package com.example.workflow.domain

enum class WorkflowStatus { QUEUED, RUNNING, SUCCEEDED, FAILED }
enum class StepStatus { PENDING, RUNNING, SUCCEEDED, FAILED, SKIPPED }

data class StepDefinition(
    val id: String,
    val type: String,
    val dependsOn: List<String> = emptyList()
)

data class WorkflowDefinition(val steps: List<StepDefinition>)
