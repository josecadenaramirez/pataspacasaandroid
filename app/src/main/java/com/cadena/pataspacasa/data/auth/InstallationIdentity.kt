package com.cadena.pataspacasa.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstallationIdentity @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences("pataspa_installation", Context.MODE_PRIVATE)
    val value: String
        get() = prefs.getString(KEY, null) ?: UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY, it).apply()
        }

    private companion object { const val KEY = "installation_id" }
}
