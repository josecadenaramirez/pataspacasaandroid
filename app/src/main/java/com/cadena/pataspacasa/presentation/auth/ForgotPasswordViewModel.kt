package com.cadena.pataspacasa.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.usecase.auth.RecoverPasswordUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val recoverPasswordUseCase: RecoverPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun setInitialEmail(email: String) {
        if (_uiState.value.email.isEmpty()) {
            _uiState.value = _uiState.value.copy(email = email)
        }
    }

    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value, emailError = null) }

    fun sendReset() {
        val state = _uiState.value
        val email = state.email.trim()
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = state.copy(emailError = "Ingresa un email válido.")
            return
        }
        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                recoverPasswordUseCase.execute(email)
                _uiState.value = _uiState.value.copy(isLoading = false, emailSent = true)
            } catch (e: AppError) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = AppErrorMapper.from(e))
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val emailSent: Boolean = false,
    val error: AppError? = null
)
