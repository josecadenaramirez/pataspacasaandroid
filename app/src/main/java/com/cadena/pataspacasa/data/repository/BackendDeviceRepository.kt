package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.auth.InstallationIdentity
import com.cadena.pataspacasa.data.datasource.BackendAuthDataSource
import com.cadena.pataspacasa.data.network.RestClient
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackendDeviceRepository @Inject constructor(
    private val auth: BackendAuthDataSource,
    private val client: RestClient,
    private val installationIdentity: InstallationIdentity
) {
    suspend fun upsert(fcmToken: String) {
        if (fcmToken.isBlank()) return
        send(
            method = "POST",
            path = "me/devices",
            body = client.json.encodeToString(
                RegisterDeviceRequest.serializer(),
                RegisterDeviceRequest(installationIdentity.value, fcmToken, "android")
            )
        )
    }

    suspend fun delete() = send("DELETE", "me/devices/${installationIdentity.value}", null)

    private suspend fun send(method: String, path: String, body: String?) {
        var token = auth.getAccessToken(false)
        repeat(2) { attempt ->
            val response = client.request(
                method,
                "${AppConfiguration.apiBaseUrl}/$path",
                bearerToken = token,
                body = body
            )
            android.util.Log.d("PushQA", "$method $path -> HTTP ${response.status}")
            if (method == "POST" && path == "me/devices" && response.status == 400) {
                android.util.Log.d("PushQA", "Respuesta del 400: ${response.body}")
            }
            if (response.status == 401 && attempt == 0) token = auth.getAccessToken(true)
            else {
                if (response.status !in 200..299) throw AppError.ServerError(response.status)
                return
            }
        }
        throw AppError.SessionExpired
    }

    @Serializable
    private data class RegisterDeviceRequest(
        val installationId: String,
        val fcmToken: String,
        val platform: String
    )
}
