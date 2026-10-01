package com.cadena.pataspacasa

import com.cadena.pataspacasa.data.mapper.PetMapper
import com.cadena.pataspacasa.data.dto.PetDTO
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.domain.entity.PetStatus
import com.cadena.pataspacasa.domain.entity.PlanType
import org.junit.Assert.*
import org.junit.Test

class PetMapperTest {

    private fun buildDto(
        id: String = "pet1",
        name: String = "Max",
        species: String? = "dog",
        breed: String? = "Labrador",
        status: String? = "normal",
        lostMode: Boolean? = null,
        isPremiumActive: Boolean? = false,
        phones: List<String>? = listOf("5512345678"),
        paidUntil: String? = null
    ) = PetDTO(
        id = id,
        name = name,
        species = species,
        breed = breed,
        status = status,
        lostMode = lostMode,
        isPremiumActive = isPremiumActive,
        phones = phones,
        paidUntil = paidUntil
    )

    @Test
    fun `maps dog species correctly`() {
        val pet = PetMapper.toDomain(buildDto(species = "dog"))
        assertEquals(PetSpecies.DOG, pet.species)
    }

    @Test
    fun `maps cat species correctly`() {
        val pet = PetMapper.toDomain(buildDto(species = "cat"))
        assertEquals(PetSpecies.CAT, pet.species)
    }

    @Test
    fun `maps unknown species as other`() {
        val pet = PetMapper.toDomain(buildDto(species = "unknown"))
        assertEquals(PetSpecies.OTHER, pet.species)
    }

    @Test
    fun `lostMode flag overrides status string`() {
        val pet = PetMapper.toDomain(buildDto(status = "normal", lostMode = true))
        assertEquals(PetStatus.LOST, pet.status)
    }

    @Test
    fun `null lostMode uses status string`() {
        val pet = PetMapper.toDomain(buildDto(status = "lost", lostMode = null))
        assertEquals(PetStatus.LOST, pet.status)
    }

    @Test
    fun `phones mapped correctly`() {
        val pet = PetMapper.toDomain(buildDto(phones = listOf("5512345678", "5598765432")))
        assertEquals(2, pet.phones.size)
        assertEquals("5512345678", pet.phones[0])
    }

    @Test
    fun `isPremiumActive defaults to false when null`() {
        val pet = PetMapper.toDomain(buildDto(isPremiumActive = null))
        assertFalse(pet.isPremiumActive)
    }

    @Test
    fun `isPremiumActive mapped from DTO`() {
        val pet = PetMapper.toDomain(buildDto(isPremiumActive = true))
        assertTrue(pet.isPremiumActive)
    }

    @Test
    fun `valid paidUntil date parsed`() {
        val pet = PetMapper.toDomain(buildDto(paidUntil = "2027-08-01"))
        assertNotNull(pet.paidUntil)
    }

    @Test
    fun `invalid paidUntil date returns null`() {
        val pet = PetMapper.toDomain(buildDto(paidUntil = "not-a-date"))
        assertNull(pet.paidUntil)
    }
}
