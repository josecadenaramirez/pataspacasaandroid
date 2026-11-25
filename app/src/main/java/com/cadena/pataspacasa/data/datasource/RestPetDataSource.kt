package com.cadena.pataspacasa.data.datasource

import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.data.dto.*
import com.cadena.pataspacasa.data.network.RestClient
import com.cadena.pataspacasa.data.network.RestResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.ByteArrayOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestPetDataSource @Inject constructor(
    private val auth: BackendAuthDataSource,
    private val client: RestClient
) {
    suspend fun getMyPets(): GetMyPetsResponseDTO =
        authorized("GET", "me/pets", serializer = GetMyPetsResponseDTO.serializer())

    suspend fun getMyPet(id: String): GetMyPetResponseDTO =
        authorized("GET", "me/pets/${segment(id)}", serializer = GetMyPetResponseDTO.serializer())

    suspend fun claimPet(tagId: String): ClaimPetResponseDTO =
        authorized(
            "POST",
            "me/pets/claim/${segment(tagId)}",
            body = "{}",
            serializer = ClaimPetResponseDTO.serializer()
        )

    suspend fun updatePet(id: String, request: UpdatePetRequestDTO): UpdatePetResponseDTO =
        authorized(
            "PATCH",
            "me/pets/${segment(id)}",
            body = updateBody(request),
            serializer = UpdatePetResponseDTO.serializer()
        )

    suspend fun uploadPetPhoto(id: String, imageData: ByteArray): UpdatePetPhotoResponseDTO {
        if (!isJpeg(imageData)) throw AppError.InvalidPhotoType
        if (imageData.size > MAX_PHOTO_BYTES) throw AppError.PhotoTooLarge
        val boundary = "PatasPaCasa-${UUID.randomUUID()}"
        val multipart = buildPhotoMultipart(boundary, imageData)
        return authorized(
            method = "POST",
            path = "pets/${segment(id)}/photo",
            bodyBytes = multipart,
            contentType = "multipart/form-data; boundary=$boundary",
            serializer = UpdatePetPhotoResponseDTO.serializer()
        )
    }

    suspend fun setLostMode(id: String, enabled: Boolean): SetPetLostModeResponseDTO =
        authorized(
            "PATCH",
            "me/pets/${segment(id)}/lost-mode",
            body = client.json.encodeToString(LostModeBody.serializer(), LostModeBody(enabled)),
            serializer = SetPetLostModeResponseDTO.serializer()
        )

    private suspend fun <T> authorized(
        method: String,
        path: String,
        body: String? = null,
        bodyBytes: ByteArray? = null,
        contentType: String = "application/json",
        serializer: kotlinx.serialization.KSerializer<T>
    ): T {
        var token = auth.getAccessToken(false)
        repeat(2) { attempt ->
            val response = client.request(
                method = method,
                url = "${AppConfiguration.apiBaseUrl}/$path",
                bearerToken = token,
                body = body,
                bodyBytes = bodyBytes,
                contentType = contentType,
                noCache = true
            )
            if (response.status == 401 && attempt == 0) {
                token = auth.getAccessToken(true)
            } else {
                if (response.status !in 200..299) throw mapError(response)
                return client.decode(response, serializer)
            }
        }
        throw AppError.SessionExpired
    }

    private fun mapError(response: RestResponse): AppError {
        val code = runCatching {
            client.json.decodeFromString(BackendErrorDTO.serializer(), response.body).error
        }.getOrNull()
        return when (code) {
            "unauthenticated" -> if (response.status == 401) AppError.SessionExpired else AppError.NotAuthenticated
            "user_not_found" -> AppError.UserNotFound
            "tag_not_found" -> AppError.TagNotFound
            "tag_not_activated" -> AppError.TagDisabled
            "email_mismatch" -> AppError.PermissionDenied
            "already_claimed" -> AppError.TagAlreadyLinked
            "pet_not_found" -> AppError.PetNotFound
            "validation_failed" -> AppError.TagInvalid
            "invalid_type" -> AppError.InvalidPhotoType
            "file_too_large" -> AppError.PhotoTooLarge
            "no_file", "upload_error" -> AppError.PhotoUploadFailed
            "unauthorized" -> if (response.status == 401) AppError.SessionExpired else AppError.PermissionDenied
            else -> when (response.status) {
                401 -> AppError.SessionExpired
                403 -> AppError.PermissionDenied
                404 -> AppError.PetNotFound
                413 -> AppError.PhotoRejectedByGateway
                422 -> AppError.TagInvalid
                else -> AppError.ServerError(response.status)
            }
        }
    }

    private fun updateBody(request: UpdatePetRequestDTO): String = buildJsonObject {
        fun string(name: String, value: String?) { put(name, value?.let(::JsonPrimitive) ?: JsonNull) }
        string("name", request.name)
        string("petType", request.petType)
        string("customPetType", request.customPetType)
        string("breed", request.breed)
        string("customBreed", request.customBreed)
        string("approxBirthDate", request.approxBirthDate)
        string("sex", request.sex)
        put("sterilized", request.sterilized?.let(::JsonPrimitive) ?: JsonNull)
        string("ownerName", request.ownerName)
        string("ownerEmail", request.ownerEmail)
        put("phones", request.phones?.let { JsonArray(it.map(::JsonPrimitive)) } ?: JsonNull)
        string("address", request.address)
        string("allergies", request.allergies)
        string("treatment", request.treatment)
    }.toString()

    private fun segment(value: String): String = java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20")

    private fun buildPhotoMultipart(boundary: String, imageData: ByteArray): ByteArray =
        ByteArrayOutputStream().use { output ->
            output.write("--$boundary\r\n".toByteArray())
            output.write("Content-Disposition: form-data; name=\"photo\"; filename=\"pet-photo.jpg\"\r\n".toByteArray())
            output.write("Content-Type: image/jpeg\r\n\r\n".toByteArray())
            output.write(imageData)
            output.write("\r\n--$boundary--\r\n".toByteArray())
            output.toByteArray()
        }

    private fun isJpeg(data: ByteArray): Boolean =
        data.size >= 3 && data[0] == 0xFF.toByte() && data[1] == 0xD8.toByte() && data[2] == 0xFF.toByte()

    @Serializable private data class LostModeBody(val lostMode: Boolean)

    private companion object {
        const val MAX_PHOTO_BYTES = 5 * 1024 * 1024
    }
}
