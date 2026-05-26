package com.ranbirsingh.ktorbackend.db

import com.ranbirsingh.ktorbackend.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseFactory {
    fun connect(config: DatabaseConfig): DatabaseHandle {
        val dataSource = HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = config.url
                username = config.user
                password = config.password
                maximumPoolSize = config.maximumPoolSize
            },
        )

        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        return DatabaseHandle(
            database = Database.connect(dataSource),
            dataSource = dataSource,
        )
    }
}

data class DatabaseHandle(
    val database: Database,
    val dataSource: HikariDataSource,
) : AutoCloseable {
    override fun close() {
        dataSource.close()
    }
}
