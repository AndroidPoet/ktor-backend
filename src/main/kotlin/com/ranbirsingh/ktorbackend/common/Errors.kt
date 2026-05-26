package com.ranbirsingh.ktorbackend.common

import com.ranbirsingh.ktorbackend.users.DuplicateUserEmailException
import com.ranbirsingh.ktorbackend.users.UserNotFoundException
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureErrors() {
    install(StatusPages) {
        exception<ValidationException> { call, cause ->
            call.respondProblem(
                status = HttpStatusCode.BadRequest,
                code = "validation_failed",
                detail = cause.message ?: "Request validation failed",
                errors = cause.errors,
            )
        }
        exception<BadRequestException> { call, cause ->
            call.respondProblem(
                status = HttpStatusCode.BadRequest,
                code = "bad_request",
                detail = cause.message ?: "Bad request",
            )
        }
        exception<DuplicateUserEmailException> { call, cause ->
            call.respondProblem(
                status = HttpStatusCode.Conflict,
                code = "duplicate_user_email",
                detail = cause.message ?: "Email already exists",
            )
        }
        exception<UserNotFoundException> { call, cause ->
            call.respondProblem(
                status = HttpStatusCode.NotFound,
                code = "user_not_found",
                detail = cause.message ?: "User not found",
            )
        }
        exception<Throwable> { call, _ ->
            call.respondProblem(
                status = HttpStatusCode.InternalServerError,
                code = "internal_error",
                detail = "Unexpected server error",
            )
        }
    }
}

private suspend fun io.ktor.server.application.ApplicationCall.respondProblem(
    status: HttpStatusCode,
    code: String,
    detail: String,
    errors: Map<String, String> = emptyMap(),
) {
    respond(
        status,
        ProblemDetails(
            title = status.description,
            status = status.value,
            detail = detail,
            code = code,
            errors = errors,
        ),
    )
}
