package com.cadena.pataspacasa

import com.cadena.pataspacasa.domain.entity.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class PetPremiumEntitlementTest {

    private fun buildPet(isPremiumActive: Boolean, status: PetStatus = PetStatus.NORMAL) = Pet(
        id = "pet1",
        publicId = null,
        ownerId = "uid1",
        name = "Max",
        species = PetSpecies.DOG,
        breed = "Labrador",
        age = 3,
        photoUrl = null,
        notes = null,
        allergies = null,
        treatment = null,
        ownerName = null,
        phones = listOf("5512345678"),
        status = status,
        tagId = null,
        plan = if (isPremiumActive) PlanType.PREMIUM else PlanType.BASIC,
        isPremiumActive = isPremiumActive,
        paidUntil = null,
        createdAt = Date(),
        updatedAt = Date()
    )

    @Test
    fun `basic pet cannot edit profile`() {
        val pet = buildPet(isPremiumActive = false)
        assertFalse(pet.canEditProfile)
    }

    @Test
    fun `premium pet can edit profile`() {
        val pet = buildPet(isPremiumActive = true)
        assertTrue(pet.canEditProfile)
    }

    @Test
    fun `basic pet cannot toggle lost mode`() {
        val pet = buildPet(isPremiumActive = false)
        assertFalse(pet.canToggleLostMode)
    }

    @Test
    fun `premium pet can toggle lost mode`() {
        val pet = buildPet(isPremiumActive = true)
        assertTrue(pet.canToggleLostMode)
    }

    @Test
    fun `basic pet cannot update photo`() {
        val pet = buildPet(isPremiumActive = false)
        assertFalse(pet.canUpdatePhoto)
    }

    @Test
    fun `lost pet has isLost true`() {
        val pet = buildPet(isPremiumActive = true, status = PetStatus.LOST)
        assertTrue(pet.isLost)
        assertTrue(pet.status.isLost)
    }

    @Test
    fun `normal pet has isLost false`() {
        val pet = buildPet(isPremiumActive = false, status = PetStatus.NORMAL)
        assertFalse(pet.isLost)
    }

    @Test
    fun `pet with tagId has hasNFCTag true`() {
        val pet = buildPet(isPremiumActive = false).copy(tagId = "ABC123")
        assertTrue(pet.hasNFCTag)
    }

    @Test
    fun `pet without tagId has hasNFCTag false`() {
        val pet = buildPet(isPremiumActive = false)
        assertFalse(pet.hasNFCTag)
    }
}
