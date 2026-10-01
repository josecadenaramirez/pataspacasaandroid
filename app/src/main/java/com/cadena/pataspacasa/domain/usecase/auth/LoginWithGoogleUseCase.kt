package com.cadena.pataspacasa.domain.usecase.auth

import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.GoogleSignInResult
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend fun execute(): GoogleSignInResult = authRepository.signInWithGoogleFull()
}
