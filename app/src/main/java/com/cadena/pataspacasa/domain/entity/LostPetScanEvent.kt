package com.cadena.pataspacasa.domain.entity

import java.util.Date

data class LostPetScanEvent(
    val id: String,
    val petId: String,
    val petName: String,
    val scannedAt: Date,
    val latitude: Double?,
    val longitude: Double?,
    val locationDescription: String?
)
