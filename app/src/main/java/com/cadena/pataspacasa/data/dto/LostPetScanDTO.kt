package com.cadena.pataspacasa.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class LostPetScanDTO(
    val id: String,
    val petId: String,
    val petName: String? = null,
    val scannedAt: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationDescription: String? = null
)

@Serializable
data class GetLostPetScansRequestDTO(
    val petId: String
)

@Serializable
data class GetLostPetScansResponseDTO(
    val ok: Boolean? = null,
    val scans: List<LostPetScanDTO> = emptyList()
)
