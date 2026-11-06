package com.cadena.pataspacasa.data.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.data.datasource.CloudFunctionDataSource
import com.cadena.pataspacasa.data.dto.*
import com.cadena.pataspacasa.data.mapper.PetMapper
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.domain.entity.PetStatus
import com.cadena.pataspacasa.domain.entity.PlanType
import com.cadena.pataspacasa.domain.repository.PetRepository
import java.io.ByteArrayOutputStream
import java.util.Base64
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebasePetRepository @Inject constructor(
    private val dataSource: CloudFunctionDataSource
) : PetRepository {

    override suspend fun getPets(): List<Pet> {
        return try {
            val dtos = dataSource.getMyPets()
            PetMapper.toDomain(dtos)
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    override suspend fun getPet(id: String): Pet {
        return try {
            val dto = dataSource.getMyPet(id)
            PetMapper.toDomain(dto)
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    override suspend fun claimPet(tagId: String): Pet {
        return try {
            val request = ClaimPetRequestDTO(tagId = tagId)
            val resp = dataSource.claimPetByTag(request)
            try {
                getPet(resp.petId)
            } catch (e: Exception) {
                Pet(
                    id = resp.petId,
                    publicId = null,
                    ownerId = "",
                    name = "Mascota vinculada",
                    species = PetSpecies.OTHER,
                    breed = "",
                    petType = "other",
                    customPetType = null,
                    customBreed = null,
                    approxBirthDate = null,
                    sex = null,
                    sterilized = null,
                    age = null,
                    photoUrl = null,
                    notes = null,
                    allergies = null,
                    treatment = null,
                    ownerName = null,
                    ownerEmail = null,
                    address = null,
                    phones = emptyList(),
                    status = PetStatus.NORMAL,
                    lostMode = false,
                    tagId = resp.tagId,
                    activationStatus = null,
                    nfcProgrammed = null,
                    nfcProgrammedAt = null,
                    plan = PlanType.BASIC,
                    isPremiumActive = false,
                    paidUntil = null,
                    createdAt = Date(),
                    updatedAt = Date()
                )
            }
        } catch (e: AppError) {
            throw e
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    override suspend fun createPet(pet: Pet): Pet {
        return try {
            val request = CreatePetRequestDTO(
                name = pet.name,
                species = pet.species.value,
                breed = pet.breed,
                age = pet.age,
                notes = pet.notes,
                allergies = pet.allergies,
                treatment = pet.treatment,
                phones = pet.phones.ifEmpty { null }
            )
            val dto = dataSource.createPet(request)
            PetMapper.toDomain(dto)
        } catch (e: AppError) {
            throw e
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    override suspend fun updatePet(pet: Pet): Pet {
        return try {
            val request = UpdatePetRequestDTO(
                petId = pet.id,
                name = pet.name,
                breed = pet.breed.ifEmpty { null },
                age = pet.age,
                notes = pet.notes,
                allergies = pet.allergies,
                treatment = pet.treatment,
                ownerName = pet.ownerName,
                phones = pet.phones.ifEmpty { null }
            )
            val resp = dataSource.updateOwnPet(request)
            val petId = resp.petId ?: pet.id
            try {
                getPet(petId)
            } catch (_: Exception) {
                pet
            }
        } catch (e: AppError) {
            throw e
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    override suspend fun updatePetPhoto(petId: String, imageData: ByteArray): Pet {
        return try {
            val jpegData = ensureJpeg(imageData)
            val base64 = Base64.getEncoder().encodeToString(jpegData)
            val request = UpdatePetPhotoRequestDTO(
                petId = petId,
                imageBase64 = base64,
                mimeType = "image/jpeg"
            )
            dataSource.updateOwnPetPhoto(request)
            getPet(petId)
        } catch (e: AppError) {
            throw e
        } catch (e: Exception) {
            throw AppError.PhotoUploadFailed
        }
    }

    override suspend fun setLostMode(petId: String): Pet {
        return try {
            val request = SetLostModeRequestDTO(petId = petId, enable = true)
            dataSource.setOwnPetLostMode(request)
            getPet(petId)
        } catch (e: AppError) {
            throw e
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    override suspend fun disableLostMode(petId: String): Pet {
        return try {
            val request = SetLostModeRequestDTO(petId = petId, enable = false)
            dataSource.setOwnPetLostMode(request)
            getPet(petId)
        } catch (e: AppError) {
            throw e
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }

    private fun ensureJpeg(data: ByteArray): ByteArray {
        val isJpeg = data.size >= 3 && data[0] == 0xFF.toByte() &&
                data[1] == 0xD8.toByte() && data[2] == 0xFF.toByte()
        if (isJpeg) return data
        val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            ?: throw AppError.PhotoUploadFailed
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        return out.toByteArray()
    }
}
