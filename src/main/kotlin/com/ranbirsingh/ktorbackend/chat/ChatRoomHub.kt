package com.ranbirsingh.ktorbackend.chat

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChatRoomHub(
    private val repository: ChatRepository,
) {
    private val lock = Mutex()
    private val rooms = mutableMapOf<String, ChatRoom>()

    suspend fun join(roomId: String, session: WebSocketSession) {
        lock.withLock {
            room(roomId).sessions += session
        }
    }

    suspend fun leave(roomId: String, session: WebSocketSession) {
        lock.withLock {
            room(roomId).sessions -= session
        }
    }

    suspend fun history(roomId: String): List<ChatMessage> =
        repository.recentMessages(roomId)

    suspend fun publish(message: NewChatMessage): ChatMessage {
        val saved = repository.save(message.toMessage())
        val sessions = lock.withLock {
            val room = room(message.roomId)
            room.sessions.toList()
        }

        val frame = Frame.Text(ChatJson.encodeToString(ChatMessage.serializer(), saved))
        sessions.forEach { session ->
            runCatching { session.send(frame) }
        }

        return saved
    }

    private fun room(roomId: String): ChatRoom =
        rooms.getOrPut(roomId) { ChatRoom() }
}

private class ChatRoom {
    val sessions = mutableSetOf<WebSocketSession>()
}
