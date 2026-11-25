package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.network.RestClient
import com.cadena.pataspacasa.domain.entity.Breed
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.domain.repository.BreedCatalogRepository
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendBreedCatalogRepository @Inject constructor(
    private val client: RestClient
) : BreedCatalogRepository {
    override suspend fun getBreeds(species: PetSpecies): List<Breed> {
        val response = client.request(
            "GET",
            "${AppConfiguration.apiBaseUrl}/breeds?type=${species.value}"
        )
        if (response.status !in 200..299) throw AppError.ServerError(response.status)
        return client.decode(response, BreedsResponse.serializer()).breeds.map { Breed(it.id, it.name) }
    }

    @Serializable private data class BreedDTO(val id: String, val name: String)
    @Serializable private data class BreedsResponse(val type: String, val breeds: List<BreedDTO>)
}
