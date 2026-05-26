package com.ranbirsingh.ktorbackend.users

import com.ranbirsingh.ktorbackend.common.RequestIdHeader
import com.ranbirsingh.ktorbackend.common.UsersRoute
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.plugins.resources.Resources as ClientResources
import io.ktor.client.plugins.resources.get
import io.ktor.client.plugins.resources.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals

class UserRoutesTest {
    @Test
    fun test_create_whenRequestIsValid_returnsCreatedUserAndRequestId() = testApplication {
        val repository = FakeUserRepository()
        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                userRoutes(UserService(repository))
            }
        }
        val client = createClient {
            install(ClientResources)
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val requestId = UUID.randomUUID().toString()

        val response = client.post(UsersRoute()) {
            contentType(ContentType.Application.Json)
            header(RequestIdHeader, requestId)
            setBody(CreateUserRequest("Founder@Example.com", " Founder "))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("founder@example.com", response.body<UserResponse>().email)
    }

    @Test
    fun test_findById_whenUserExists_returnsUser() = testApplication {
        val id = UUID.randomUUID()
        val repository = FakeUserRepository(
            user = User(id, "founder@example.com", "Founder", LocalDateTime.parse("2026-05-26T10:00:00")),
        )
        application {
            install(Resources)
            install(ContentNegotiation) { json() }
            routing {
                userRoutes(UserService(repository))
            }
        }
        val client = createClient {
            install(ClientResources)
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.get(UsersRoute.ById(id = id.toString()))

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("founder@example.com", response.body<UserResponse>().email)
    }

    private class FakeUserRepository(
        private val user: User? = null,
    ) : UserRepository {
        override fun existsByEmail(email: String): Boolean = false

        override fun save(user: NewUser): User =
            User(user.id, user.email, user.displayName, LocalDateTime.parse("2026-05-26T10:00:00"))

        override fun findById(id: UUID): User? = user
    }
}
