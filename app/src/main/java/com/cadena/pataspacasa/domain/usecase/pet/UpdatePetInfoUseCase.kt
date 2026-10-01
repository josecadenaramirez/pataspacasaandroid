package com.cadena.pataspacasa.domain.usecase.pet

import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject

class UpdatePetInfoUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(pet: Pet): Pet = petRepository.updatePet(pet)
}
