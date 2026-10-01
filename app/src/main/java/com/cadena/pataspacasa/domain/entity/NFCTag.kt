package com.cadena.pataspacasa.domain.entity

import java.util.Date

enum class TagActivationStatus(val value: String) {
    UNASSIGNED("unassigned"),
    ACTIVATED("activated"),
    DISABLED("disabled");

    val displayName: String
        get() = when (this) {
            UNASSIGNED -> "Sin asignar"
            ACTIVATED -> "Activa"
            DISABLED -> "Desactivada"
        }

    companion object {
        fun from(raw: String?): TagActivationStatus = values().find { it.value == raw } ?: UNASSIGNED
    }
}

data class NFCTag(
    val id: String,
    val publicCode: String,
    val activationStatus: TagActivationStatus,
    val linkedPetId: String?,
    val linkedUserId: String?,
    val activatedAt: Date?
) {
    val isLinked: Boolean get() = linkedPetId != null
    val isActive: Boolean get() = activationStatus == TagActivationStatus.ACTIVATED
}
