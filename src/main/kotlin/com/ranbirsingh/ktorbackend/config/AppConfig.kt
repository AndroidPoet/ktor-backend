package com.ranbirsingh.ktorbackend.config

data class AppConfig(
    val serverPort: Int,
    val database: DatabaseConfig,
) {
    companion object {
        fun fromEnvironment(env: Map<String, String> = System.getenv()) = AppConfig(
            serverPort = env["PORT"]?.toIntOrNull() ?: 8080,
            database = DatabaseConfig(
                url = env["DATABASE_URL"] ?: "jdbc:postgresql://localhost:5432/app",
                user = env["DATABASE_USER"] ?: "app",
                password = env["DATABASE_PASSWORD"] ?: "app",
                maximumPoolSize = env["DATABASE_POOL_SIZE"]?.toIntOrNull() ?: 10,
            ),
        )
    }
}

data class DatabaseConfig(
    val url: String,
    val user: String,
    val password: String,
    val maximumPoolSize: Int,
)
