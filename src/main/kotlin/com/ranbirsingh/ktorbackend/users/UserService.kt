package com.ranbirsingh.ktorbackend.users

import java.util.UUID

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
