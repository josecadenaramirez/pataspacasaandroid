package com.cadena.pataspacasa.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class BackendAuthUserDTO(
    val id: String,
    val email: String,
    val name: String? = null,
    val role: String? = null
)

@Serializable
data class BackendLoginResponseDTO(
    val ok: Boolean,
    val user: BackendAuthUserDTO,
    val accessToken: String,
    val refreshToken: String,
    val refreshExpiresAt: String
)

@Serializable
data class BackendRefreshResponseDTO(
    val ok: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val refreshExpiresAt: String,
    val user: BackendAuthUserDTO
)

@Serializable
data class BackendRegisterResponseDTO(val ok: Boolean, val user: BackendAuthUserDTO)

@Serializable
data class BackendLogoutResponseDTO(val ok: Boolean)

@Serializable
data class BackendErrorDTO(val error: String)
