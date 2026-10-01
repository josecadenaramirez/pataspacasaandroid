package com.cadena.pataspacasa.domain.entity

enum class PetStatus(val value: String) {
    NORMAL("normal"),
    LOST("lost");

    val displayName: String
        get() = when (this) {
            NORMAL -> "En casa"
            LOST -> "Perdido"
        }

    val isLost: Boolean get() = this == LOST

    companion object {
        fun from(raw: String?): PetStatus = values().find { it.value == raw } ?: NORMAL
    }
}
