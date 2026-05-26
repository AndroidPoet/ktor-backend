package com.ranbirsingh.ktorbackend.common

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ProblemDetails(
    val type: String = "about:blank",
    val title: String,
    val status: Int,
    val detail: String,
    val code: String,
    @Contextual val errors: Map<String, String> = emptyMap(),
)
