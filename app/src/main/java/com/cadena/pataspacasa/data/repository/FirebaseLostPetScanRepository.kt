package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.datasource.CloudFunctionDataSource
import com.cadena.pataspacasa.data.dto.GetLostPetScansRequestDTO
import com.cadena.pataspacasa.data.mapper.LostPetScanMapper
import com.cadena.pataspacasa.domain.entity.LostPetScanEvent
import com.cadena.pataspacasa.domain.repository.LostPetScanRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseLostPetScanRepository @Inject constructor(
    private val dataSource: CloudFunctionDataSource
) : LostPetScanRepository {

    override suspend fun getScans(petId: String): List<LostPetScanEvent> {
        val request = GetLostPetScansRequestDTO(petId = petId)
        val resp = dataSource.getLostPetScans(request)
        return resp.scans.map { LostPetScanMapper.toDomain(it) }
    }

    override suspend fun getScan(eventId: String, petId: String): LostPetScanEvent {
        val scans = getScans(petId)
        return scans.firstOrNull { it.id == eventId } ?: throw AppError.NotFound
    }
}
