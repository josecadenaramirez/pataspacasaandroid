package com.cadena.pataspacasa

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.domain.entity.*
import com.cadena.pataspacasa.domain.repository.PetRepository
import com.cadena.pataspacasa.domain.usecase.pet.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class PetActivationTest {

    private val petRepository = mockk<PetRepository>()
    private val claimPetUseCase = ClaimPetUseCase(petRepository)
    private val enableLostModeUseCase = EnableLostModeUseCase(petRepository)
    private val disableLostModeUseCase = DisableLostModeUseCase(petRepository)

    private fun buildPet(isPremium: Boolean = false, status: PetStatus = PetStatus.NORMAL) = Pet(
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
        tagId = "ABC123",
        activationStatus = null,
        nfcProgrammed = null,
        nfcProgrammedAt = null,
        plan = if (isPremium) PlanType.PREMIUM else PlanType.BASIC,
        isPremiumActive = isPremium,
        paidUntil = null,
        createdAt = Date(),
        updatedAt = Date()
    )

    @Test
    fun `claimPet calls repository with correct tagId`() = runTest {
        val expected = buildPet()
        coEvery { petRepository.claimPet("ABC123") } returns expected
        val result = claimPetUseCase.execute("ABC123")
        assertEquals("pet1", result.id)
        coVerify { petRepository.claimPet("ABC123") }
    }

    @Test
    fun `enableLostMode calls setLostMode on repository`() = runTest {
        val expected = buildPet(isPremium = true, status = PetStatus.LOST)
        coEvery { petRepository.setLostMode("pet1") } returns expected
        val result = enableLostModeUseCase.execute("pet1")
        assertTrue(result.isLost)
    }

    @Test
    fun `disableLostMode calls disableLostMode on repository`() = runTest {
        val expected = buildPet(isPremium = true, status = PetStatus.NORMAL)
        coEvery { petRepository.disableLostMode("pet1") } returns expected
        val result = disableLostModeUseCase.execute("pet1")
        assertFalse(result.isLost)
    }

    @Test
    fun `claimPet throws PetNotFound if tag does not exist`() = runTest {
        coEvery { petRepository.claimPet("INVALID") } throws AppError.TagNotFound
        val exception = runCatching { claimPetUseCase.execute("INVALID") }.exceptionOrNull()
        assertTrue(exception is AppError.TagNotFound)
    }
}
