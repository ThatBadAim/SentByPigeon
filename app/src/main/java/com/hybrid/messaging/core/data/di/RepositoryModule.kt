package com.hybrid.messaging.core.data.di

import com.hybrid.messaging.core.data.repository.AuthRepositoryImpl
import com.hybrid.messaging.core.data.repository.ChatRoomRepositoryImpl
import com.hybrid.messaging.core.data.repository.MessageRepositoryImpl
import com.hybrid.messaging.core.data.repository.ServerRepositoryImpl
import com.hybrid.messaging.core.data.repository.WebRtcRepositoryImpl
import com.hybrid.messaging.core.domain.repository.AuthRepository
import com.hybrid.messaging.core.domain.repository.ChatRoomRepository
import com.hybrid.messaging.core.domain.repository.MessageRepository
import com.hybrid.messaging.core.domain.repository.ServerRepository
import com.hybrid.messaging.core.domain.repository.WebRtcRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindChatRoomRepository(impl: ChatRoomRepositoryImpl): ChatRoomRepository

    @Binds
    @Singleton
    abstract fun bindServerRepository(impl: ServerRepositoryImpl): ServerRepository

    @Binds
    @Singleton
    abstract fun bindWebRtcRepository(impl: WebRtcRepositoryImpl): WebRtcRepository
}
