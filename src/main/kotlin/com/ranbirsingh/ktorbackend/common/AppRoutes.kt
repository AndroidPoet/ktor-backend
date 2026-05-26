package com.ranbirsingh.ktorbackend.common

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object AppRoutes {
    const val Liveness = "/livez"
    const val Readiness = "/readyz"
    const val OpenApiJson = "/openapi.json"
}

@Serializable
@Resource("/api/users")
class UsersRoute {
    @Serializable
    @Resource("{id}")
    data class ById(
        val parent: UsersRoute = UsersRoute(),
        val id: String,
    )
}
