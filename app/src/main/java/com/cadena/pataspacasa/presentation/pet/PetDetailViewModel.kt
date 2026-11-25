package com.cadena.pataspacasa.presentation.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.usecase.pet.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PetDetailUiState {
    object Loading : PetDetailUiState()
    data class Success(val pet: Pet) : PetDetailUiState()
    data class Error(val message: String) : PetDetailUiState()
    object NotFound : PetDetailUiState()
}

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    private val getPetDetailUseCase: GetPetDetailUseCase,
    private val disableLostModeUseCase: DisableLostModeUseCase,
    private val updatePetPhotoUseCase: UpdatePetPhotoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetDetailUiState>(PetDetailUiState.Loading)
    val uiState: StateFlow<PetDetailUiState> = _uiState.asStateFlow()

    private val _isChangingLostMode = MutableStateFlow(false)
    val isChangingLostMode: StateFlow<Boolean> = _isChangingLostMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var petId: String = ""

    fun setPetId(id: String) {
        if (petId == id && _uiState.value !is PetDetailUiState.Loading) return
        petId = id
        loadPet()
    }

    fun loadPet() {
        if (petId.isEmpty()) return
        _uiState.value = PetDetailUiState.Loading
        viewModelScope.launch {
            try {
                val pet = getPetDetailUseCase.execute(petId)
                _uiState.value = PetDetailUiState.Success(pet)
            } catch (e: AppError) {
                when (e) {
                    is AppError.PetNotFound -> _uiState.value = PetDetailUiState.NotFound
                    else -> _uiState.value = PetDetailUiState.Error(e.userMessage)
                }
            } catch (e: Exception) {
                _uiState.value = PetDetailUiState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun disableLostMode() {
        _isChangingLostMode.value = true
        viewModelScope.launch {
            try {
                val updatedPet = disableLostModeUseCase.execute(petId)
                _uiState.value = PetDetailUiState.Success(updatedPet)
            } catch (e: AppError) {
                _errorMessage.value = e.userMessage
            } catch (e: Exception) {
                _errorMessage.value = AppErrorMapper.from(e).userMessage
            } finally {
                _isChangingLostMode.value = false
            }
        }
    }

    fun updatePhoto(imageData: ByteArray) {
        viewModelScope.launch {
            try {
                val updatedPet = updatePetPhotoUseCase.execute(petId, imageData)
                _uiState.value = PetDetailUiState.Success(updatedPet)
            } catch (e: AppError) {
                _errorMessage.value = e.userMessage
            } catch (e: Exception) {
                _errorMessage.value = AppErrorMapper.from(e).userMessage
            }
        }
    }

    fun clearError() { _errorMessage.value = null }
}
