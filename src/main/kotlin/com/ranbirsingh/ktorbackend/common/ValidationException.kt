package com.ranbirsingh.ktorbackend.common

class ValidationException(
    val errors: Map<String, String>,
) : RuntimeException("Request validation failed")
