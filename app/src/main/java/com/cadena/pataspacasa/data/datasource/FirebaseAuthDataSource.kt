package com.cadena.pataspacasa.data.datasource

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.User
import com.cadena.pataspacasa.domain.repository.SessionState
import com.cadena.pataspacasa.data.mapper.UserMapper
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthDataSource @Inject constructor() {

    private val auth: FirebaseAuth = Firebase.auth

    suspend fun signIn(email: String, password: String): FirebaseUser {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user ?: throw AppError.NotAuthenticated
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    suspend fun register(email: String, password: String, displayName: String): FirebaseUser {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw AppError.NotAuthenticated
            val profileUpdates = userProfileChangeRequest { setDisplayName(displayName) }
            user.updateProfile(profileUpdates).await()
            user
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    suspend fun signInWithGoogleCredential(idToken: String): Pair<FirebaseUser, Boolean> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw AppError.NotAuthenticated
            val isNewUser = result.additionalUserInfo?.isNewUser ?: false
            Pair(user, isNewUser)
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String) {
        try {
            auth.sendPasswordResetEmail(email).await()
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    fun currentUser(): FirebaseUser? = auth.currentUser

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun reloadCurrentUser() {
        try {
            auth.currentUser?.reload()?.await()
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    suspend fun getIdToken(forceRefresh: Boolean = false): String {
        val user = auth.currentUser ?: throw AppError.NotAuthenticated
        return try {
            val result = user.getIdToken(forceRefresh).await()
            result.token ?: throw AppError.NotAuthenticated
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }
}
