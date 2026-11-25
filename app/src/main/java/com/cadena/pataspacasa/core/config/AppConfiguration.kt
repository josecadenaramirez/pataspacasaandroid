package com.cadena.pataspacasa.core.config

import com.cadena.pataspacasa.BuildConfig

object AppConfiguration {
    val environment: String = BuildConfig.APP_ENVIRONMENT
    val apiBaseUrl: String = BuildConfig.API_BASE_URL.trimEnd('/') + "/" +
        BuildConfig.API_VERSION.trim('/')
    val publicWebBaseUrl: String = BuildConfig.PUBLIC_WEB_BASE_URL.trimEnd('/')
    val firebaseEnabled: Boolean = BuildConfig.FIREBASE_ENABLED

    fun examplePetUrl(code: String = "aBc123"): String = "$publicWebBaseUrl/p/$code"
}
