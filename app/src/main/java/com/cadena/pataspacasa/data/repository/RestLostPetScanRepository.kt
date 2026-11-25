package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.datasource.BackendAuthDataSource
import com.cadena.pataspacasa.data.dto.RestLostPetScanDetailResponseDTO
import com.cadena.pataspacasa.data.dto.RestLostPetScansResponseDTO
import com.cadena.pataspacasa.data.mapper.LostPetScanMapper
import com.cadena.pataspacasa.data.network.RestClient
import com.cadena.pataspacasa.domain.entity.LostPetScanEvent
import com.cadena.pataspacasa.domain.repository.LostPetScanRepository
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestLostPetScanRepository @Inject constructor(
    private val auth: BackendAuthDataSource,
    private val client: RestClient
) : LostPetScanRepository {
    override suspend fun getScans(petId: String): List<LostPetScanEvent> {
        val query = "petId=${encode(petId)}&limit=50"
        val response = request("me/lost-pet-scans?$query")
        val decoded = client.decode(response, RestLostPetScansResponseDTO.serializer())
        if (!decoded.ok) throw AppError.InvalidServerResponse
        return decoded.scans.map { dto ->
            if (dto.resolvedId().isBlank() || dto.petId.isNullOrBlank()) throw AppError.InvalidServerResponse
            LostPetScanMapper.toDomain(dto)
        }
    }

    override suspend fun getScan(eventId: String, petId: String): LostPetScanEvent {
        val response = request("me/lost-pet-scans/${encode(eventId)}")
        val decoded = client.decode(response, RestLostPetScanDetailResponseDTO.serializer())
        if (!decoded.ok || decoded.scan.resolvedId().isBlank() || decoded.scan.petId.isNullOrBlank()) {
            throw AppError.InvalidServerResponse
        }
        if (decoded.scan.petId != petId) throw AppError.PermissionDenied
        return LostPetScanMapper.toDomain(decoded.scan)
    }

    private suspend fun request(path: String): com.cadena.pataspacasa.data.network.RestResponse {
        var token = auth.getAccessToken(false)
        repeat(2) { attempt ->
            val response = client.request(
                method = "GET",
                url = "${AppConfiguration.apiBaseUrl}/$path",
                bearerToken = token,
                timeoutMillis = 10_000,
                noCache = true
            )
            if (response.status == 401 && attempt == 0) {
                token = auth.getAccessToken(true)
            } else {
                when (response.status) {
                    in 200..299 -> return response
                    401 -> throw AppError.NotAuthenticated
                    403 -> throw AppError.PermissionDenied
                    404 -> throw AppError.NotFound
                    else -> throw AppError.ServerError(response.status)
                }
            }
        }
        throw AppError.SessionExpired
    }

    private fun encode(value: String): String = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
}
