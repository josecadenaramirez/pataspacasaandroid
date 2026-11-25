package com.cadena.pataspacasa.data.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.cadena.pataspacasa.core.config.AppConfiguration
import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.domain.entity.PlanType
import com.cadena.pataspacasa.domain.entity.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.security.KeyStore
import java.util.Date
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureSessionStore @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences(
        "pataspa_${AppConfiguration.environment.lowercase()}_session",
        Context.MODE_PRIVATE
    )
    private val keyAlias = "pataspa_${AppConfiguration.environment.lowercase()}_refresh_key"
    private val json = Json { ignoreUnknownKeys = true }

    fun saveRefreshToken(value: String) {
        try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            prefs.edit()
                .putString(KEY_REFRESH, Base64.encodeToString(cipher.doFinal(value.toByteArray()), Base64.NO_WRAP))
                .putString(KEY_IV, Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
                .apply()
        } catch (_: Exception) {
            throw AppError.SecureStorageFailed
        }
    }

    fun loadRefreshToken(): String? = try {
        val encrypted = prefs.getString(KEY_REFRESH, null) ?: return null
        val iv = prefs.getString(KEY_IV, null) ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP))
        )
        String(cipher.doFinal(Base64.decode(encrypted, Base64.NO_WRAP)))
    } catch (_: Exception) {
        clear()
        null
    }

    fun saveUser(user: User) {
        val value = PersistedUser(
            id = user.id,
            name = user.name,
            email = user.email,
            photoUrl = user.photoUrl,
            role = user.role,
            plan = user.plan.value,
            createdAt = user.createdAt.time,
            isEmailVerified = user.isEmailVerified
        )
        prefs.edit().putString(KEY_USER, json.encodeToString(PersistedUser.serializer(), value)).apply()
    }

    fun loadUser(): User? = try {
        val raw = prefs.getString(KEY_USER, null) ?: return null
        val user = json.decodeFromString(PersistedUser.serializer(), raw)
        User(
            id = user.id,
            name = user.name,
            email = user.email,
            photoUrl = user.photoUrl,
            role = user.role,
            plan = PlanType.from(user.plan),
            createdAt = Date(user.createdAt),
            isEmailVerified = user.isEmailVerified
        )
    } catch (_: Exception) {
        prefs.edit().remove(KEY_USER).apply()
        null
    }

    fun clearUser() {
        prefs.edit().remove(KEY_USER).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_REFRESH).remove(KEY_IV).remove(KEY_USER).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(keyAlias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            generateKey()
        }
    }

    @Serializable
    private data class PersistedUser(
        val id: String,
        val name: String,
        val email: String,
        val photoUrl: String?,
        val role: String?,
        val plan: String,
        val createdAt: Long,
        val isEmailVerified: Boolean
    )

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_REFRESH = "refresh_token"
        const val KEY_IV = "refresh_token_iv"
        const val KEY_USER = "authenticated_user"
    }
}
