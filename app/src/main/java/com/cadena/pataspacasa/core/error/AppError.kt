package com.cadena.pataspacasa.core.error

sealed class AppError : Exception() {
    // Auth
    object InvalidCredentials : AppError()
    object EmailAlreadyInUse : AppError()
    object WeakPassword : AppError()
    object UserNotFound : AppError()
    object GoogleSignInCancelled : AppError()
    object SessionExpired : AppError()
    object NotAuthenticated : AppError()
    object EmailNotVerified : AppError()

    // Pet
    object PetNotFound : AppError()
    object PhotoUploadFailed : AppError()
    object LostModeAlreadyActive : AppError()
    object LostModeNotActive : AppError()

    // Tag (NFC)
    object TagNotFound : AppError()
    object TagAlreadyLinked : AppError()
    object TagInvalid : AppError()
    object TagDisabled : AppError()
    object TagAlreadyOwned : AppError()

    // Subscription
    object SubscriptionNotFound : AppError()
    object PremiumRequired : AppError()

    // Network / System
    object NetworkUnavailable : AppError()
    data class ServerError(val code: Int) : AppError()
    object PermissionDenied : AppError()
    object NfcNotAvailable : AppError()
    object NfcReadFailed : AppError()

    // Generic
    object NotFound : AppError()
    data class Unknown(val reason: String) : AppError()

    val userMessage: String
        get() = when (this) {
            is InvalidCredentials -> "Email o contraseña incorrectos. Intenta de nuevo."
            is EmailAlreadyInUse -> "Este email ya está registrado. ¿Quieres iniciar sesión?"
            is WeakPassword -> "La contraseña debe tener al menos 8 caracteres."
            is UserNotFound -> "No encontramos una cuenta con ese email."
            is GoogleSignInCancelled -> "Inicio de sesión con Google cancelado."
            is SessionExpired -> "Tu sesión ha expirado. Inicia sesión de nuevo."
            is NotAuthenticated -> "Debes iniciar sesión para continuar."
            is EmailNotVerified -> "Verifica tu correo electrónico para continuar. Revisa tu bandeja de entrada y sigue el enlace de verificación."
            is PetNotFound -> "No encontramos a esta mascota. Puede que haya sido eliminada."
            is PhotoUploadFailed -> "No se pudo subir la foto. Intenta con una imagen más pequeña."
            is LostModeAlreadyActive -> "El modo Perdido ya está activo para esta mascota."
            is LostModeNotActive -> "Esta mascota no está en modo Perdido."
            is TagNotFound -> "No encontramos esta placa NFC. Verifica el código e intenta de nuevo."
            is TagAlreadyLinked -> "Esta placa ya está vinculada a otra mascota."
            is TagInvalid -> "El código de placa no es válido."
            is TagDisabled -> "Esta placa fue desactivada y no puede usarse."
            is TagAlreadyOwned -> "Esta placa ya está asociada a tu cuenta."
            is SubscriptionNotFound -> "No encontramos tu suscripción. Contacta a soporte."
            is PremiumRequired -> "Esta función está disponible solo en el plan Premium. 🐾"
            is NetworkUnavailable -> "Sin conexión. Revisa tu internet e intenta de nuevo."
            is ServerError -> "Error del servidor ($code). Intenta de nuevo más tarde."
            is PermissionDenied -> "No tienes permiso para realizar esta acción."
            is NfcNotAvailable -> "Este dispositivo no tiene NFC o está desactivado."
            is NfcReadFailed -> "No se pudo leer la placa NFC. Inténtalo de nuevo."
            is NotFound -> "No encontramos lo que buscabas."
            is Unknown -> "Ocurrió un error inesperado: $reason"
        }
}
