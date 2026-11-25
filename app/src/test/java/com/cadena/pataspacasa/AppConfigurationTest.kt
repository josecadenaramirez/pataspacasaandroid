package com.cadena.pataspacasa

import com.cadena.pataspacasa.core.config.AppConfiguration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppConfigurationTest {
    @Test
    fun `variant resolves the expected versioned API URL`() {
        val expected = when (BuildConfig.APP_ENVIRONMENT) {
            "QA" -> "https://qa.pataspacasa.com/api/v1"
            "PROD" -> "https://pataspacasa.com/api/v1"
            else -> error("Unknown environment ${BuildConfig.APP_ENVIRONMENT}")
        }

        assertEquals(expected, AppConfiguration.apiBaseUrl)
        assertEquals(1, Regex("/api/v1").findAll(AppConfiguration.apiBaseUrl).count())
        assertFalse(AppConfiguration.apiBaseUrl.contains("/api/api/"))
    }

    @Test
    fun `public pet example follows the selected environment`() {
        assertEquals(
            "${BuildConfig.PUBLIC_WEB_BASE_URL}/p/aBc123",
            AppConfiguration.examplePetUrl()
        )
    }
}
