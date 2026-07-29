package com.hybrid.messaging.core.data.repository

import com.hybrid.messaging.core.database.dao.UserDao
import com.hybrid.messaging.core.database.entity.UserEntity
import com.hybrid.messaging.core.domain.repository.AuthRepository
import com.hybrid.messaging.core.domain.repository.WebRtcRepository
import com.hybrid.messaging.core.domain.util.Resource
import com.hybrid.messaging.core.model.CallSession
import com.hybrid.messaging.core.model.CallState
import com.hybrid.messaging.core.model.User
import com.hybrid.messaging.core.model.UserStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao
) : AuthRepository {

    private val currentUserId = "user_me"

    override val currentUser: Flow<User?> = userDao.getUserById(currentUserId).map { entity ->
        entity?.let {
            User(
                id = it.id,
                phoneNumber = it.phoneNumber,
                username = it.username,
                displayName = it.displayName,
                avatarUrl = it.avatarUrl,
                status = it.status,
                statusCustomMessage = it.statusCustomMessage,
                publicKeyFingerprint = it.publicKeyFingerprint,
                lastSeenTimestamp = it.lastSeenTimestamp
            )
        }
    }

    override suspend fun registerWithPhoneNumber(phoneNumber: String, username: String): Resource<User> {
        val userEntity = UserEntity(
            id = currentUserId,
            phoneNumber = phoneNumber,
            username = username,
            displayName = username,
            avatarUrl = "https://i.pravatar.cc/300?u=$phoneNumber",
            status = UserStatus.ONLINE,
            statusCustomMessage = "Building the future of E2EE messaging 🚀",
            publicKeyFingerprint = "SIG3-FINGERPRINT-88A9-99B2-C11D",
            lastSeenTimestamp = System.currentTimeMillis()
        )
        userDao.insertUser(userEntity)
        return Resource.Success(
            User(
                id = userEntity.id,
                phoneNumber = userEntity.phoneNumber,
                username = userEntity.username,
                displayName = userEntity.displayName,
                avatarUrl = userEntity.avatarUrl,
                status = userEntity.status,
                publicKeyFingerprint = userEntity.publicKeyFingerprint
            )
        )
    }

    override suspend fun verifyOtp(phoneNumber: String, code: String): Resource<User> {
        return registerWithPhoneNumber(phoneNumber, "Alex Mercer")
    }

    override suspend fun updateE2eeIdentityKeys(publicKeyFingerprint: String): Resource<Unit> {
        return Resource.Success(Unit)
    }

    override suspend fun logout() {
        // Clear local credentials
    }
}

@Singleton
class WebRtcRepositoryImpl @Inject constructor() : WebRtcRepository {

    private val _activeCallSession = MutableStateFlow<CallSession?>(null)
    override val activeCallSession: Flow<CallSession?> = _activeCallSession.asStateFlow()

    override suspend fun initiateCall(roomId: String, isVideo: Boolean): Resource<CallSession> {
        val session = CallSession(
            callId = UUID.randomUUID().toString(),
            roomId = roomId,
            callerId = "user_me",
            isVideoCall = isVideo,
            state = CallState.DIALING
        )
        _activeCallSession.value = session
        return Resource.Success(session)
    }

    override suspend fun acceptCall(callId: String): Resource<Unit> {
        _activeCallSession.value = _activeCallSession.value?.copy(state = CallState.CONNECTED)
        return Resource.Success(Unit)
    }

    override suspend fun rejectCall(callId: String): Resource<Unit> {
        _activeCallSession.value = _activeCallSession.value?.copy(state = CallState.ENDED)
        _activeCallSession.value = null
        return Resource.Success(Unit)
    }

    override suspend fun endCall(callId: String): Resource<Unit> {
        _activeCallSession.value = null
        return Resource.Success(Unit)
    }
}
