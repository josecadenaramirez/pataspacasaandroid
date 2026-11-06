package com.cadena.pataspacasa.domain.entity

import java.util.Date

/**
 * Domain entity for a Pet.
 * Mirrors iOS Pet struct structure and computed properties.
 * Authority for permission decisions: Backend via isPremiumActive flag.
 */
data class Pet(
    // MARK: - Identification
    val id: String,
    val publicId: String?,           // Opaque ID for public URLs (pataspacasa.com/p/{publicId})
    val ownerId: String,

    // MARK: - Basic Info
    val name: String,
    val species: PetSpecies,
    val breed: String,
    val petType: String,             // Display type (usually matches species)
    val customPetType: String?,       // Custom pet type if user provides one
    val customBreed: String?,         // Custom breed if not in catalog
    val approxBirthDate: PetCalendarDate?,
    val sex: String?,
    val sterilized: Boolean?,
    val age: Int?,
    val photoUrl: String?,
    val notes: String?,

    // MARK: - Medical / Care Info
    val allergies: String?,          // Known allergies
    val treatment: String?,          // Active medications or treatments

    // MARK: - Owner Contact (visible in public profile)
    val ownerName: String?,          // Visible name in public tag
    val ownerEmail: String?,
    val phones: List<String> = emptyList(),  // Emergency contact numbers
    val address: String?,

    // MARK: - Status
    val status: PetStatus,
    val lostMode: Boolean = false,
    val tagId: String?,              // Linked NFC tag ID
    val activationStatus: String?,   // Tag activation status
    val nfcProgrammed: Boolean?,     // Whether NFC tag has been programmed
    val nfcProgrammedAt: Date?,

    // MARK: - Plan / Subscription
    val plan: PlanType = PlanType.BASIC,  // Informational plan type
    /**
     * Authority for premium permissions.
     * Replaces any local plan/date logic. Only this flag decides access.
     */
    val isPremiumActive: Boolean = false,
    val paidUntil: Date?,            // Subscription expiration date

    // MARK: - Timestamps
    val createdAt: Date,
    val updatedAt: Date
) {
    // MARK: - Computed Properties

    val isLost: Boolean
        get() = lostMode || status.isLost

    val hasNFCTag: Boolean
        get() = tagId != null

    /**
     * User can always edit pet profile regardless of plan.
     */
    val canEditProfile: Boolean
        get() = true

    /**
     * User can always toggle lost mode regardless of plan.
     */
    val canToggleLostMode: Boolean
        get() = true

    /**
     * User can always update pet photo regardless of plan.
     */
    val canUpdatePhoto: Boolean
        get() = true

    // MARK: - Backward Compatibility Aliases

    val nfcTagId: String?
        get() = tagId
}
