package com.ranbirsingh.ktorbackend.chat

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChatRoomHubTest {
    @Test
    fun test_publish_whenMessageIsSent_persistsMessage() = runTest {
        val repository = FakeChatRepository()
        val hub = ChatRoomHub(repository)

        hub.publish(NewChatMessage(roomId = "general", sender = "founder", text = "hello"))

        assertEquals("hello", repository.saved.single().text)
    }

    private class FakeChatRepository : ChatRepository {
        val saved = mutableListOf<ChatMessage>()

        override fun save(message: ChatMessage): ChatMessage {
            saved += message
            return message
        }

        override fun recentMessages(roomId: String, limit: Int): List<ChatMessage> =
            saved.filter { it.roomId == roomId }.takeLast(limit)
    }
}
