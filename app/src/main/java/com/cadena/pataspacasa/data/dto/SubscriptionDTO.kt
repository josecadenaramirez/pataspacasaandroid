package com.cadena.pataspacasa.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class VerifySubscriptionRequestDTO(
    val productId: String,
    val purchaseToken: String,
    val platform: String = "android"
)

@Serializable
data class VerifySubscriptionResponseDTO(
    val ok: Boolean,
    val plan: String? = null,
    val paidUntil: String? = null,
    val productId: String? = null,
    val expiresAt: String? = null
)
