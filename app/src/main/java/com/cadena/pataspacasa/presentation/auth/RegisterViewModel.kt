package com.cadena.pataspacasa.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value, nameError = null) }
    fun onEmailChange(value: String) { _uiState.value = _uiState.value.copy(email = value, emailError = null) }
    fun onPasswordChange(value: String) { _uiState.value = _uiState.value.copy(password = value, passwordError = null) }
    fun onConfirmPasswordChange(value: String) { _uiState.value = _uiState.value.copy(confirmPassword = value, confirmPasswordError = null) }

    fun register(onSuccess: () -> Unit) {
        val state = _uiState.value
        if (!validateInputs(state)) return

        _uiState.value = state.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                registerUseCase.execute(state.name, state.email, state.password)
                onSuccess()
            } catch (e: AppError) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = AppErrorMapper.from(e))
            }
        }
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    fun validateNameOnChange() {
        val name = _uiState.value.name.trim()
        _uiState.value = _uiState.value.copy(
            nameError = if (name.isNotEmpty() && name.length < 2) "El nombre debe tener al menos 2 caracteres." else null
        )
    }

    private fun validateInputs(state: RegisterUiState): Boolean {
        val nameError = when {
            state.name.isBlank() -> "El nombre no puede estar vacío."
            state.name.trim().length < 2 -> "El nombre debe tener al menos 2 caracteres."
            else -> null
        }
        val emailError = when {
            state.email.isBlank() -> "El email no puede estar vacío."
            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email.trim()).matches() -> "Ingresa un email válido."
            else -> null
        }
        val passwordError = when {
            state.password.isBlank() -> "La contraseña no puede estar vacía."
            state.password.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
            !state.password.any { it.isUpperCase() } -> "La contraseña debe tener al menos una mayúscula."
            else -> null
        }
        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Confirma tu contraseña."
            state.confirmPassword != state.password -> "Las contraseñas no coinciden."
            else -> null
        }
        _uiState.value = state.copy(
            nameError = nameError,
            emailError = emailError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError
        )
        return nameError == null && emailError == null && passwordError == null && confirmPasswordError == null
    }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val error: AppError? = null
)
