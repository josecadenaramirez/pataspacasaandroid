package com.cadena.pataspacasa.domain.repository

import com.cadena.pataspacasa.domain.entity.Pet

interface PetRepository {
    suspend fun getPets(): List<Pet>
    suspend fun getPet(id: String): Pet
    suspend fun claimPet(tagId: String): Pet
    suspend fun createPet(pet: Pet): Pet
    suspend fun updatePet(pet: Pet): Pet
    suspend fun updatePetPhoto(petId: String, imageData: ByteArray): Pet
    suspend fun setLostMode(petId: String): Pet
    suspend fun disableLostMode(petId: String): Pet
}
