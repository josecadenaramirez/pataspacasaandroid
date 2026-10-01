package com.cadena.pataspacasa.domain.repository

import com.cadena.pataspacasa.domain.entity.Subscription

interface SubscriptionRepository {
    suspend fun getSubscription(): Subscription
    suspend fun isPremium(): Boolean
    suspend fun saveSubscription(subscription: Subscription)
}
