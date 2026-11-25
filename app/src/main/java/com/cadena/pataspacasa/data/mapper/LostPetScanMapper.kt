package com.cadena.pataspacasa.data.mapper

import com.cadena.pataspacasa.data.dto.LostPetScanDTO
import com.cadena.pataspacasa.domain.entity.LostPetScanEvent
import java.util.Date

object LostPetScanMapper {
    fun toDomain(dto: LostPetScanDTO): LostPetScanEvent {
        // Resolve id and timestamp from either legacy Android keys or backend/iOS keys
        val resolvedId = dto.resolvedId()
        val rawDate = dto.resolvedScannedAt()
        val parsedDate = rawDate?.let { PetMapper.parseIso8601(it) }

        // Resolve coordinates: prefer direct latitude/longitude, fall back to nested locationApprox
        val lat = dto.latitude ?: dto.locationApprox?.latitude
        val lon = dto.longitude ?: dto.locationApprox?.longitude

        return LostPetScanEvent(
            id = resolvedId,
            petId = dto.petId ?: "",
            petName = dto.petName ?: "",
            scannedAt = parsedDate,
            latitude = lat,
            longitude = lon,
            locationDescription = dto.locationDescription,
            tagId = dto.tagId.orEmpty(),
            source = dto.source.orEmpty(),
            hasLocation = dto.hasLocation,
            accuracyMeters = dto.locationApprox?.accuracyMeters,
            visitorDeniedLocation = dto.visitorDeniedLocation,
            pushEligible = dto.pushEligible
        )
    }
}
