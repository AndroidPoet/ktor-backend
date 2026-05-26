package com.ranbirsingh.ktorbackend.users

import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val createdAt: String,
)

fun User.toResponse() = UserResponse(
    id = id.toString(),
    email = email,
    displayName = displayName,
    createdAt = createdAt.toString(),
)
