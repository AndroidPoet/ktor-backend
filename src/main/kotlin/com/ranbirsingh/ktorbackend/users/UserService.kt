package com.ranbirsingh.ktorbackend.users

import com.ranbirsingh.ktorbackend.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.UUID

@Inject
@SingleIn(AppScope::class)
class UserService(
    private val users: UserRepository,
) {
    fun create(command: CreateUserCommand): User {
        if (users.existsByEmail(command.email)) {
            throw DuplicateUserEmailException(command.email)
        }

        return users.save(
            NewUser(
                id = UUID.randomUUID(),
                email = command.email,
                displayName = command.displayName,
            ),
        )
    }

    fun findById(id: UUID): User =
        users.findById(id) ?: throw UserNotFoundException(id)
}
