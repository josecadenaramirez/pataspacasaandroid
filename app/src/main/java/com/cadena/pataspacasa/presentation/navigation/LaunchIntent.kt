package com.cadena.pataspacasa.presentation.navigation

import android.content.Intent
import com.cadena.pataspacasa.presentation.pet.TagCodeParser

sealed interface LaunchIntent {
    data class LinkTag(val tagCode: String) : LaunchIntent
    data class LostPetScan(val eventId: String, val petId: String) : LaunchIntent
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

    return null
}
