package com.ranbirsingh.ktorbackend.users

import java.util.UUID

interface UserRepository {
    fun existsByEmail(email: String): Boolean

    fun save(user: NewUser): User

    fun findById(id: UUID): User?
}
