package com.cadena.pataspacasa.domain.usecase.auth

import com.cadena.pataspacasa.domain.repository.AuthRepository
import javax.inject.Inject

class RecoverPasswordUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(email: String) {
        val trimmedEmail = email.trim().lowercase()
        require(trimmedEmail.isNotEmpty()) { "El email no puede estar vacío." }
        authRepository.resetPassword(trimmedEmail)
    }
}
