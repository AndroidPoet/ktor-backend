package com.ranbirsingh.ktorbackend.users

import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class UserServiceTest {
    @Test
    fun test_create_whenEmailIsNew_persistsUser() {
        val repository = FakeUserRepository()
        val service = UserService(repository)
        val command = CreateUserCommand("founder@example.com", "Founder")

        val user = service.create(command)

        assertNotNull(user.id)
        assertEquals("founder@example.com", user.email)
        assertEquals("Founder", user.displayName)
        assertNotNull(repository.savedUser)
    }

    @Test
    fun test_create_whenEmailAlreadyExists_throwsDuplicateUserEmailException() {
        val repository = FakeUserRepository(emailExists = true)
        val service = UserService(repository)

        assertFailsWith<DuplicateUserEmailException> {
            service.create(CreateUserCommand("founder@example.com", "Founder"))
        }
    }

    @Test
    fun test_findById_whenUserDoesNotExist_throwsUserNotFoundException() {
        val repository = FakeUserRepository()
        val service = UserService(repository)

        assertFailsWith<UserNotFoundException> {
            service.findById(UUID.randomUUID())
        }
    }

    private class FakeUserRepository(
        private val emailExists: Boolean = false,
    ) : UserRepository {
        var savedUser: User? = null

        override fun existsByEmail(email: String): Boolean = emailExists

        override fun save(user: NewUser): User {
            savedUser = User(user.id, user.email, user.displayName, LocalDateTime.now())
            return savedUser!!
        }

        override fun findById(id: UUID): User? = null
    }
}
