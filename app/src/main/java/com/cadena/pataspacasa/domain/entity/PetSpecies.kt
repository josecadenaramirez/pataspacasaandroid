package com.cadena.pataspacasa.domain.entity

enum class PetSpecies(val value: String) {
    DOG("dog"),
    CAT("cat"),
    OTHER("other");

    val displayName: String
        get() = when (this) {
            DOG -> "Perro"
            CAT -> "Gato"
            OTHER -> "Otro"
        }

    companion object {
        fun from(raw: String?): PetSpecies = values().find { it.value == raw } ?: OTHER
    }
}
