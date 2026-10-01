package com.cadena.pataspacasa.domain.entity

data class SubscriptionProduct(
    val id: String,
    val plan: PlanType,
    val displayName: String,
    val description: String,
    val displayPrice: String,
    val billingPeriod: String
)
