package com.ranbirsingh.ktorbackend

import com.ranbirsingh.ktorbackend.common.AppRoutes
import com.ranbirsingh.ktorbackend.common.configureErrors
import com.ranbirsingh.ktorbackend.common.configureObservability
import com.ranbirsingh.ktorbackend.config.AppConfig
import com.ranbirsingh.ktorbackend.db.DatabaseFactory
import com.ranbirsingh.ktorbackend.chat.ChatRoomHub
import com.ranbirsingh.ktorbackend.chat.chatRoutes
import com.ranbirsingh.ktorbackend.users.SqlUserRepository
import com.ranbirsingh.ktorbackend.users.UserService
import com.ranbirsingh.ktorbackend.users.userRoutes
import io.ktor.server.application.ApplicationStopped
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.resources.Resources
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

fun main() {
    embeddedServer(Netty, port = AppConfig.fromEnvironment().serverPort) {
        module()
    }.start(wait = true)
}

fun Application.module(config: AppConfig = AppConfig.fromEnvironment()) {
    val database = DatabaseFactory.connect(config.database)
    val userService = UserService(SqlUserRepository(database.database))
    val chat = ChatRoomHub()

    install(DefaultHeaders)
    install(Resources)
    install(WebSockets) {
        pingPeriodMillis = 15.seconds.inWholeMilliseconds
        timeoutMillis = 15.seconds.inWholeMilliseconds
        maxFrameSize = 64 * 1024
        masking = false
    }
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }

    configureObservability()
    configureErrors()

    routing {
        get(AppRoutes.Liveness) {
            call.respondText("ok")
        }
        get(AppRoutes.Readiness) {
            database.dataSource.connection.use { connection ->
                connection.createStatement().use { statement ->
                    statement.execute("select 1")
                }
            }
            call.respondText("ok")
        }
        openAPI(path = "openapi", swaggerFile = "openapi/openapi.json")
        get(AppRoutes.OpenApiJson) {
            call.respondText(
                this::class.java.classLoader.getResource("openapi/openapi.json")!!.readText(),
                io.ktor.http.ContentType.Application.Json,
            )
        }
        userRoutes(userService)
        chatRoutes(chat)
    }

    monitor.subscribe(ApplicationStopped) {
        database.close()
    }
}
