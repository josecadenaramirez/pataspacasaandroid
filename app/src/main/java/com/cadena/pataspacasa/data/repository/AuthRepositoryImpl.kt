package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.auth.InstallationIdentity
import com.cadena.pataspacasa.data.auth.SecureSessionStore
import com.cadena.pataspacasa.data.datasource.BackendAuthDataSource
import com.cadena.pataspacasa.data.dto.BackendAuthUserDTO
import com.cadena.pataspacasa.domain.entity.PlanType
import com.cadena.pataspacasa.domain.entity.User
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.GoogleSignInResult
import com.cadena.pataspacasa.domain.repository.SessionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val backend: BackendAuthDataSource,
    private val sessionStore: SecureSessionStore,
    private val installationIdentity: InstallationIdentity,
    private val devices: BackendDeviceRepository
) : AuthRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bootstrapMutex = Mutex()
    private val bootstrapStarted = AtomicBoolean(false)
    private val state = MutableStateFlow<SessionState?>(
        sessionStore.loadUser()?.let(SessionState::Authenticated)
    )

    init {
        backend.setInvalidationHandler { publish(null) }
    }

    override suspend fun signIn(email: String, password: String): User {
        val user = backend.login(email.trim(), password).user.toDomain()
        publish(user)
        return user
    }

    override suspend fun register(name: String, email: String, password: String): User =
        backend.register(name.trim(), email.trim(), password).user.toDomain()

    override suspend fun signInWithGoogle(): User =
        throw AppError.Unknown("Google sign-in not implemented for backend REST flow")

    override suspend fun signInWithGoogleFull(): GoogleSignInResult =
        throw AppError.Unknown("Google sign-in not implemented for backend REST flow")

    override suspend fun signOut() {
        try {
            runCatching { devices.delete() }
            backend.logout(installationIdentity.value)
        } finally {
            publish(null)
        }
    }

    override suspend fun resetPassword(email: String) = backend.resetPassword(email.trim())

    override fun currentUser(): User? = (state.value as? SessionState.Authenticated)?.user

    override fun observeAuthState(): Flow<SessionState> {
        if (bootstrapStarted.compareAndSet(false, true)) {
            if (state.value == null) {
                scope.launch { bootstrap() }
            } else if (backend.hasRefreshToken()) {
                scope.launch { runCatching { refreshCurrentUser() } }
            } else {
                publish(null)
            }
        }
        return state.filterNotNull()
    }

    private suspend fun bootstrap() = bootstrapMutex.withLock {
        if (state.value != null) return
        if (!backend.hasRefreshToken()) {
            state.value = SessionState.Unauthenticated
            return
        }
        try {
            publish(backend.refresh().user.toDomain(currentUser()))
        } catch (error: AppError) {
            if (error == AppError.NotAuthenticated || error == AppError.SessionExpired) publish(null)
            else state.value = SessionState.Unauthenticated
        }
    }

    override suspend fun refreshCurrentUser(): User? = try {
        backend.refresh().user.toDomain(currentUser()).also(::publish)
    } catch (error: AppError) {
        if (error == AppError.NotAuthenticated || error == AppError.SessionExpired) {
            publish(null)
            null
        } else throw error
    }

    override suspend fun forceRefreshIdToken(): String = backend.getAccessToken(forceRefresh = true)

    private fun publish(user: User?) {
        if (user == null) {
            sessionStore.clearUser()
            state.value = SessionState.Unauthenticated
        } else {
            sessionStore.saveUser(user)
            state.value = SessionState.Authenticated(user)
        }
    }

    private fun BackendAuthUserDTO.toDomain(cached: User? = null) = User(
        id = id,
        name = name?.trim().orEmpty(),
        email = email,
        photoUrl = cached?.photoUrl,
        role = role,
        plan = cached?.plan ?: PlanType.BASIC,
        createdAt = cached?.createdAt ?: Date(),
        isEmailVerified = cached?.isEmailVerified ?: false
    )
}
