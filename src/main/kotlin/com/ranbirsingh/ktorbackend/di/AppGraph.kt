package com.ranbirsingh.ktorbackend.di

import com.ranbirsingh.ktorbackend.chat.ChatRepository
import com.ranbirsingh.ktorbackend.chat.ChatRoomHub
import com.ranbirsingh.ktorbackend.chat.PostgresChatRepository
import com.ranbirsingh.ktorbackend.config.AppConfig
import com.ranbirsingh.ktorbackend.db.DatabaseFactory
import com.ranbirsingh.ktorbackend.db.DatabaseHandle
import com.ranbirsingh.ktorbackend.users.PostgresUserRepository
import com.ranbirsingh.ktorbackend.users.UserRepository
import com.ranbirsingh.ktorbackend.users.UserService
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.Scope
import dev.zacsweers.metro.SingleIn
import org.jetbrains.exposed.v1.jdbc.Database

@Scope
annotation class AppScope

@DependencyGraph(AppScope::class)
interface AppGraph {
    val database: DatabaseHandle
    val userService: UserService
    val chat: ChatRoomHub

    @Binds
    val PostgresUserRepository.bindUserRepository: UserRepository

    @Binds
    val PostgresChatRepository.bindChatRepository: ChatRepository

    @SingleIn(AppScope::class)
    @Provides
    fun provideDatabaseHandle(config: AppConfig): DatabaseHandle =
        DatabaseFactory.connect(config.database)

    @Provides
    fun provideDatabase(handle: DatabaseHandle): Database =
        handle.database

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides config: AppConfig): AppGraph
    }
}
