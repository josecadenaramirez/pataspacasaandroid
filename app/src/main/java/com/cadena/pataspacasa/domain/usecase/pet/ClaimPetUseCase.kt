package com.cadena.pataspacasa.domain.usecase.pet

import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject

class ClaimPetUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(tagId: String): Pet = petRepository.claimPet(tagId)
}
