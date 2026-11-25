package com.cadena.pataspacasa.data.dto

import kotlinx.serialization.Serializable

/**
 * DTOs tolerant to both the Android-old shape (scans / scannedAt / id)
 * and the iOS/backend shape (items / createdAt / eventId).
 */
@Serializable
data class LostPetScanDTO(
    val scanId: String? = null,
    val id: String? = null,
    val eventId: String? = null,
    val petId: String? = null,
    val petName: String? = null,
    val tagId: String? = null,
    val scannedAt: String? = null,
    val createdAt: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationDescription: String? = null,
    val locationApprox: LocationApproxDTO? = null,
    val source: String? = null,
    val hasLocation: Boolean = false,
    val visitorDeniedLocation: Boolean = false,
    val pushEligible: Boolean = false
) {
    fun resolvedId(): String = scanId ?: id ?: eventId ?: ""
    fun resolvedScannedAt(): String? = scannedAt ?: createdAt
}

@Serializable
data class LocationApproxDTO(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Double? = null
)

@Serializable
data class GetLostPetScansRequestDTO(
    val petId: String
)

@Serializable
data class GetLostPetScansResponseDTO(
    val ok: Boolean? = null,
    // old android shape
    val scans: List<LostPetScanDTO> = emptyList(),
    // iOS/backend shape
    val items: List<LostPetScanDTO> = emptyList(),
    // pagination support returned by backend
    val nextCursor: String? = null
)

@Serializable
data class RestLostPetScansResponseDTO(val ok: Boolean, val scans: List<LostPetScanDTO>)

@Serializable
data class RestLostPetScanDetailResponseDTO(val ok: Boolean, val scan: LostPetScanDTO)
