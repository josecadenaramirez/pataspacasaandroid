package com.cadena.pataspacasa.domain.entity

import java.util.Date

/**
 * Domain entity for a User.
 * Mirrors iOS User struct structure and computed properties.
 */
data class User(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String?,
    val role: String? = null,

    /**
     * Subscribed plan type — informational only.
     * ⚠️ DO NOT use for access control.
     * Permission authority is per-pet via pet.isPremiumActive.
     */
    val plan: PlanType,

    val createdAt: Date,

    /**
     * Whether email was verified by Firebase Auth.
     * Obtained from Data layer when mapping FirebaseAuth.User.
     */
    val isEmailVerified: Boolean = false
) {
    // MARK: - Computed Properties

    /**
     * Display name with fallback.
     */
    val displayName: String
        get() {
            val trimmed = name.trim()
            return if (trimmed.isEmpty()) "Usuario" else trimmed
        }

    /**
     * Whether subscribed to premium plan.
     * ⚠️ This does NOT guarantee active permissions.
     * A plan can be .premium but expired → pet.isPremiumActive == false.
     * Use only for UI info (premium badge, plan display, etc).
     */
    val hasPremiumPlan: Boolean
        get() = plan == PlanType.PREMIUM

    // MARK: - Backward Compatibility Aliases

    val subscriptionPlan: PlanType
        get() = plan

    @Deprecated(
        "Does not determine effective permissions. Use pet.isPremiumActive.",
        replaceWith = ReplaceWith("hasPremiumPlan")
    )
    val isPremium: Boolean
        get() = plan == PlanType.PREMIUM
}
