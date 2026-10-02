package com.example.workflow.persistence

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "workflows", indexes = [Index(name = "idx_workflow_idempotency", columnList = "idempotencyKey", unique = true)])
class WorkflowEntity(
    @Id
    val id: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var definitionJson: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: Status = Status.QUEUED,

    @Column(columnDefinition = "TEXT")
    var error: String? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    var completedAt: Instant? = null,

    @Column(nullable = false)
    val idempotencyKey: String
) {
    enum class Status { QUEUED, RUNNING, SUCCEEDED, FAILED }
}

@Entity
@Table(name = "workflow_steps", uniqueConstraints = [UniqueConstraint(columnNames = ["workflowId", "stepId"])])
class WorkflowStepEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val pk: Long = 0,

    @Column(nullable = false)
    val workflowId: String,

    @Column(nullable = false)
    val stepId: String,

    @Column(nullable = false)
    val type: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val dependsOnJson: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: StepStatus = StepStatus.PENDING,

    var error: String? = null,

    var attempts: Int = 0
) {
    enum class StepStatus { PENDING, RUNNING, SUCCEEDED, FAILED, SKIPPED }
}
