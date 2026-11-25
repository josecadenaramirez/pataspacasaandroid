package com.cadena.pataspacasa

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.datasource.RestPetDataSource
import com.cadena.pataspacasa.data.dto.*
import com.cadena.pataspacasa.data.repository.BackendPetRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import com.cadena.pataspacasa.domain.entity.*
import java.util.Date

class BackendPetRepositoryTest {
    private val remote = mockk<RestPetDataSource>()
    private val repository = BackendPetRepository(remote)

    @Test
    fun `claim refetches authoritative pet and never fabricates a fallback`() = runTest {
        coEvery { remote.claimPet("aBc123") } returns ClaimPetResponseDTO(true, "pet-1", "aBc123", false)
        coEvery { remote.getMyPet("pet-1") } returns GetMyPetResponseDTO(true, PetDTO(petId = "pet-1", name = "Luna"))

        val result = repository.claimPet("aBc123")

        assertEquals("pet-1", result.id)
        assertEquals("Luna", result.name)
        coVerify(exactly = 1) { remote.getMyPet("pet-1") }
    }

    @Test
    fun `claim propagates detail failure instead of reporting local success`() = runTest {
        coEvery { remote.claimPet(any()) } returns ClaimPetResponseDTO(true, "pet-1", null, false)
        coEvery { remote.getMyPet("pet-1") } throws AppError.NetworkUnavailable

        val error = runCatching { repository.claimPet("aBc123") }.exceptionOrNull()

        assertSame(AppError.NetworkUnavailable, error)
    }

    @Test
    fun `update returns exact server pet`() = runTest {
        val original = pet(name = "Antes")
        coEvery { remote.updatePet(original.id, any()) } returns
            UpdatePetResponseDTO(true, PetDTO(petId = original.id, name = "Después"))

        assertEquals("Después", repository.updatePet(original).name)
    }

    @Test
    fun `update maps the existing birth date without clearing it`() = runTest {
        val original = pet(name = "Luna").copy(approxBirthDate = PetCalendarDate(2020, 2, 29))
        val request = slot<UpdatePetRequestDTO>()
        coEvery { remote.updatePet(original.id, capture(request)) } returns
            UpdatePetResponseDTO(
                true,
                PetDTO(petId = original.id, name = original.name, approxBirthDate = "2020-02-29")
            )

        repository.updatePet(original)

        assertEquals("2020-02-29", request.captured.approxBirthDate)
    }

    @Test
    fun `photo upload refetches authoritative pet`() = runTest {
        val bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        coEvery { remote.uploadPetPhoto("pet-1", bytes) } returns
            UpdatePetPhotoResponseDTO(
                data = UpdatePetPhotoDataDTO(
                    photoUrl = "https://cdn.example/new.jpg",
                    updatedAt = "2026-11-11T12:00:00Z"
                )
            )
        coEvery { remote.getMyPet("pet-1") } returns GetMyPetResponseDTO(
            ok = true,
            pet = PetDTO(petId = "pet-1", name = "Luna", photoUrl = "https://cdn.example/new.jpg")
        )

        val result = repository.updatePetPhoto("pet-1", bytes)

        assertEquals("https://cdn.example/new.jpg", result.photoUrl)
        coVerify(exactly = 1) { remote.getMyPet("pet-1") }
    }

    @Test
    fun `photo upload rejects malformed success response`() = runTest {
        coEvery { remote.uploadPetPhoto(any(), any()) } returns
            UpdatePetPhotoResponseDTO(photoUrl = null, ok = true)

        val error = runCatching {
            repository.updatePetPhoto("pet-1", byteArrayOf(1))
        }.exceptionOrNull()

        assertSame(AppError.InvalidServerResponse, error)
    }

    private fun pet(name: String) = Pet(
        id = "pet-1", publicId = null, ownerId = "", name = name,
        species = PetSpecies.DOG, breed = "Labrador", petType = "dog",
        customPetType = null, customBreed = null, approxBirthDate = null,
        sex = null, sterilized = null, age = null, photoUrl = null, notes = null,
        allergies = null, treatment = null, ownerName = null, ownerEmail = null,
        phones = emptyList(), address = null, status = PetStatus.NORMAL,
        lostMode = false, tagId = null, activationStatus = null, nfcProgrammed = null,
        nfcProgrammedAt = null, plan = PlanType.BASIC, isPremiumActive = false,
        paidUntil = null, createdAt = Date(), updatedAt = Date()
    )
}
