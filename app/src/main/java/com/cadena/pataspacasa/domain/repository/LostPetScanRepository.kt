package com.cadena.pataspacasa.domain.repository

import com.cadena.pataspacasa.domain.entity.LostPetScanEvent

interface LostPetScanRepository {
    suspend fun getScans(petId: String): List<LostPetScanEvent>
    suspend fun getScan(eventId: String, petId: String): LostPetScanEvent
}
