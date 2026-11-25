package com.cadena.pataspacasa.domain.usecase.pet

import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject

class UpdatePetPhotoUseCase @Inject constructor(
    private val petRepository: PetRepository
) {
    suspend fun execute(petId: String, imageData: ByteArray): Pet {
        if (petId.isBlank()) throw com.cadena.pataspacasa.core.error.AppError.PetNotFound
        if (imageData.isEmpty()) throw com.cadena.pataspacasa.core.error.AppError.PhotoUploadFailed
        if (imageData.size > MAX_PHOTO_BYTES) throw com.cadena.pataspacasa.core.error.AppError.PhotoTooLarge
        return petRepository.updatePetPhoto(petId, imageData)
    }

    private companion object {
        const val MAX_PHOTO_BYTES = 5 * 1024 * 1024
    }
}
