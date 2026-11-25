package com.cadena.pataspacasa.presentation.pet.lostmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.usecase.pet.DisableLostModeUseCase
import com.cadena.pataspacasa.domain.usecase.pet.EnableLostModeUseCase
import com.cadena.pataspacasa.domain.usecase.pet.GetPetDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LostModeStep {
    object Loading : LostModeStep()
    object Processing : LostModeStep()
    data class Inactive(val pet: Pet) : LostModeStep()
    data class Active(val pet: Pet) : LostModeStep()
    data class ActivationSuccess(val pet: Pet) : LostModeStep()
    data class DeactivationSuccess(val pet: Pet) : LostModeStep()
    data class Error(val message: String) : LostModeStep()
}

@HiltViewModel
class LostModeViewModel @Inject constructor(
    private val getPetDetailUseCase: GetPetDetailUseCase,
    private val enableLostModeUseCase: EnableLostModeUseCase,
    private val disableLostModeUseCase: DisableLostModeUseCase
) : ViewModel() {

    private val _step = MutableStateFlow<LostModeStep>(LostModeStep.Loading)
    val step: StateFlow<LostModeStep> = _step.asStateFlow()

    private val _showConfirmActivation = MutableStateFlow(false)
    val showConfirmActivation: StateFlow<Boolean> = _showConfirmActivation.asStateFlow()

    private val _showConfirmDeactivation = MutableStateFlow(false)
    val showConfirmDeactivation: StateFlow<Boolean> = _showConfirmDeactivation.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var petId: String = ""
    private var lastConfirmedPet: Pet? = null

    val petName: String
        get() = when (val s = _step.value) {
            is LostModeStep.Inactive -> s.pet.name
            is LostModeStep.Active -> s.pet.name
            is LostModeStep.ActivationSuccess -> s.pet.name
            is LostModeStep.DeactivationSuccess -> s.pet.name
            else -> ""
        }

    fun setPetId(id: String) {
        petId = id
        loadPet()
    }

    private fun loadPet() {
        _step.value = LostModeStep.Loading
        viewModelScope.launch {
            try {
                val pet = getPetDetailUseCase.execute(petId)
                lastConfirmedPet = pet
                _step.value = if (pet.isLost) LostModeStep.Active(pet) else LostModeStep.Inactive(pet)
            } catch (e: Exception) {
                _step.value = LostModeStep.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun requestActivation() {
        if (_step.value is LostModeStep.Inactive) _showConfirmActivation.value = true
    }
    fun requestDeactivation() {
        if (_step.value is LostModeStep.Active) _showConfirmDeactivation.value = true
    }
    fun dismissActivation() { _showConfirmActivation.value = false }
    fun dismissDeactivation() { _showConfirmDeactivation.value = false }

    fun confirmActivation() {
        _showConfirmActivation.value = false
        _step.value = LostModeStep.Processing
        viewModelScope.launch {
            try {
                val pet = enableLostModeUseCase.execute(petId)
                lastConfirmedPet = pet
                _step.value = LostModeStep.ActivationSuccess(pet)
            } catch (e: AppError) {
                restoreConfirmedPet(e.userMessage)
            } catch (e: Exception) {
                restoreConfirmedPet(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun confirmDeactivation() {
        _showConfirmDeactivation.value = false
        _step.value = LostModeStep.Processing
        viewModelScope.launch {
            try {
                val pet = disableLostModeUseCase.execute(petId)
                lastConfirmedPet = pet
                _step.value = LostModeStep.DeactivationSuccess(pet)
            } catch (e: AppError) {
                restoreConfirmedPet(e.userMessage)
            } catch (e: Exception) {
                restoreConfirmedPet(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun retryAfterError() = loadPet()
    fun clearError() { _errorMessage.value = null }

    private fun restoreConfirmedPet(message: String) {
        val pet = lastConfirmedPet
        _step.value = when {
            pet == null -> LostModeStep.Error(message)
            pet.isLost -> LostModeStep.Active(pet)
            else -> LostModeStep.Inactive(pet)
        }
        _errorMessage.value = message
    }
}
