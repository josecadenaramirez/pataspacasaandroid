package com.cadena.pataspacasa.domain.usecase.pet

import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject

class EnableLostModeUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(petId: String): Pet {
        if (petId.isBlank()) throw com.cadena.pataspacasa.core.error.AppError.PetNotFound
        val pet = petRepository.getPet(petId)
        if (pet.isLost) throw com.cadena.pataspacasa.core.error.AppError.LostModeAlreadyActive
        return petRepository.setLostMode(petId)
    }
}

class DisableLostModeUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(petId: String): Pet {
        if (petId.isBlank()) throw com.cadena.pataspacasa.core.error.AppError.PetNotFound
        return petRepository.disableLostMode(petId)
    }
}
