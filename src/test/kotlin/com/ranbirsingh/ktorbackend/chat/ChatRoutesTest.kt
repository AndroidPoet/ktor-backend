package com.ranbirsingh.ktorbackend.chat

import com.ranbirsingh.ktorbackend.common.AppRoutes
import com.ranbirsingh.ktorbackend.common.ChatMessagesRoute
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.resources.Resources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation
import io.ktor.server.resources.Resources as ServerResources
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.ktor.server.websocket.WebSockets as ServerWebSockets
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ChatRoutesTest {
    @Test
    fun test_chatWebSocket_whenMessageIsSent_broadcastsMessage() = testApplication {
        val chat = ChatRoomHub()
        application {
            install(ServerResources)
            install(ServerWebSockets)
            install(ServerContentNegotiation) { json() }
            routing {
                chatRoutes(chat)
            }
        }
        val client = createClient {
            install(WebSockets)
        }

        client.webSocket(AppRoutes.chatWebSocket(roomId = "general", sender = "founder")) {
            send(Frame.Text("hello"))
            val response = incoming.receive() as Frame.Text
            val message = ChatJson.decodeFromString(ChatMessage.serializer(), response.readText())

            assertEquals("general", message.roomId)
            assertEquals("founder", message.sender)
            assertEquals("hello", message.text)
        }
    }

    @Test
    fun test_history_whenRoomHasMessages_returnsMessages() = testApplication {
        val chat = ChatRoomHub()
        chat.publish(NewChatMessage(roomId = "general", sender = "founder", text = "hello"))
        application {
            install(ServerResources)
            install(ServerWebSockets)
            install(ServerContentNegotiation) { json() }
            routing {
                chatRoutes(chat)
            }
        }
        val client = createClient {
            install(Resources)
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.get(ChatMessagesRoute("general"))
        val messages = response.body<List<ChatMessage>>()

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("hello", messages.single().text)
    }
}
