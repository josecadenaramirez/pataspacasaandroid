package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.data.datasource.FirebaseAuthDataSource
import com.cadena.pataspacasa.data.mapper.UserMapper
import com.cadena.pataspacasa.domain.entity.User
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.GoogleSignInResult
import com.cadena.pataspacasa.domain.repository.SessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): User {
        val firebaseUser = authDataSource.signIn(email, password)
        return UserMapper.toDomain(firebaseUser)
    }

    override suspend fun register(name: String, email: String, password: String): User {
        val firebaseUser = authDataSource.register(email, password, name)
        return UserMapper.toDomain(firebaseUser)
    }

    override suspend fun signInWithGoogle(): User {
        return signInWithGoogleFull().user
    }

    override suspend fun signInWithGoogleFull(): GoogleSignInResult {
        // Google Sign-In is handled in the presentation layer via Activity
        // The actual credential is passed through GoogleSignInHelper
        throw AppError.Unknown("Google Sign-In must be initiated from Activity context")
    }

    suspend fun signInWithGoogleCredential(idToken: String): GoogleSignInResult {
        val (firebaseUser, isNewUser) = authDataSource.signInWithGoogleCredential(idToken)
        return GoogleSignInResult(UserMapper.toDomain(firebaseUser), isNewUser)
    }

    override suspend fun signOut() {
        authDataSource.signOut()
    }

    override suspend fun resetPassword(email: String) {
        authDataSource.sendPasswordResetEmail(email)
    }

    override fun currentUser(): User? {
        val firebaseUser = authDataSource.currentUser() ?: return null
        return UserMapper.toDomain(firebaseUser)
    }

    override fun observeAuthState(): Flow<SessionState> {
        return authDataSource.observeAuthState().map { firebaseUser ->
            if (firebaseUser != null) {
                SessionState.Authenticated(UserMapper.toDomain(firebaseUser))
            } else {
                SessionState.Unauthenticated
            }
        }
    }

    override suspend fun refreshCurrentUser(): User? {
        authDataSource.reloadCurrentUser()
        return currentUser()
    }

    override suspend fun forceRefreshIdToken(): String {
        return authDataSource.getIdToken(forceRefresh = true)
    }
}
