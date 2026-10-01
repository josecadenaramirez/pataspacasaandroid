package com.cadena.pataspacasa.presentation.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.domain.entity.PetStatus
import com.cadena.pataspacasa.domain.entity.PlanType
import com.cadena.pataspacasa.domain.usecase.pet.CreatePetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

sealed class AddPetSaveState {
    object Idle : AddPetSaveState()
    object Saving : AddPetSaveState()
    data class Success(val pet: Pet) : AddPetSaveState()
    data class Error(val message: String) : AddPetSaveState()
}

@HiltViewModel
class AddPetViewModel @Inject constructor(
    private val createPetUseCase: CreatePetUseCase
) : ViewModel() {

    private val _saveState = MutableStateFlow<AddPetSaveState>(AddPetSaveState.Idle)
    val saveState: StateFlow<AddPetSaveState> = _saveState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    private val _breed = MutableStateFlow("")
    val breed: StateFlow<String> = _breed.asStateFlow()
    private val _phonePrimary = MutableStateFlow("")
    val phonePrimary: StateFlow<String> = _phonePrimary.asStateFlow()
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()
    private val _phonesError = MutableStateFlow<String?>(null)
    val phonesError: StateFlow<String?> = _phonesError.asStateFlow()

    fun onNameChange(v: String) { _name.value = v; _nameError.value = null }
    fun onBreedChange(v: String) { _breed.value = v }
    fun onPhonePrimaryChange(v: String) { _phonePrimary.value = v; _phonesError.value = null }

    fun createPet(onSuccess: (Pet) -> Unit) {
        val trimName = _name.value.trim()
        if (trimName.isEmpty()) { _nameError.value = "El nombre no puede estar vacío."; return }
        val p1 = _phonePrimary.value.trim()
        if (p1.isEmpty()) { _phonesError.value = "Agrega al menos un número de celular."; return }

        val pet = Pet(
            id = UUID.randomUUID().toString(),
            publicId = null,
            ownerId = "",
            name = trimName,
            species = PetSpecies.DOG,
            breed = _breed.value.trim(),
            age = null,
            photoUrl = null,
            notes = null,
            allergies = null,
            treatment = null,
            ownerName = null,
            phones = listOf(p1.filter { it.isDigit() }).filter { it.isNotEmpty() },
            status = PetStatus.NORMAL,
            tagId = null,
            plan = PlanType.BASIC,
            isPremiumActive = false,
            paidUntil = null,
            createdAt = Date(),
            updatedAt = Date()
        )
        _saveState.value = AddPetSaveState.Saving
        viewModelScope.launch {
            try {
                val created = createPetUseCase.execute(pet)
                _saveState.value = AddPetSaveState.Success(created)
                onSuccess(created)
            } catch (e: AppError) {
                _saveState.value = AddPetSaveState.Error(e.userMessage)
            } catch (e: Exception) {
                _saveState.value = AddPetSaveState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }
}
