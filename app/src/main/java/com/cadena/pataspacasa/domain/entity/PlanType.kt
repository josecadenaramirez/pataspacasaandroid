package com.cadena.pataspacasa.domain.entity

enum class PlanType(val value: String) {
    BASIC("basic"),
    PREMIUM("premium");

    val displayName: String
        get() = when (this) {
            BASIC -> "Básico"
            PREMIUM -> "Premium"
        }

    val isPremium: Boolean get() = this == PREMIUM

    companion object {
        fun from(raw: String?): PlanType = values().find { it.value == raw } ?: BASIC
    }
}
