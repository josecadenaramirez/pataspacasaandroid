package com.cadena.pataspacasa

import com.cadena.pataspacasa.data.datasource.CloudFunctionDataSource
import com.cadena.pataspacasa.data.dto.PetDTO
import com.cadena.pataspacasa.data.repository.FirebasePetRepository
import com.cadena.pataspacasa.core.error.AppError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

/**
 * Regression tests for stale pet data issue.
 * Ensures that getPet(id) always fetches fresh data from the individual pet endpoint,
 * not from a cached list.
 */
class PetDetailDataFreshnessTest {

    private val cloudFunctionDataSource = mockk<CloudFunctionDataSource>()
    private val repository = FirebasePetRepository(cloudFunctionDataSource)

    private fun buildPetDTO(
        id: String = "pet1",
        name: String = "Max",
        breed: String = "Labrador",
        phones: List<String> = listOf("5512345678")
    ) = PetDTO(
        id = id,
        name = name,
        species = "dog",
        breed = breed,
        status = "normal",
        lostMode = null,
        isPremiumActive = false,
        phones = phones,
        paidUntil = null
    )

    @Test
    fun `getPet fetches from individual endpoint, not from list`() = runTest {
        val petId = "pet123"
        val updatedBreed = "Golden Retriever"
        val freshPhone = "5599999999"

        val freshDTO = buildPetDTO(
            id = petId,
            breed = updatedBreed,
            phones = listOf(freshPhone)
        )

        coEvery { cloudFunctionDataSource.getMyPet(petId) } returns freshDTO

        val result = repository.getPet(petId)

        assertEquals(petId, result.id)
        assertEquals(updatedBreed, result.breed)
        assertEquals(1, result.phones.size)
        assertEquals(freshPhone, result.phones[0])
    }

    @Test
    fun `getPet returns empty phones list when backend returns null`() = runTest {
        val petId = "pet123"
        val dtoWithNullPhones = buildPetDTO(id = petId, phones = listOf()).copy(phones = null)

        coEvery { cloudFunctionDataSource.getMyPet(petId) } returns dtoWithNullPhones

        val result = repository.getPet(petId)

        assertEquals(petId, result.id)
        assertTrue(result.phones.isEmpty())
    }

    @Test
    fun `getPet returns empty string for breed when backend returns null`() = runTest {
        val petId = "pet123"
        val dtoWithNullBreed = buildPetDTO(id = petId).copy(breed = null)

        coEvery { cloudFunctionDataSource.getMyPet(petId) } returns dtoWithNullBreed

        val result = repository.getPet(petId)

        assertEquals(petId, result.id)
        assertEquals("", result.breed)
    }

    @Test
    fun `getPet handles deleted phones - empty list becomes empty`() = runTest {
        val petId = "pet123"
        val updatedDTO = buildPetDTO(
            id = petId,
            phones = emptyList()
        )

        coEvery { cloudFunctionDataSource.getMyPet(petId) } returns updatedDTO

        val result = repository.getPet(petId)

        assertEquals(petId, result.id)
        assertTrue(result.phones.isEmpty())
    }

    @Test
    fun `getPet handles modified breed`() = runTest {
        val petId = "pet123"
        val oldBreed = "Labrador"
        val newBreed = "Golden Retriever"

        val dtoWithNewBreed = buildPetDTO(id = petId, breed = newBreed)

        coEvery { cloudFunctionDataSource.getMyPet(petId) } returns dtoWithNewBreed

        val result = repository.getPet(petId)

        assertEquals(petId, result.id)
        assertEquals(newBreed, result.breed)
        assertNotEquals(oldBreed, result.breed)
    }

    @Test
    fun `getPet propagates network errors`() = runTest {
        val petId = "pet123"

        coEvery { cloudFunctionDataSource.getMyPet(petId) } throws Exception("Network error")

        try {
            repository.getPet(petId)
            fail("Expected exception")
        } catch (e: AppError) {
            assertTrue(e is AppError)
        }
    }

    @Test
    fun `getPet handles pet not found error`() = runTest {
        val petId = "nonexistent"

        coEvery { cloudFunctionDataSource.getMyPet(petId) } throws Exception("Not found")

        try {
            repository.getPet(petId)
            fail("Expected exception")
        } catch (e: AppError) {
            assertTrue(e is AppError)
        }
    }

    @Test
    fun `phones and breed come from same endpoint response`() = runTest {
        val petId = "pet123"
        val expectedPhones = listOf("5512345678")
        val expectedBreed = "Labrador"

        val dto = buildPetDTO(
            id = petId,
            phones = expectedPhones,
            breed = expectedBreed
        )

        coEvery { cloudFunctionDataSource.getMyPet(petId) } returns dto

        val result = repository.getPet(petId)

        assertEquals(petId, result.id)
        assertEquals(expectedPhones, result.phones)
        assertEquals(expectedBreed, result.breed)
    }
}
