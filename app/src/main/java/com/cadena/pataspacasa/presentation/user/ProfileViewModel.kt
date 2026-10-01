package com.cadena.pataspacasa.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.usecase.auth.RecoverPasswordUseCase
import com.cadena.pataspacasa.domain.usecase.auth.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val signOutUseCase: SignOutUseCase,
    private val recoverPasswordUseCase: RecoverPasswordUseCase
) : ViewModel() {

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = authRepository.currentUser()
        _userName.value = user?.name ?: ""
        _email.value = user?.email ?: ""
    }

    fun signOut(onSuccess: () -> Unit) {
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                signOutUseCase.execute()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = AppErrorMapper.from(e).userMessage
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun sendPasswordReset() {
        val email = _email.value
        if (email.isEmpty()) return
        _isProcessing.value = true
        viewModelScope.launch {
            try {
                recoverPasswordUseCase.execute(email)
                _successMessage.value = "Te enviamos un correo a $email para restablecer tu contraseña."
            } catch (e: AppError) {
                _errorMessage.value = e.userMessage
            } catch (e: Exception) {
                _errorMessage.value = AppErrorMapper.from(e).userMessage
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun clearSuccess() { _successMessage.value = null }
    fun clearError() { _errorMessage.value = null }
}
