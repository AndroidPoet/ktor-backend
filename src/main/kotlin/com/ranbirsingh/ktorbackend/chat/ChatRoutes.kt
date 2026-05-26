package com.ranbirsingh.ktorbackend.chat

import com.ranbirsingh.ktorbackend.common.AppRoutes
import com.ranbirsingh.ktorbackend.common.ChatMessagesRoute
import com.ranbirsingh.ktorbackend.common.ValidationException
import io.ktor.server.resources.get
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText

fun Route.chatRoutes(chat: ChatRoomHub) {
    get<ChatMessagesRoute> { route ->
        call.respond(chat.history(route.roomId))
    }

    webSocket(AppRoutes.ChatWebSocket) {
        val roomId = call.parameters["roomId"]?.takeIf { it.isNotBlank() }
            ?: throw ValidationException(mapOf("roomId" to "Room ID is required"))
        val sender = call.request.queryParameters["sender"]?.takeIf { it.isNotBlank() }
            ?: throw ValidationException(mapOf("sender" to "Sender is required"))

        chat.join(roomId, this)
        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    chat.publish(
                        NewChatMessage(
                            roomId = roomId,
                            sender = sender,
                            text = frame.readText().trim().takeIf { it.isNotBlank() }
                                ?: throw ValidationException(mapOf("text" to "Message text is required")),
                        ),
                    )
                }
            }
        } finally {
            chat.leave(roomId, this)
        }
    }
}
