package com.cadena.pataspacasa.domain.usecase.auth

import com.cadena.pataspacasa.domain.entity.User
import com.cadena.pataspacasa.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(email: String, password: String): User {
        val trimmedEmail = email.trim().lowercase()
        require(trimmedEmail.isNotEmpty()) { "El email no puede estar vacío." }
        require(password.isNotEmpty()) { "La contraseña no puede estar vacía." }
        return authRepository.signIn(trimmedEmail, password)
    }
}
