package com.ranbirsingh.ktorbackend.users

import com.ranbirsingh.ktorbackend.di.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.datetime
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID

@Inject
@SingleIn(AppScope::class)
class PostgresUserRepository(
    private val database: Database,
) : UserRepository {
    override fun existsByEmail(email: String): Boolean =
        transaction(database) {
            UsersTable
                .selectAll()
                .where { UsersTable.email eq email }
                .limit(1)
                .any()
        }

    override fun save(user: NewUser): User =
        transaction(database) {
            UsersTable.insert {
                it[id] = user.id
                it[email] = user.email
                it[displayName] = user.displayName
            }

            findById(user.id) ?: error("User insert did not return a record")
        }

    override fun findById(id: UUID): User? =
        transaction(database) {
            UsersTable
                .selectAll()
                .where { UsersTable.id eq id }
                .singleOrNull()
                ?.let {
                    User(
                        id = it[UsersTable.id],
                        email = it[UsersTable.email],
                        displayName = it[UsersTable.displayName],
                        createdAt = it[UsersTable.createdAt],
                    )
                }
        }
}

private object UsersTable : Table("users") {
    val id = javaUUID("id")
    val email = varchar("email", 320)
    val displayName = varchar("display_name", 120)
    val createdAt = datetime("created_at")

    override val primaryKey = PrimaryKey(id)
}
