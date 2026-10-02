package com.example.workflow.api

import com.example.workflow.service.NotFound
import com.example.workflow.service.WorkflowService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/workflows")
class WorkflowController(private val service: WorkflowService) {

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun create(
        @RequestHeader("Idempotency-Key") idempotencyKey: String,
        @Valid @RequestBody request: CreateWorkflowRequest
    ): CreateWorkflowResponse {
        val workflow = service.create(request, idempotencyKey)
        service.executeAsync(workflow.id)
        return CreateWorkflowResponse(workflow.id, workflow.status.name)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: String) = service.get(id)
}

@RestControllerAdvice
class ApiExceptionHandler {
    @ExceptionHandler(NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun notFound(ex: NotFound) = mapOf("error" to ex.message)

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun badRequest(ex: IllegalArgumentException) = mapOf("error" to ex.message)

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun generic(ex: Exception) = mapOf("error" to (ex.message ?: "Internal server error"))
}
