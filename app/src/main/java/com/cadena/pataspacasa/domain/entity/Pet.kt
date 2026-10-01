package com.cadena.pataspacasa.domain.entity

import java.util.Date

data class Pet(
    val id: String,
    val publicId: String?,
    val ownerId: String,
    val name: String,
    val species: PetSpecies,
    val breed: String,
    val age: Int?,
    val photoUrl: String?,
    val notes: String?,
    val allergies: String?,
    val treatment: String?,
    val ownerName: String?,
    val phones: List<String>,
    val status: PetStatus,
    val tagId: String?,
    val plan: PlanType,
    val isPremiumActive: Boolean,
    val paidUntil: Date?,
    val createdAt: Date,
    val updatedAt: Date
) {
    val isLost: Boolean get() = status.isLost
    val hasNFCTag: Boolean get() = tagId != null
    val canEditProfile: Boolean get() = isPremiumActive
    val canToggleLostMode: Boolean get() = isPremiumActive
    val canUpdatePhoto: Boolean get() = isPremiumActive
}
