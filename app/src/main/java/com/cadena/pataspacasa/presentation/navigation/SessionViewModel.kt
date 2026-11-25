package com.cadena.pataspacasa.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(authRepository: AuthRepository) : ViewModel() {
    val state: StateFlow<SessionState?> = authRepository.observeAuthState().stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )
}
