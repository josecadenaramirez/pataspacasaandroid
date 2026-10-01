package com.cadena.pataspacasa.domain.usecase.auth

import com.cadena.pataspacasa.domain.entity.User
import com.cadena.pataspacasa.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(name: String, email: String, password: String): User {
        val trimmedName = name.trim()
        val trimmedEmail = email.trim().lowercase()
        require(trimmedName.length >= 2) { "El nombre debe tener al menos 2 caracteres." }
        require(trimmedEmail.isNotEmpty()) { "El email no puede estar vacío." }
        require(password.length >= 8) { "La contraseña debe tener al menos 8 caracteres." }
        require(password.any { it.isUpperCase() }) { "La contraseña debe tener al menos una mayúscula." }
        return authRepository.register(trimmedName, trimmedEmail, password)
    }
}
