package com.cadena.pataspacasa

import com.cadena.pataspacasa.data.catalog.BreedCatalog
import com.cadena.pataspacasa.domain.entity.PetSpecies
import org.junit.Assert.assertTrue
import org.junit.Test

class BreedCatalogTest {
    @Test
    fun `dog and cat catalogs are non-empty and ordered`() {
        val dogs = BreedCatalog.breedsFor(PetSpecies.DOG)
        val cats = BreedCatalog.breedsFor(PetSpecies.CAT)
        assertTrue(dogs.first() == "Mestizo / Mixto")
        assertTrue(cats.first() == "Mestizo / Doméstico")
    }
}
