package com.ranbirsingh.ktorbackend.users

import java.util.UUID

data class NewUser(
    val id: UUID,
    val email: String,
    val displayName: String,
)
