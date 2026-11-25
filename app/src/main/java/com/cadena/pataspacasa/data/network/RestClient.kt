package com.cadena.pataspacasa.data.network

import com.cadena.pataspacasa.core.error.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class RestResponse(val status: Int, val body: String)

@Singleton
class RestClient @Inject constructor() {
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    suspend fun request(
        method: String,
        url: String,
        bearerToken: String? = null,
        body: String? = null,
        bodyBytes: ByteArray? = null,
        contentType: String = "application/json",
        timeoutMillis: Int = 15_000,
        noCache: Boolean = false
    ): RestResponse = withContext(Dispatchers.IO) {
        if (body != null && bodyBytes != null) throw AppError.InvalidServerResponse
        val connection = try {
            (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = timeoutMillis
                readTimeout = timeoutMillis
                setRequestProperty("Accept", "application/json")
                bearerToken?.let { setRequestProperty("Authorization", "Bearer $it") }
                if (noCache) {
                    useCaches = false
                    setRequestProperty("Cache-Control", "no-store, no-cache")
                    setRequestProperty("Pragma", "no-cache")
                }
                val requestBody = bodyBytes ?: body?.toByteArray(Charsets.UTF_8)
                if (requestBody != null) {
                    doOutput = true
                    setRequestProperty("Content-Type", contentType)
                    setFixedLengthStreamingMode(requestBody.size)
                    outputStream.use { it.write(requestBody) }
                }
            }
        } catch (_: IOException) {
            throw AppError.NetworkUnavailable
        }

        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            RestResponse(status, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } catch (_: IOException) {
            throw AppError.NetworkUnavailable
        } finally {
            connection.disconnect()
        }
    }

    fun <T> decode(response: RestResponse, serializer: KSerializer<T>): T = try {
        json.decodeFromString(serializer, response.body)
    } catch (_: Exception) {
        throw AppError.InvalidServerResponse
    }
}
