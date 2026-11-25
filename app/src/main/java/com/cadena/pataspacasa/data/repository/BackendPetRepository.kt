package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.datasource.RestPetDataSource
import com.cadena.pataspacasa.data.dto.UpdatePetRequestDTO
import com.cadena.pataspacasa.data.mapper.PetMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.repository.PetRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendPetRepository @Inject constructor(
    private val remote: RestPetDataSource
) : PetRepository {
    override suspend fun getPets(): List<Pet> = PetMapper.toDomain(remote.getMyPets().pets)

    override suspend fun getPet(id: String): Pet = PetMapper.toDomain(
        remote.getMyPet(id).pet ?: throw AppError.PetNotFound
    )

    override suspend fun claimPet(tagId: String): Pet {
        val response = remote.claimPet(tagId)
        if (!response.ok) throw AppError.InvalidServerResponse
        return getPet(response.petId)
    }

    override suspend fun createPet(pet: Pet): Pet = throw AppError.PermissionDenied

    override suspend fun updatePet(pet: Pet): Pet {
        val response = remote.updatePet(
            pet.id,
            UpdatePetRequestDTO(
                name = pet.name,
                petType = pet.petType,
                customPetType = pet.customPetType,
                breed = if (pet.petType == "other") null else pet.breed,
                customBreed = pet.customBreed,
                approxBirthDate = pet.approxBirthDate?.toApiValue(),
                sex = pet.sex,
                sterilized = pet.sterilized,
                ownerName = pet.ownerName,
                ownerEmail = pet.ownerEmail,
                phones = pet.phones,
                address = pet.address,
                allergies = pet.allergies,
                treatment = pet.treatment
            )
        )
        if (!response.ok) throw AppError.InvalidServerResponse
        return PetMapper.toDomain(response.pet)
    }

    override suspend fun updatePetPhoto(petId: String, imageData: ByteArray): Pet {
        val response = remote.uploadPetPhoto(petId, imageData)
        if (response.ok == false || response.resolvedPhotoUrl.isNullOrBlank()) {
            throw AppError.InvalidServerResponse
        }
        return getPet(petId)
    }

    override suspend fun setLostMode(petId: String): Pet {
        if (!remote.setLostMode(petId, true).ok) throw AppError.InvalidServerResponse
        return getPet(petId)
    }

    override suspend fun disableLostMode(petId: String): Pet {
        if (!remote.setLostMode(petId, false).ok) throw AppError.InvalidServerResponse
        return getPet(petId)
    }
}
