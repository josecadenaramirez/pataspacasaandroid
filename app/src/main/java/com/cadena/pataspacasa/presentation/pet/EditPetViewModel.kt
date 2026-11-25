package com.cadena.pataspacasa.presentation.pet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.data.catalog.BreedCatalog
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.domain.usecase.pet.GetPetDetailUseCase
import com.cadena.pataspacasa.domain.usecase.pet.UpdatePetInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class EditPetSaveState { IDLE, SAVING, SAVED, PERMISSION_DENIED }

sealed class EditPetLoadState {
    object Loading : EditPetLoadState()
    data class Success(val pet: Pet) : EditPetLoadState()
    data class Error(val message: String) : EditPetLoadState()
}

@HiltViewModel
class EditPetViewModel @Inject constructor(
    private val getPetDetailUseCase: GetPetDetailUseCase,
    private val updatePetInfoUseCase: UpdatePetInfoUseCase
) : ViewModel() {

    private val _loadState = MutableStateFlow<EditPetLoadState>(EditPetLoadState.Loading)
    val loadState: StateFlow<EditPetLoadState> = _loadState.asStateFlow()

    private val _saveState = MutableStateFlow(EditPetSaveState.IDLE)
    val saveState: StateFlow<EditPetSaveState> = _saveState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()
    private val _species = MutableStateFlow(PetSpecies.DOG)
    val species: StateFlow<PetSpecies> = _species.asStateFlow()
    private val _breed = MutableStateFlow("")
    val breed: StateFlow<String> = _breed.asStateFlow()
    private val _breedOptions = MutableStateFlow(BreedCatalog.breedsFor(PetSpecies.DOG))
    val breedOptions: StateFlow<List<String>> = _breedOptions.asStateFlow()
    private val _customPetType = MutableStateFlow("")
    val customPetType: StateFlow<String> = _customPetType.asStateFlow()
    private val _sex = MutableStateFlow("")
    val sex: StateFlow<String> = _sex.asStateFlow()
    private val _sterilized = MutableStateFlow<Boolean?>(null)
    val sterilized: StateFlow<Boolean?> = _sterilized.asStateFlow()
    private val _ownerName = MutableStateFlow("")
    val ownerName: StateFlow<String> = _ownerName.asStateFlow()
    private val _ownerEmail = MutableStateFlow("")
    val ownerEmail: StateFlow<String> = _ownerEmail.asStateFlow()
    private val _address = MutableStateFlow("")
    val address: StateFlow<String> = _address.asStateFlow()
    private val _phonePrimary = MutableStateFlow("")
    val phonePrimary: StateFlow<String> = _phonePrimary.asStateFlow()
    private val _phoneSecondary1 = MutableStateFlow("")
    val phoneSecondary1: StateFlow<String> = _phoneSecondary1.asStateFlow()
    private val _phoneSecondary2 = MutableStateFlow("")
    val phoneSecondary2: StateFlow<String> = _phoneSecondary2.asStateFlow()
    private val _allergies = MutableStateFlow("")
    val allergies: StateFlow<String> = _allergies.asStateFlow()
    private val _treatment = MutableStateFlow("")
    val treatment: StateFlow<String> = _treatment.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError.asStateFlow()
    private val _breedError = MutableStateFlow<String?>(null)
    val breedError: StateFlow<String?> = _breedError.asStateFlow()
    private val _phonesError = MutableStateFlow<String?>(null)
    val phonesError: StateFlow<String?> = _phonesError.asStateFlow()
    private val _saveError = MutableStateFlow<String?>(null)
    val saveError: StateFlow<String?> = _saveError.asStateFlow()

    private var petId: String = ""
    private var loadedPet: Pet? = null

    fun setPetId(id: String) {
        petId = id
        loadPet()
    }

    fun onNameChange(v: String) { _name.value = v; _nameError.value = null }
    fun onSpeciesChange(value: PetSpecies) {
        if (_species.value == value) return
        _species.value = value
        _breed.value = ""
        _breedError.value = null
        _customPetType.value = ""
        _breedOptions.value = BreedCatalog.breedsFor(value)
    }
    fun onBreedChange(v: String) {
        if (v !in _breedOptions.value) return
        _breed.value = v
        _breedError.value = null
    }
    fun onOwnerNameChange(v: String) { _ownerName.value = v }
    fun onOwnerEmailChange(v: String) { _ownerEmail.value = v }
    fun onAddressChange(v: String) { _address.value = v }
    fun onCustomPetTypeChange(v: String) { _customPetType.value = v }
    fun onSexChange(v: String) { _sex.value = v }
    fun onSterilizedChange(v: Boolean?) { _sterilized.value = v }
    fun onPhonePrimaryChange(v: String) { _phonePrimary.value = v; _phonesError.value = null }
    fun onPhoneSecondary1Change(v: String) { _phoneSecondary1.value = v; _phonesError.value = null }
    fun onPhoneSecondary2Change(v: String) { _phoneSecondary2.value = v; _phonesError.value = null }
    fun onAllergiesChange(v: String) { _allergies.value = v }
    fun onTreatmentChange(v: String) { _treatment.value = v }

    private fun loadPet() {
        _loadState.value = EditPetLoadState.Loading
        viewModelScope.launch {
            try {
                val pet = getPetDetailUseCase.execute(petId)
                loadedPet = pet
                _name.value = pet.name
                _species.value = pet.species
                _customPetType.value = pet.customPetType ?: if (pet.species == PetSpecies.OTHER) pet.petType else ""
                _breed.value = pet.breed
                _breedOptions.value = BreedCatalog.breedsFor(pet.species)
                _sex.value = pet.sex ?: ""
                _sterilized.value = pet.sterilized
                _ownerName.value = pet.ownerName ?: ""
                _ownerEmail.value = pet.ownerEmail ?: ""
                _address.value = pet.address ?: ""
                _phonePrimary.value = pet.phones.getOrElse(0) { "" }
                _phoneSecondary1.value = pet.phones.getOrElse(1) { "" }
                _phoneSecondary2.value = pet.phones.getOrElse(2) { "" }
                _allergies.value = pet.allergies ?: ""
                _treatment.value = pet.treatment ?: ""
                _loadState.value = EditPetLoadState.Success(pet)
            } catch (e: Exception) {
                _loadState.value = EditPetLoadState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        if (_saveState.value == EditPetSaveState.SAVING) return
        val pet = loadedPet ?: return
        _saveError.value = null
        val trimName = _name.value.trim()
        if (trimName.isEmpty()) { _nameError.value = "El nombre no puede estar vacío."; return }
        if ((_species.value == PetSpecies.DOG || _species.value == PetSpecies.CAT) && _breed.value.trim().isEmpty()) {
            _breedError.value = "Selecciona una raza."
            return
        }

        val p1 = _phonePrimary.value.trim()
        val p2 = _phoneSecondary1.value.trim()
        val p3 = _phoneSecondary2.value.trim()
        fun normalize(s: String) = s.filter { it.isDigit() }
        val phones = listOf(normalize(p1), normalize(p2), normalize(p3)).filter { it.isNotEmpty() }
        if (phones.any { it.length < 10 || it.length > 15 }) {
            _phonesError.value = "Ingresa un número de celular válido."
            return
        }

        val updatedPet = pet.copy(
            name = trimName,
            species = _species.value,
            petType = _species.value.value,
            customPetType = if (_species.value == PetSpecies.OTHER) _customPetType.value.trim().ifEmpty { null } else null,
            breed = if (_species.value == PetSpecies.OTHER) "" else _breed.value.trim(),
            customBreed = null,
            // The edit form intentionally does not expose this field. The API
            // mapper serializes nulls, so retaining the loaded value prevents
            // an unrelated edit from clearing an existing birth date.
            approxBirthDate = pet.approxBirthDate,
            sex = _sex.value.ifEmpty { null },
            sterilized = _sterilized.value,
            ownerName = _ownerName.value.trim().ifEmpty { null },
            ownerEmail = _ownerEmail.value.trim().ifEmpty { null },
            address = _address.value.trim().ifEmpty { null },
            phones = phones,
            allergies = _allergies.value.trim().ifEmpty { null },
            treatment = _treatment.value.trim().ifEmpty { null }
        )
        _saveState.value = EditPetSaveState.SAVING
        viewModelScope.launch {
            try {
                updatePetInfoUseCase.execute(updatedPet)
                _saveState.value = EditPetSaveState.SAVED
                onSuccess()
            } catch (e: AppError) {
                when (e) {
                    is AppError.PremiumRequired, is AppError.PermissionDenied -> _saveState.value = EditPetSaveState.PERMISSION_DENIED
                    else -> { _saveState.value = EditPetSaveState.IDLE; _saveError.value = e.userMessage }
                }
            } catch (e: Exception) {
                _saveState.value = EditPetSaveState.IDLE
                _saveError.value = AppErrorMapper.from(e).userMessage
            }
        }
    }

    fun retry() = loadPet()
}
