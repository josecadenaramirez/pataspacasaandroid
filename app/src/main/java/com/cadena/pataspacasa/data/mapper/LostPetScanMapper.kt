package com.cadena.pataspacasa.data.mapper

import com.cadena.pataspacasa.data.dto.LostPetScanDTO
import com.cadena.pataspacasa.domain.entity.LostPetScanEvent
import java.util.Date

object LostPetScanMapper {
    fun toDomain(dto: LostPetScanDTO): LostPetScanEvent = LostPetScanEvent(
        id = dto.id,
        petId = dto.petId,
        petName = dto.petName ?: "",
        scannedAt = dto.scannedAt?.let { PetMapper.parseIso8601(it) } ?: Date(),
        latitude = dto.latitude,
        longitude = dto.longitude,
        locationDescription = dto.locationDescription
    )
}
