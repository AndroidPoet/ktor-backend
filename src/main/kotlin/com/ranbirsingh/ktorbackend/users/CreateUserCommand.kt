package com.ranbirsingh.ktorbackend.users

data class CreateUserCommand(
    val email: String,
    val displayName: String,
)
