package com.cadena.pataspacasa.domain.usecase.pet

import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject

class CreatePetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(pet: Pet): Pet = petRepository.createPet(pet)
}
