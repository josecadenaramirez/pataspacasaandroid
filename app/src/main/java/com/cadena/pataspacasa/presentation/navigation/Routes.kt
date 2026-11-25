package com.cadena.pataspacasa.presentation.navigation

import kotlinx.serialization.Serializable

// Auth routes
@Serializable object SplashRoute
@Serializable object LoginRoute
@Serializable object RegisterRoute
@Serializable data class ForgotPasswordRoute(val email: String? = null)

// Authenticated routes
@Serializable object HomeRoute
@Serializable data class PetDetailRoute(val petId: String)
@Serializable data class EditPetRoute(val petId: String)
@Serializable object AddPetRoute
@Serializable data class LinkTagRoute(val tagCode: String)
@Serializable object ProfileRoute
@Serializable data class LostModeRoute(val petId: String)
@Serializable data class LostPetScanDetailRoute(val eventId: String, val petId: String)
@Serializable data class LostPetScanHistoryRoute(val petId: String, val petName: String)
