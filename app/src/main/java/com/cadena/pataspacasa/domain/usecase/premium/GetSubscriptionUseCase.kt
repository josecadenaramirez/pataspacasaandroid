package com.cadena.pataspacasa.domain.usecase.premium

import com.cadena.pataspacasa.domain.entity.Subscription
import com.cadena.pataspacasa.domain.repository.SubscriptionRepository
import javax.inject.Inject

class GetSubscriptionUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend fun execute(): Subscription = subscriptionRepository.getSubscription()
}
