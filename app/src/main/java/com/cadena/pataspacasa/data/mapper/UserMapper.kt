package com.cadena.pataspacasa.data.mapper

import com.cadena.pataspacasa.domain.entity.PlanType
import com.cadena.pataspacasa.domain.entity.User
import com.google.firebase.auth.FirebaseUser
import java.util.Date

object UserMapper {
    fun toDomain(firebaseUser: FirebaseUser): User = User(
        id = firebaseUser.uid,
        name = firebaseUser.displayName ?: "",
        email = firebaseUser.email ?: "",
        photoUrl = firebaseUser.photoUrl?.toString(),
        plan = PlanType.BASIC,
        createdAt = firebaseUser.metadata?.creationTimestamp
            ?.let { Date(it) } ?: Date(),
        isEmailVerified = firebaseUser.isEmailVerified
    )
}
