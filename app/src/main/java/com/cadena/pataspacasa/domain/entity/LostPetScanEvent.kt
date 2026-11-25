package com.cadena.pataspacasa.domain.entity

import java.util.Date

data class LostPetScanEvent(
    val id: String,
    val petId: String,
    val petName: String,
    val scannedAt: Date?,
    val latitude: Double?,
    val longitude: Double?,
    val locationDescription: String?,
    val tagId: String = "",
    val source: String = "",
    val hasLocation: Boolean = latitude != null && longitude != null,
    val accuracyMeters: Double? = null,
    val visitorDeniedLocation: Boolean = false,
    val pushEligible: Boolean = false
)
