package com.cadena.pataspacasa.core.error


object AppErrorMapper {

    fun from(throwable: Throwable): AppError {
        return when (throwable) {
            is AppError -> throwable
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
