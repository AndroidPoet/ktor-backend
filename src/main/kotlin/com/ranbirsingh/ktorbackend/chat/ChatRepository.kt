package com.ranbirsingh.ktorbackend.chat

interface ChatRepository {
    fun save(message: ChatMessage): ChatMessage

    fun recentMessages(roomId: String, limit: Int = 100): List<ChatMessage>
}
