package com.cadena.pataspacasa.domain.entity

import java.util.Date

data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val plan: PlanType,
    val createdAt: Date,
    val isEmailVerified: Boolean = false
) {
    val displayName: String get() = name
    val hasPremiumPlan: Boolean get() = plan == PlanType.PREMIUM
}
