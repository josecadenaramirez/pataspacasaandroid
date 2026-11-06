package com.cadena.pataspacasa.domain.entity

import java.util.Calendar
import java.util.Date

/**
 * Represents a date in YYYY-MM-DD format without timezone info.
 * Used for pet birthdate to avoid timezone conversion issues.
 * Equivalent to Swift's PetCalendarDate.
 */
data class PetCalendarDate(
    val year: Int,
    val month: Int,
    val day: Int
) {
    companion object {
        /**
         * Parse from API format "YYYY-MM-DD".
         */
        fun fromApiValue(apiValue: String): PetCalendarDate? {
            val regex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
            if (!regex.matches(apiValue)) return null

            val parts = apiValue.split("-")
            if (parts.size != 3) return null

            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null

            if (month !in 1..12 || day !in 1..31) return null

            // Validate date is actually valid
            val cal = Calendar.getInstance()
            cal.set(year, month - 1, day) // Month is 0-indexed in Calendar
            val validated = cal.get(Calendar.YEAR) == year &&
                    cal.get(Calendar.MONTH) == month - 1 &&
                    cal.get(Calendar.DAY_OF_MONTH) == day

            if (!validated) return null

            return PetCalendarDate(year, month, day)
        }

        /**
         * Create from a Date object.
         */
        fun fromDate(date: Date): PetCalendarDate? {
            val cal = Calendar.getInstance()
            cal.time = date
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1 // Convert to 1-indexed
            val day = cal.get(Calendar.DAY_OF_MONTH)

            return PetCalendarDate(year, month, day)
        }
    }

    /**
     * Convert to API format "YYYY-MM-DD".
     */
    fun toApiValue(): String = String.format("%04d-%02d-%02d", year, month, day)

    /**
     * Convert to Date for UI pickers (set to noon UTC).
     */
    fun toDate(): Date? {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 12, 0, 0) // Month is 0-indexed, noon UTC
        return try {
            cal.time
        } catch (e: Exception) {
            null
        }
    }

    override fun toString(): String = toApiValue()
}
