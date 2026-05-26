package com.ranbirsingh.ktorbackend.users

import com.ranbirsingh.ktorbackend.common.ValidationException
import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val email: String,
    val displayName: String,
) {
    fun toCommand(): CreateUserCommand {
        val errors = buildMap {
            if (email.isBlank()) put("email", "Email is required")
            if (!email.contains("@")) put("email", "Email must be valid")
            if (email.length > 320) put("email", "Email must be 320 characters or fewer")
            if (displayName.isBlank()) put("displayName", "Display name is required")
            if (displayName.length > 120) put("displayName", "Display name must be 120 characters or fewer")
        }

        if (errors.isNotEmpty()) {
            throw ValidationException(errors)
        }

        return CreateUserCommand(
            email = email.trim().lowercase(),
            displayName = displayName.trim(),
        )
    }
}
