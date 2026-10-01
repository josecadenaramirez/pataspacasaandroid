package com.cadena.pataspacasa.data.datasource

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.core.error.AppErrorMapper
import com.cadena.pataspacasa.data.dto.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudFunctionDataSource @Inject constructor(
    private val authDataSource: FirebaseAuthDataSource
) {
    private val functions: FirebaseFunctions = Firebase.functions("us-central1")
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend fun getMyPets(): List<PetDTO> {
        val token = authDataSource.getIdToken()
        val payload = mapOf("token" to token)
        val result = functions.getHttpsCallable("getMyPets").call(payload).await()
        val jsonStr = Json.encodeToString(result.data as? Map<*, *> ?: emptyMap<String, Any>())
        return try {
            val resp = json.decodeFromString<GetMyPetsResponseDTO>(jsonStr)
            resp.pets
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun claimPetByTag(request: ClaimPetRequestDTO): ClaimPetResponseDTO {
        val token = authDataSource.getIdToken()
        val payload = mutableMapOf<String, Any>(
            "tagId" to request.tagId,
            "token" to token
        )
        return callFunction("claimPet", payload)
    }

    suspend fun createPet(request: CreatePetRequestDTO): PetDTO {
        val token = authDataSource.getIdToken()
        val payload = buildMap<String, Any?> {
            put("name", request.name)
            put("species", request.species)
            put("breed", request.breed)
            request.age?.let { put("age", it) }
            request.notes?.let { put("notes", it) }
            request.allergies?.let { put("allergies", it) }
            request.treatment?.let { put("treatment", it) }
            request.phones?.let { put("phones", it) }
            put("token", token)
        }.filterValues { it != null }.mapValues { it.value!! }
        return callFunction("claimPet", payload)
    }

    suspend fun updateOwnPet(request: UpdatePetRequestDTO): UpdateOwnPetResponseDTO {
        val token = authDataSource.getIdToken()
        val payload = buildMap<String, Any?> {
            put("petId", request.petId)
            put("name", request.name)
            request.breed?.let { put("breed", it) }
            request.age?.let { put("age", it) }
            request.notes?.let { put("notes", it) }
            request.allergies?.let { put("allergies", it) }
            request.treatment?.let { put("treatment", it) }
            request.ownerName?.let { put("ownerName", it) }
            request.phones?.let { put("phones", it) }
            put("token", token)
        }.filterValues { it != null }.mapValues { it.value!! }
        return callFunction("updateOwnPet", payload)
    }

    suspend fun updateOwnPetPhoto(request: UpdatePetPhotoRequestDTO): UpdatePetPhotoResponseDTO {
        val token = authDataSource.getIdToken()
        val payload = mapOf(
            "petId" to request.petId,
            "photoBase64" to request.imageBase64,
            "photoContentType" to request.mimeType,
            "token" to token
        )
        return callFunction("updateOwnPetPhoto", payload)
    }

    suspend fun setOwnPetLostMode(request: SetLostModeRequestDTO): SetLostModeResponseDTO {
        val token = authDataSource.getIdToken()
        val payload = mapOf(
            "petId" to request.petId,
            "enable" to request.enable,
            "token" to token
        )
        return callFunction("setOwnPetLostMode", payload)
    }

    suspend fun verifySubscription(request: VerifySubscriptionRequestDTO): VerifySubscriptionResponseDTO {
        val token = authDataSource.getIdToken()
        val payload = mapOf(
            "productId" to request.productId,
            "purchaseToken" to request.purchaseToken,
            "platform" to request.platform,
            "token" to token
        )
        return callFunction("verifySubscription", payload)
    }

    suspend fun getLostPetScans(request: GetLostPetScansRequestDTO): GetLostPetScansResponseDTO {
        val token = authDataSource.getIdToken()
        val payload = mapOf(
            "petId" to request.petId,
            "token" to token
        )
        return callFunction("getLostPetScans", payload)
    }

    private suspend inline fun <reified T> callFunction(name: String, payload: Map<String, Any>): T {
        return try {
            val result = functions.getHttpsCallable(name).call(payload).await()
            val dataStr = kotlinx.serialization.json.Json.encodeToString(
                result.data as? Map<*, *> ?: emptyMap<String, Any>()
            )
            json.decodeFromString(dataStr)
        } catch (e: Exception) {
            throw AppErrorMapper.from(e)
        }
    }
}
