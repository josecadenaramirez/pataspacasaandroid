package com.cadena.pataspacasa.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isAuthenticated = MutableStateFlow<Boolean?>(null)
    val isAuthenticated: StateFlow<Boolean?> = _isAuthenticated.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // Wait for first auth state emission with a 500ms grace period
            try {
                val state = authRepository.observeAuthState().first()
                _isAuthenticated.value = state is SessionState.Authenticated
            } catch (e: Exception) {
                // If we can't determine auth state, default to unauthenticated
                delay(500)
                _isAuthenticated.value = false
            }
        }
    }
}
