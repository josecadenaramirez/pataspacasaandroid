package com.cadena.pataspacasa.domain.repository

import com.cadena.pataspacasa.domain.entity.NFCTag

interface TagRepository {
    suspend fun getTag(tagCode: String): NFCTag
    suspend fun linkTagToPet(tagCode: String, petId: String): NFCTag
    suspend fun validateTag(tagCode: String): NFCTag
}
