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

sealed class LostPetScanHistoryUiState {
    object Loading : LostPetScanHistoryUiState()
    data class Success(val scans: List<LostPetScanEvent>) : LostPetScanHistoryUiState()
    object Empty : LostPetScanHistoryUiState()
    data class Error(val message: String) : LostPetScanHistoryUiState()
}

@HiltViewModel
class LostPetScanHistoryViewModel @Inject constructor(
    private val lostPetScanRepository: LostPetScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LostPetScanHistoryUiState>(LostPetScanHistoryUiState.Loading)
    val uiState: StateFlow<LostPetScanHistoryUiState> = _uiState.asStateFlow()

    private var petId: String = ""

    fun setPetId(id: String) {
        petId = id
        load()
    }

    fun load() {
        _uiState.value = LostPetScanHistoryUiState.Loading
        viewModelScope.launch {
            try {
                val scans = lostPetScanRepository.getScans(petId)
                _uiState.value = if (scans.isEmpty()) LostPetScanHistoryUiState.Empty
                else LostPetScanHistoryUiState.Success(scans.sortedByDescending { it.scannedAt })
            } catch (e: Exception) {
                _uiState.value = LostPetScanHistoryUiState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }
}
