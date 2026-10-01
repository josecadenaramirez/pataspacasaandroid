package com.cadena.pataspacasa.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.domain.entity.Subscription
import com.cadena.pataspacasa.domain.entity.SubscriptionProduct
import com.cadena.pataspacasa.domain.service.SubscriptionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PremiumViewState {
    object Loading : PremiumViewState()
    object Purchasing : PremiumViewState()
    data class AlreadyPremium(val subscription: Subscription) : PremiumViewState()
    data class Available(val products: List<SubscriptionProduct>) : PremiumViewState()
    data class Success(val subscription: Subscription) : PremiumViewState()
    data class Restored(val subscription: Subscription) : PremiumViewState()
    data class Error(val message: String) : PremiumViewState()
}

@HiltViewModel
class PremiumViewModel @Inject constructor(
    private val subscriptionService: SubscriptionService
) : ViewModel() {

    private val _state = MutableStateFlow<PremiumViewState>(PremiumViewState.Loading)
    val state: StateFlow<PremiumViewState> = _state.asStateFlow()

    private val _selectedProductId = MutableStateFlow<String?>(null)
    val selectedProductId: StateFlow<String?> = _selectedProductId.asStateFlow()

    fun loadData() {
        _state.value = PremiumViewState.Loading
        viewModelScope.launch {
            try {
                val current = subscriptionService.currentSubscription()
                if (current != null && current.isActive) {
                    _state.value = PremiumViewState.AlreadyPremium(current)
                    return@launch
                }
                val products = subscriptionService.loadProducts()
                if (products.isEmpty()) {
                    _state.value = PremiumViewState.Error(
                        "Suscripción no disponible\nNo pudimos obtener el plan Premium. Intenta nuevamente en unos minutos."
                    )
                } else {
                    _selectedProductId.value = products.first().id
                    _state.value = PremiumViewState.Available(products)
                }
            } catch (e: Exception) {
                _state.value = PremiumViewState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun selectProduct(id: String) { _selectedProductId.value = id }

    fun purchase() {
        val productId = _selectedProductId.value ?: return
        _state.value = PremiumViewState.Purchasing
        viewModelScope.launch {
            try {
                val sub = subscriptionService.purchase(productId)
                _state.value = PremiumViewState.Success(sub)
            } catch (e: AppError) {
                if (e is AppError.Unknown && e.reason.contains("cancelada", ignoreCase = true)) {
                    loadData()
                } else {
                    _state.value = PremiumViewState.Error(e.userMessage)
                }
            } catch (e: Exception) {
                _state.value = PremiumViewState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun restorePurchases() {
        _state.value = PremiumViewState.Loading
        viewModelScope.launch {
            try {
                val sub = subscriptionService.restorePurchases()
                if (sub != null && sub.isActive) {
                    _state.value = PremiumViewState.Restored(sub)
                } else {
                    _state.value = PremiumViewState.Error("No encontramos suscripciones activas para restaurar.")
                }
            } catch (e: Exception) {
                _state.value = PremiumViewState.Error(AppErrorMapper.from(e).userMessage)
            }
        }
    }

    fun retry() = loadData()
}
