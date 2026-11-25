package com.cadena.pataspacasa.data.datasource

import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.auth.SecureSessionStore
import com.cadena.pataspacasa.data.dto.*
import com.cadena.pataspacasa.data.network.RestClient
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendAuthDataSource @Inject constructor(
    private val sessionStore: SecureSessionStore,
    private val client: RestClient
) {
    @Volatile private var accessToken: String? = null
    private val refreshMutex = Mutex()
    @Volatile private var invalidationHandler: (() -> Unit)? = null

    suspend fun login(email: String, password: String): BackendLoginResponseDTO {
        val response = post("auth/login", LoginRequest(email, password))
        if (response.status != 200) throw mapError(response.status, response.body)
        val result = client.decode(response, BackendLoginResponseDTO.serializer())
        if (!result.ok) throw AppError.InvalidServerResponse
        storeTokens(result.accessToken, result.refreshToken)
        return result
    }

    suspend fun register(name: String, email: String, password: String): BackendRegisterResponseDTO {
        val response = post("auth/register", RegisterRequest(name, email, password))
        if (response.status != 201) throw mapError(response.status, response.body)
        return client.decode(response, BackendRegisterResponseDTO.serializer()).also {
            if (!it.ok) throw AppError.InvalidServerResponse
        }
    }

    suspend fun refresh(): BackendRefreshResponseDTO = refreshMutex.withLock {
        val refreshToken = sessionStore.loadRefreshToken()?.takeIf { it.isNotBlank() }
            ?: throw AppError.NotAuthenticated
        val response = post("auth/refresh", RefreshRequest(refreshToken))
        if (response.status != 200) {
            val error = mapError(response.status, response.body)
            if (error == AppError.SessionExpired || response.status == 401) invalidateSession()
            throw error
        }
        client.decode(response, BackendRefreshResponseDTO.serializer()).also {
            if (!it.ok) throw AppError.InvalidServerResponse
            storeTokens(it.accessToken, it.refreshToken)
        }
    }

    suspend fun logout(installationId: String) {
        val refreshToken = sessionStore.loadRefreshToken()
        try {
            if (!refreshToken.isNullOrBlank()) {
                val response = post("auth/logout", LogoutRequest(refreshToken, installationId))
                if (response.status != 200) throw mapError(response.status, response.body)
                if (!client.decode(response, BackendLogoutResponseDTO.serializer()).ok) {
                    throw AppError.InvalidServerResponse
                }
            }
        } finally {
            clearTokens()
        }
    }

    suspend fun resetPassword(email: String) {
        val response = post("auth/reset-password", ResetPasswordRequest(email))
        if (response.status !in 200..299) throw mapError(response.status, response.body)
    }

    suspend fun getAccessToken(forceRefresh: Boolean = false): String {
        if (!forceRefresh) accessToken?.takeIf { it.isNotBlank() }?.let { return it }
        return refresh().accessToken
    }

    fun hasRefreshToken(): Boolean = !sessionStore.loadRefreshToken().isNullOrBlank()

    fun clearTokens() {
        accessToken = null
        sessionStore.clear()
    }

    fun setInvalidationHandler(handler: () -> Unit) {
        invalidationHandler = handler
    }

    private fun invalidateSession() {
        clearTokens()
        invalidationHandler?.invoke()
    }

    private fun storeTokens(access: String, refresh: String) {
        if (access.isBlank() || refresh.isBlank()) throw AppError.InvalidServerResponse
        sessionStore.saveRefreshToken(refresh.trim())
        accessToken = access.trim()
    }

    private suspend fun <T> post(path: String, body: T) = client.request(
        method = "POST",
        url = "${AppConfiguration.apiBaseUrl}/$path",
        body = when (body) {
            is LoginRequest -> client.json.encodeToString(LoginRequest.serializer(), body)
            is RegisterRequest -> client.json.encodeToString(RegisterRequest.serializer(), body)
            is RefreshRequest -> client.json.encodeToString(RefreshRequest.serializer(), body)
            is LogoutRequest -> client.json.encodeToString(LogoutRequest.serializer(), body)
            is ResetPasswordRequest -> client.json.encodeToString(ResetPasswordRequest.serializer(), body)
            else -> throw AppError.InvalidServerResponse
        }
    )

    private fun mapError(status: Int, body: String): AppError {
        val code = runCatching { client.json.decodeFromString(BackendErrorDTO.serializer(), body).error }.getOrNull()
        return when (code) {
            "invalid_credentials" -> AppError.InvalidCredentials
            "user_disabled" -> AppError.PermissionDenied
            "invalid_refresh_token" -> AppError.SessionExpired
            "email_taken" -> AppError.EmailAlreadyInUse
            "bad_request" -> AppError.Validation
            else -> if (status == 401) AppError.SessionExpired else AppError.ServerError(status)
        }
    }

    @Serializable private data class LoginRequest(val email: String, val password: String)
    @Serializable private data class RegisterRequest(val name: String, val email: String, val password: String)
    @Serializable private data class RefreshRequest(val refreshToken: String)
    @Serializable private data class LogoutRequest(val refreshToken: String, val installationId: String)
    @Serializable private data class ResetPasswordRequest(val email: String)
}
