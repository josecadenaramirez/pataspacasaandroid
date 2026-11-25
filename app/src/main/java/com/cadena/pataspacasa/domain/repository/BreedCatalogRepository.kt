package com.cadena.pataspacasa.domain.repository

import com.cadena.pataspacasa.domain.entity.Breed
import com.cadena.pataspacasa.domain.entity.PetSpecies

interface BreedCatalogRepository {
    suspend fun getBreeds(species: PetSpecies): List<Breed>
}
