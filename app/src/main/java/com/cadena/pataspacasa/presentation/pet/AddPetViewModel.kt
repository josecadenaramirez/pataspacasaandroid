package com.cadena.pataspacasa.presentation.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.usecase.pet.ClaimPetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface AddPetState {
    object Idle : AddPetState
    object Scanning : AddPetState
    object Linking : AddPetState
    data class Success(val pet: Pet) : AddPetState
    data class Failure(val message: String) : AddPetState
}

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val claimPet: ClaimPetUseCase
) : ViewModel() {
    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()
    private val _state = MutableStateFlow<AddPetState>(AddPetState.Idle)
    val state: StateFlow<AddPetState> = _state.asStateFlow()

    fun onInputChange(value: String) { _input.value = value }
    fun beginScan() { if (_state.value == AddPetState.Idle) _state.value = AddPetState.Scanning }
    fun scanFailed() { _state.value = AddPetState.Failure("No se pudo leer la placa. Acércala de nuevo o pega su enlace.") }
    fun scanned(value: String) { _input.value = value; link() }

    fun link() {
        if (_state.value == AddPetState.Linking) return
        val tagId = TagCodeParser.parse(_input.value) ?: run {
            _state.value = AddPetState.Failure(
                "Ingresa un ID válido o un enlace exacto como ${AppConfiguration.examplePetUrl()}"
            )
            return
        }
        _input.value = tagId
        _state.value = AddPetState.Linking
        viewModelScope.launch {
            try {
                _state.value = AddPetState.Success(claimPet.execute(tagId))
            } catch (error: AppError) {
                _state.value = AddPetState.Failure(error.userMessage)
            } catch (error: Exception) {
                _state.value = AddPetState.Failure(AppErrorMapper.from(error).userMessage)
            }
        }
    }

    fun retry() { _state.value = AddPetState.Idle }
    fun startAnother() { _input.value = ""; _state.value = AddPetState.Idle }
}
