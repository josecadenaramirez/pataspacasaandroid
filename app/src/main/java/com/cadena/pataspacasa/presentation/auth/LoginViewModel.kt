package com.cadena.pataspacasa.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.SessionState
import com.cadena.pataspacasa.domain.usecase.auth.LoginUseCase
import com.cadena.pataspacasa.domain.usecase.auth.LoginWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.value.let {
        _uiState.value = it.copy(email = value, emailError = null)
    }

    fun onPasswordChange(value: String) = _uiState.value.let {
        _uiState.value = it.copy(password = value, passwordError = null)
    }

    fun signIn(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!validateInputs(state)) return

        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                loginUseCase.execute(state.email, state.password)
                onSuccess()
            } catch (e: AppError) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = AppErrorMapper.from(e))
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    fun validateEmailOnChange() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.value = _uiState.value.copy(emailError = null)
        } else {
            _uiState.value = _uiState.value.copy(emailError = "Ingresa un email válido.")
        }
    }

    private fun validateInputs(state: LoginUiState): Boolean {
        val emailError = when {
            state.email.isBlank() -> "El email no puede estar vacío."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() -> "Ingresa un email válido."
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "La contraseña no puede estar vacía."
            else -> null
        }
        _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
        return emailError == null && passwordError == null
    }
}

data class LoginUiState(
    val email: String = "josecadena.rmz@hotmail.com",
    val password: String = "Sombrero!Paj4",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: AppError? = null
)
