package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.domain.entity.PlanType
import com.cadena.pataspacasa.domain.entity.Subscription
import com.cadena.pataspacasa.domain.entity.SubscriptionStatus
import com.cadena.pataspacasa.domain.repository.SubscriptionRepository
import com.cadena.pataspacasa.domain.service.SubscriptionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val subscriptionService: SubscriptionService
) : SubscriptionRepository {

    private var cachedSubscription: Subscription? = null

    override suspend fun getSubscription(): Subscription {
        val sub = subscriptionService.currentSubscription()
        cachedSubscription = sub
        return sub ?: throw AppError.SubscriptionNotFound
    }

    override suspend fun isPremium(): Boolean {
        return try {
            val sub = subscriptionService.currentSubscription()
            sub?.isActive == true
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun saveSubscription(subscription: Subscription) {
        cachedSubscription = subscription
    }
}
