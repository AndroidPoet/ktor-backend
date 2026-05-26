package com.ranbirsingh.ktorbackend.chat

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime
import java.util.UUID

class PostgresChatRepository(
    private val database: Database,
) : ChatRepository {
    override fun save(message: ChatMessage): ChatMessage =
        transaction(database) {
            ChatMessagesTable.insert {
                it[id] = UUID.fromString(message.id)
                it[roomId] = message.roomId
                it[sender] = message.sender
                it[text] = message.text
                it[sentAt] = LocalDateTime.parse(message.sentAt)
            }
            message
        }

    override fun recentMessages(roomId: String, limit: Int): List<ChatMessage> =
        transaction(database) {
            ChatMessagesTable
                .selectAll()
                .where { ChatMessagesTable.roomId eq roomId }
                .orderBy(ChatMessagesTable.sentAt, SortOrder.DESC)
                .limit(limit)
                .map {
                    ChatMessage(
                        id = it[ChatMessagesTable.id].toString(),
                        roomId = it[ChatMessagesTable.roomId],
                        sender = it[ChatMessagesTable.sender],
                        text = it[ChatMessagesTable.text],
                        sentAt = it[ChatMessagesTable.sentAt].toString(),
                    )
                }
                .asReversed()
        }
}

private object ChatMessagesTable : Table("chat_messages") {
    val id = javaUUID("id")
    val roomId = varchar("room_id", 120)
    val sender = varchar("sender", 120)
    val text = varchar("text", 2000)
    val sentAt = datetime("sent_at")

    override val primaryKey = PrimaryKey(id)
}
