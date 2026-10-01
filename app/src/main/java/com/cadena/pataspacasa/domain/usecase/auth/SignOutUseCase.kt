package com.cadena.pataspacasa.domain.usecase.auth

import com.cadena.pataspacasa.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute() = authRepository.signOut()
}
