package com.cadena.pataspacasa.data.service

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.datasource.CloudFunctionDataSource
import com.cadena.pataspacasa.data.dto.VerifySubscriptionRequestDTO
import com.cadena.pataspacasa.domain.entity.PlanType
import com.cadena.pataspacasa.domain.entity.Subscription
import com.cadena.pataspacasa.domain.entity.SubscriptionProduct
import com.cadena.pataspacasa.domain.entity.SubscriptionStatus
import com.cadena.pataspacasa.domain.service.SubscriptionService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object PlayBillingProductIds {
    const val PREMIUM_YEARLY = "com.pataspacasa.premium.yearly"
}

@Singleton
class GooglePlayBillingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudFunctionDataSource: CloudFunctionDataSource
) : SubscriptionService, PurchasesUpdatedListener {

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    private var purchaseContinuation: kotlinx.coroutines.CancellableContinuation<Subscription>? = null
    private var currentActivity: Activity? = null

    fun setActivity(activity: Activity) {
        currentActivity = activity
    }

    private suspend fun ensureConnected() {
        if (billingClient.isReady) return
        suspendCancellableCoroutine { cont ->
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        cont.resume(Unit)
                    } else {
                        cont.resumeWithException(AppError.Unknown("Billing setup failed: ${result.debugMessage}"))
                    }
                }
                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    override suspend fun loadProducts(): List<SubscriptionProduct> {
        return try {
            ensureConnected()
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(PlayBillingProductIds.PREMIUM_YEARLY)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()
            suspendCancellableCoroutine { cont ->
                billingClient.queryProductDetailsAsync(params) { result, productDetailsList ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        val products = productDetailsList.map { details ->
                            val offerDetails = details.subscriptionOfferDetails?.firstOrNull()
                            val pricePhase = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
                            SubscriptionProduct(
                                id = details.productId,
                                plan = PlanType.PREMIUM,
                                displayName = details.name,
                                description = details.description,
                                displayPrice = pricePhase?.formattedPrice ?: "",
                                billingPeriod = pricePhase?.billingPeriod ?: "P1Y"
                            )
                        }
                        cont.resume(products)
                    } else {
                        cont.resume(emptyList())
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun currentSubscription(): Subscription? {
        return try {
            ensureConnected()
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            suspendCancellableCoroutine { cont ->
                billingClient.queryPurchasesAsync(params) { result, purchases ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        val activePurchase = purchases.firstOrNull { purchase ->
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                                    purchase.products.contains(PlayBillingProductIds.PREMIUM_YEARLY)
                        }
                        if (activePurchase != null) {
                            cont.resume(
                                Subscription(
                                    id = activePurchase.orderId ?: activePurchase.purchaseToken,
                                    userId = "",
                                    plan = PlanType.PREMIUM,
                                    status = SubscriptionStatus.ACTIVE,
                                    expirationDate = null,
                                    productId = PlayBillingProductIds.PREMIUM_YEARLY
                                )
                            )
                        } else {
                            cont.resume(null)
                        }
                    } else {
                        cont.resume(null)
                    }
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun purchase(productId: String): Subscription {
        ensureConnected()
        val activity = currentActivity ?: throw AppError.Unknown("No activity available for purchase")

        val productDetails = suspendCancellableCoroutine<ProductDetails?> { cont ->
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(productId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()
            billingClient.queryProductDetailsAsync(params) { _, details ->
                cont.resume(details.firstOrNull())
            }
        } ?: throw AppError.Unknown("Product not found: $productId")

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: throw AppError.Unknown("No offer token for product: $productId")

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        return suspendCancellableCoroutine { cont ->
            purchaseContinuation = cont
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }

    override suspend fun restorePurchases(): Subscription? = currentSubscription()

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        val cont = purchaseContinuation ?: return
        purchaseContinuation = null

        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val purchase = purchases?.firstOrNull {
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                if (purchase != null) {
                    // Acknowledge purchase asynchronously
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    billingClient.acknowledgePurchase(ackParams) { _ -> }

                    cont.resume(
                        Subscription(
                            id = purchase.orderId ?: purchase.purchaseToken,
                            userId = "",
                            plan = PlanType.PREMIUM,
                            status = SubscriptionStatus.ACTIVE,
                            expirationDate = null,
                            productId = PlayBillingProductIds.PREMIUM_YEARLY
                        )
                    )
                } else {
                    cont.resumeWithException(AppError.Unknown("Purchase not found in result"))
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                cont.resumeWithException(AppError.Unknown("Compra cancelada."))
            else ->
                cont.resumeWithException(AppError.Unknown("Error de facturación: ${result.debugMessage}"))
        }
    }
}
