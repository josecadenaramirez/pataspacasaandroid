package com.cadena.pataspacasa

import com.cadena.pataspacasa.data.catalog.BreedCatalog
import com.cadena.pataspacasa.domain.entity.PetSpecies
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BreedCatalogTest {
    @Test
    fun `bundled catalogs preserve species labels and order`() {
        val dogs = BreedCatalog.breedsFor(PetSpecies.DOG)
        val cats = BreedCatalog.breedsFor(PetSpecies.CAT)

        assertEquals("Mestizo / Mixto", dogs.first())
        assertEquals("Xoloitzcuintle", dogs.last())
        assertEquals("Mestizo / Doméstico", cats.first())
        assertEquals("Turkish Van", cats.last())
        assertTrue(BreedCatalog.breedsFor(PetSpecies.OTHER).isEmpty())
    }
}
