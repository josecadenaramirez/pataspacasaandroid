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
        petType = "dog",
        customPetType = null,
        customBreed = null,
        approxBirthDate = null,
        sex = null,
        sterilized = null,
        age = 3,
        photoUrl = null,
        notes = null,
        allergies = null,
        treatment = null,
        ownerName = null,
        ownerEmail = null,
        address = null,
        phones = listOf("5512345678"),
        status = status,
        lostMode = false,
        tagId = null,
        activationStatus = null,
        nfcProgrammed = null,
        nfcProgrammedAt = null,
        plan = if (isPremiumActive) PlanType.PREMIUM else PlanType.BASIC,
        isPremiumActive = isPremiumActive,
        paidUntil = null,
        createdAt = Date(),
        updatedAt = Date()
    )

    @Test
    fun `all pets can edit profile regardless of plan`() {
        val basicPet = buildPet(isPremiumActive = false)
        assertTrue(basicPet.canEditProfile)
        val premiumPet = buildPet(isPremiumActive = true)
        assertTrue(premiumPet.canEditProfile)
    }

    @Test
    fun `all pets can toggle lost mode regardless of plan`() {
        val basicPet = buildPet(isPremiumActive = false)
        assertTrue(basicPet.canToggleLostMode)
        val premiumPet = buildPet(isPremiumActive = true)
        assertTrue(premiumPet.canToggleLostMode)
    }

    @Test
    fun `all pets can update photo regardless of plan`() {
        val basicPet = buildPet(isPremiumActive = false)
        assertTrue(basicPet.canUpdatePhoto)
        val premiumPet = buildPet(isPremiumActive = true)
        assertTrue(premiumPet.canUpdatePhoto)
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
