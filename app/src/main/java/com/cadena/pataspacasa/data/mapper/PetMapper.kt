package com.cadena.pataspacasa.data.mapper

import com.cadena.pataspacasa.data.dto.PetDTO
import com.cadena.pataspacasa.domain.entity.Pet
import com.cadena.pataspacasa.domain.entity.PetCalendarDate
import com.cadena.pataspacasa.domain.entity.PetSpecies
import com.cadena.pataspacasa.domain.entity.PetStatus
import com.cadena.pataspacasa.domain.entity.PlanType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object PetMapper {

    fun toDomain(dto: PetDTO): Pet = Pet(
        id = dto.id,
        publicId = dto.publicId,
        ownerId = dto.ownerId ?: "",
        name = dto.name,
        species = PetSpecies.from(dto.species),
        breed = dto.breed ?: "",
        petType = dto.petType ?: dto.species ?: "dog",
        customPetType = dto.customPetType,
        customBreed = dto.customBreed,
        approxBirthDate = dto.approxBirthDate?.let { PetCalendarDate.fromApiValue(it) },
        sex = dto.sex,
        sterilized = dto.sterilized,
        age = dto.age,
        photoUrl = dto.photoUrl,
        notes = dto.notes,
        allergies = dto.allergies,
        treatment = dto.treatment,
        ownerName = dto.ownerName,
        ownerEmail = dto.ownerEmail,
        phones = dto.phones ?: emptyList(),
        address = dto.address,
        status = statusFrom(dto.status, dto.lostMode),
        lostMode = dto.lostMode ?: false,
        tagId = dto.tagId,
        activationStatus = dto.activationStatus,
        nfcProgrammed = dto.nfcProgrammed,
        nfcProgrammedAt = dto.nfcProgrammedAt?.let { parseIso8601(it) },
        plan = PlanType.from(dto.plan),
        isPremiumActive = dto.isPremiumActive ?: false,
        paidUntil = dto.paidUntil?.let { parseCalendarDate(it) },
        createdAt = dto.createdAt?.let { parseIso8601(it) } ?: Date(),
        updatedAt = dto.updatedAt?.let { parseIso8601(it) } ?: Date()
    )

    fun toDomain(dtos: List<PetDTO>): List<Pet> = dtos.map { toDomain(it) }

    private fun statusFrom(raw: String?, lostMode: Boolean?): PetStatus {
        if (lostMode == true) return PetStatus.LOST
        return PetStatus.from(raw)
    }

    fun parseIso8601(iso: String): Date? {
        val formatsWithMs = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (fmt in formatsWithMs) {
            try {
                return SimpleDateFormat(fmt, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }.parse(iso)
            } catch (_: Exception) {}
        }
        return null
    }

    private fun parseCalendarDate(dateStr: String): Date? {
        if (!dateStr.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return null
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }.parse(dateStr)
        } catch (_: Exception) { null }
    }
}
