package com.cadena.pataspacasa

import com.cadena.pataspacasa.domain.entity.*
import org.junit.Assert.*
import org.junit.Test
import java.util.Date

class SubscriptionStateTest {

    private fun buildSubscription(
        status: SubscriptionStatus,
        expirationDate: Date? = null
    ) = Subscription(
        id = "sub1",
        userId = "uid1",
        plan = PlanType.PREMIUM,
        status = status,
        expirationDate = expirationDate
    )

    @Test
    fun `active subscription isActive is true`() {
        val sub = buildSubscription(SubscriptionStatus.ACTIVE)
        assertTrue(sub.isActive)
    }

    @Test
    fun `expired subscription isActive is false`() {
        val sub = buildSubscription(SubscriptionStatus.EXPIRED)
        assertFalse(sub.isActive)
    }

    @Test
    fun `cancelled subscription isActive is false`() {
        val sub = buildSubscription(SubscriptionStatus.CANCELLED)
        assertFalse(sub.isActive)
    }

    @Test
    fun `expired date in past makes isExpired true`() {
        val pastDate = Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24) // yesterday
        val sub = buildSubscription(SubscriptionStatus.ACTIVE, expirationDate = pastDate)
        assertTrue(sub.isExpired)
    }

    @Test
    fun `future expiration makes isExpired false`() {
        val futureDate = Date(System.currentTimeMillis() + 1000L * 60 * 60 * 24 * 30) // 30 days
        val sub = buildSubscription(SubscriptionStatus.ACTIVE, expirationDate = futureDate)
        assertFalse(sub.isExpired)
    }

    @Test
    fun `basic plan hasPremiumPlan is false`() {
        val user = User(
            id = "uid",
            name = "Name",
            email = "test@test.com",
            photoUrl = null,
            plan = PlanType.BASIC,
            createdAt = Date()
        )
        assertFalse(user.hasPremiumPlan)
    }

    @Test
    fun `premium plan hasPremiumPlan is true`() {
        val user = User(
            id = "uid",
            name = "Name",
            email = "test@test.com",
            photoUrl = null,
            plan = PlanType.PREMIUM,
            createdAt = Date()
        )
        assertTrue(user.hasPremiumPlan)
    }
}
