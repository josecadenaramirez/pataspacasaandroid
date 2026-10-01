package com.cadena.pataspacasa.domain.usecase.pet

import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject

class UpdatePetPhotoUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(petId: String, imageData: ByteArray): Pet =
        petRepository.updatePetPhoto(petId, imageData)
}
