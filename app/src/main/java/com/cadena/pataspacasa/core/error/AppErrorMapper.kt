package com.cadena.pataspacasa.core.error

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

object AppErrorMapper {

    fun from(throwable: Throwable): AppError {
        return when (throwable) {
            is AppError -> throwable
            is FirebaseNetworkException -> AppError.NetworkUnavailable
            is FirebaseAuthWeakPasswordException -> AppError.WeakPassword
            is FirebaseAuthUserCollisionException -> AppError.EmailAlreadyInUse
            is FirebaseAuthInvalidCredentialsException -> {
                when (throwable.errorCode) {
                    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> AppError.InvalidCredentials
                    "ERROR_INVALID_EMAIL" -> AppError.InvalidCredentials
                    else -> AppError.InvalidCredentials
                }
            }
            is FirebaseAuthInvalidUserException -> {
                when (throwable.errorCode) {
                    "ERROR_USER_NOT_FOUND" -> AppError.UserNotFound
                    "ERROR_USER_DISABLED" -> AppError.PermissionDenied
                    else -> AppError.UserNotFound
                }
            }
            is FirebaseAuthException -> AppError.Unknown(throwable.errorCode)
            else -> {
                val message = throwable.message ?: "unknown"
                when {
                    message.contains("network", ignoreCase = true) ||
                    message.contains("connection", ignoreCase = true) -> AppError.NetworkUnavailable
                    message.contains("permission", ignoreCase = true) -> AppError.PermissionDenied
                    message.contains("not-found", ignoreCase = true) ||
                    message.contains("not_found", ignoreCase = true) -> AppError.NotFound
                    message.contains("unauthenticated", ignoreCase = true) -> AppError.NotAuthenticated
                    message.contains("premium", ignoreCase = true) -> AppError.PremiumRequired
                    else -> AppError.Unknown(message)
                }
            }
        }
    }
}
