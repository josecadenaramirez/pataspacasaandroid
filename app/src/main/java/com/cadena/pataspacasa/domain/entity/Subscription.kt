package com.cadena.pataspacasa.domain.entity

import java.util.Date

enum class SubscriptionStatus(val value: String) {
    ACTIVE("active"),
    EXPIRED("expired"),
    CANCELLED("cancelled"),
    TRIAL("trial");

    val displayName: String
        get() = when (this) {
            ACTIVE -> "Activa"
            EXPIRED -> "Expirada"
            CANCELLED -> "Cancelada"
            TRIAL -> "Prueba"
        }

    companion object {
        fun from(raw: String?): SubscriptionStatus = values().find { it.value == raw } ?: EXPIRED
    }
}

data class Subscription(
    val id: String,
    val userId: String,
    val plan: PlanType,
    val status: SubscriptionStatus,
    val expirationDate: Date?,
    val productId: String? = null
) {
    val isActive: Boolean get() = status == SubscriptionStatus.ACTIVE
    val isExpired: Boolean get() = expirationDate?.before(Date()) ?: false
}
