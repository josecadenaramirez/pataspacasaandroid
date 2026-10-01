package com.cadena.pataspacasa.domain.usecase.pet

import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject

class EnableLostModeUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(petId: String): Pet = petRepository.setLostMode(petId)
}

class DisableLostModeUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(petId: String): Pet = petRepository.disableLostMode(petId)
}
