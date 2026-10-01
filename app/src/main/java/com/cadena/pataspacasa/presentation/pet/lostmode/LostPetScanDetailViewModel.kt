package com.cadena.pataspacasa.presentation.pet.lostmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.LostPetScanEvent
import com.cadena.pataspacasa.domain.repository.LostPetScanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LostPetScanDetailUiState {
    object Loading : LostPetScanDetailUiState()
    data class Success(val scan: LostPetScanEvent) : LostPetScanDetailUiState()
    data class Error(val message: String) : LostPetScanDetailUiState()
}

@HiltViewModel
class LostPetScanDetailViewModel @Inject constructor(
    private val lostPetScanRepository: LostPetScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LostPetScanDetailUiState>(LostPetScanDetailUiState.Loading)
    val uiState: StateFlow<LostPetScanDetailUiState> = _uiState.asStateFlow()

    fun load(eventId: String, petId: String) {
        _uiState.value = LostPetScanDetailUiState.Loading
        viewModelScope.launch {
            try {
                val scan = lostPetScanRepository.getScan(eventId, petId)
                _uiState.value = LostPetScanDetailUiState.Success(scan)
            } catch (e: Exception) {
                _uiState.value = LostPetScanDetailUiState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }
}
