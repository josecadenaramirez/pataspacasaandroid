package com.cadena.pataspacasa.presentation.navigation

import android.content.Intent
import com.cadena.pataspacasa.presentation.pet.TagCodeParser

sealed interface LaunchIntent {
    data class LinkTag(val tagCode: String) : LaunchIntent
    data class LostPetScan(val eventId: String, val petId: String) : LaunchIntent
    data class PetDetail(val petId: String) : LaunchIntent
    object Profile : LaunchIntent
}

fun parseLaunchIntent(intent: Intent?): LaunchIntent? {
    if (intent == null) return null

    val type = intent.getStringExtra("type")
    val eventId = intent.getStringExtra("eventId")?.trim().orEmpty()
    val petId = intent.getStringExtra("petId")?.trim().orEmpty()
    if ((type == "lostPetScan" || (eventId.isNotEmpty() && petId.isNotEmpty())) &&
        eventId.isNotEmpty() && petId.isNotEmpty()
    ) {
        return LaunchIntent.LostPetScan(eventId = eventId, petId = petId)
    }

    val fromUri = intent.data?.toString()?.let(TagCodeParser::parse)
    if (!fromUri.isNullOrEmpty()) return LaunchIntent.LinkTag(fromUri)

    val segments = intent.data?.pathSegments.orEmpty()
    if (segments.size == 2 && segments[0] == "pet" && segments[1].isNotBlank()) {
        return LaunchIntent.PetDetail(segments[1])
    }
    if (segments.size == 1 && segments[0] == "profile") return LaunchIntent.Profile

    return null
}
