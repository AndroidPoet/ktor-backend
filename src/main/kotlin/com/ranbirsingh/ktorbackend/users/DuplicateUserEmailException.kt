package com.ranbirsingh.ktorbackend.users

class DuplicateUserEmailException(email: String) : RuntimeException(
    "A user already exists with email: $email",
)
