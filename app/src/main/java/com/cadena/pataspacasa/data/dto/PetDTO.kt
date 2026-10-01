package com.cadena.pataspacasa.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PetDTO(
    val id: String,
    val publicId: String? = null,
    val tagId: String? = null,
    val name: String,
    val species: String? = null,
    val breed: String? = null,
    val age: Int? = null,
    val photoUrl: String? = null,
    val notes: String? = null,
    val allergies: String? = null,
    val treatment: String? = null,
    val ownerId: String? = null,
    val ownerName: String? = null,
    val ownerEmail: String? = null,
    val phones: List<String>? = null,
    val status: String? = null,
    val lostMode: Boolean? = null,
    val plan: String? = null,
    val isPremiumActive: Boolean? = null,
    val paidUntil: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class ClaimPetRequestDTO(
    val tagId: String
)

@Serializable
data class ClaimPetResponseDTO(
    val petId: String,
    val tagId: String? = null
)

@Serializable
data class CreatePetRequestDTO(
    val name: String,
    val species: String,
    val breed: String,
    val age: Int? = null,
    val notes: String? = null,
    val allergies: String? = null,
    val treatment: String? = null,
    val phones: List<String>? = null
)

@Serializable
data class UpdatePetRequestDTO(
    val petId: String,
    val name: String,
    val breed: String? = null,
    val age: Int? = null,
    val notes: String? = null,
    val allergies: String? = null,
    val treatment: String? = null,
    val ownerName: String? = null,
    val phones: List<String>? = null
)

@Serializable
data class UpdateOwnPetResponseDTO(
    val petId: String? = null,
    val ok: Boolean? = null
)

@Serializable
data class UpdatePetPhotoRequestDTO(
    val petId: String,
    @SerialName("photoBase64") val imageBase64: String,
    @SerialName("photoContentType") val mimeType: String
)

@Serializable
data class UpdatePetPhotoResponseDTO(
    val photoUrl: String? = null,
    val ok: Boolean? = null
)

@Serializable
data class SetLostModeRequestDTO(
    val petId: String,
    val enable: Boolean
)

@Serializable
data class SetLostModeResponseDTO(
    val ok: Boolean? = null,
    val status: String? = null
)

@Serializable
data class GetMyPetsResponseDTO(
    val ok: Boolean? = null,
    val pets: List<PetDTO> = emptyList()
)
