package com.ranbirsingh.ktorbackend.chat

import kotlinx.serialization.Serializable
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

@Serializable
data class ChatMessage(
    val id: String,
    val roomId: String,
    val sender: String,
    val text: String,
    val sentAt: String,
)

data class NewChatMessage(
    val roomId: String,
    val sender: String,
    val text: String,
) {
    fun toMessage(clock: Clock = Clock.systemUTC()) = ChatMessage(
        id = UUID.randomUUID().toString(),
        roomId = roomId,
        sender = sender,
        text = text,
        sentAt = LocalDateTime.now(clock).toString(),
    )
}
