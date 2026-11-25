package com.cadena.pataspacasa.data.repository

import com.cadena.pataspacasa.core.error.AppError
import com.cadena.pataspacasa.domain.entity.NFCTag
import com.cadena.pataspacasa.domain.entity.TagActivationStatus
import com.cadena.pataspacasa.domain.repository.TagRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnavailableLiveTagRepository @Inject constructor() : TagRepository {
    override suspend fun getTag(tagCode: String): NFCTag {
        throw AppError.PermissionDenied
    }

    override suspend fun linkTagToPet(tagCode: String, petId: String): NFCTag {
        throw AppError.PermissionDenied
    }

    override suspend fun validateTag(tagCode: String): NFCTag {
        throw AppError.PermissionDenied
    }
}
