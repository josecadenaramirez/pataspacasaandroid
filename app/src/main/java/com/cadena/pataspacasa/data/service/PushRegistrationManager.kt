package com.cadena.pataspacasa.data.service

import com.cadena.pataspacasa.data.repository.BackendDeviceRepository
import com.cadena.pataspacasa.domain.repository.AuthRepository
import com.cadena.pataspacasa.domain.repository.SessionState
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PushRegistrationManager @Inject constructor(
    private val authRepository: AuthRepository,
    private val devices: BackendDeviceRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun start() {
        scope.launch {
            authRepository.observeAuthState().collectLatest { state ->
                if (state is SessionState.Authenticated) {
                    runCatching { devices.upsert(FirebaseMessaging.getInstance().token.await()) }
                }
            }
        }
    }

    fun register(token: String) {
        if (authRepository.currentUser() == null) return
        scope.launch { runCatching { devices.upsert(token) } }
    }
}
