package com.cadena.pataspacasa.domain.service

import com.cadena.pataspacasa.domain.entity.Subscription
import com.cadena.pataspacasa.domain.entity.SubscriptionProduct

interface SubscriptionService {
    suspend fun loadProducts(): List<SubscriptionProduct>
    suspend fun currentSubscription(): Subscription?
    suspend fun purchase(productId: String): Subscription
    suspend fun restorePurchases(): Subscription?
}
