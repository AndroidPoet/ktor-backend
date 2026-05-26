package com.ranbirsingh.ktorbackend.common

import io.ktor.resources.Resource
import kotlinx.serialization.Serializable

object AppRoutes {
    const val Liveness = "/livez"
    const val Readiness = "/readyz"
    const val OpenApiJson = "/openapi.json"
    const val ChatWebSocket = "/ws/chat/{roomId}"
    const val RoomIdParam = "roomId"
    const val SenderQuery = "sender"

    fun chatWebSocket(roomId: String, sender: String): String =
        "/ws/chat/$roomId?$SenderQuery=$sender"
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

@Serializable
@Resource("/api/chat/rooms/{roomId}/messages")
data class ChatMessagesRoute(
    val roomId: String,
)
