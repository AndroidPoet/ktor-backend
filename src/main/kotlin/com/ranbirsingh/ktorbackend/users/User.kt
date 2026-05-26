package com.ranbirsingh.ktorbackend.users

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val displayName: String,
    val createdAt: LocalDateTime,
)
