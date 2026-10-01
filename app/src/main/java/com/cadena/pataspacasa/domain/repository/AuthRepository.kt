package com.cadena.pataspacasa.domain.repository

import com.cadena.pataspacasa.domain.entity.User
import kotlinx.coroutines.flow.Flow

sealed class SessionState {
    data class Authenticated(val user: User) : SessionState()
    object Unauthenticated : SessionState()
}

data class GoogleSignInResult(
    val user: User,
    val isNewUser: Boolean
)

interface AuthRepository {
    suspend fun signIn(email: String, password: String): User
    suspend fun register(name: String, email: String, password: String): User
    suspend fun signInWithGoogle(): User
    suspend fun signInWithGoogleFull(): GoogleSignInResult
    suspend fun signOut()
    suspend fun resetPassword(email: String)
    fun currentUser(): User?
    fun observeAuthState(): Flow<SessionState>
    suspend fun refreshCurrentUser(): User?
    suspend fun forceRefreshIdToken(): String
}
