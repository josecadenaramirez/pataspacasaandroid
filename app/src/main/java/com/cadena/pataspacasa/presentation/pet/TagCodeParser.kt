package com.cadena.pataspacasa.presentation.pet

import java.net.URI

object TagCodeParser {
    private val validTagRegex = Regex("^[A-Za-z0-9]{4,12}$")
    private val allowedHosts = setOf(
        "qa.pataspacasa.com",
        "pataspacasa.com",
        "www.pataspacasa.com",
        "app.pataspacasa.com"
    )

    fun parse(input: String): String? {
        val value = input.trim()
        if (value.isEmpty()) return null
        if (validTagRegex.matches(value)) return value

        val uri = runCatching { URI(value) }.getOrNull() ?: return null
        if (uri.scheme?.lowercase() != "https") return null
        val host = uri.host?.lowercase() ?: return null
        if (host !in allowedHosts) return null
        if (uri.userInfo != null || uri.port != -1 || uri.query != null || uri.fragment != null) return null

        val path = uri.path ?: return null
        val segments = path.split('/').filter { it.isNotEmpty() }
        if (segments.size != 2) return null
        if (segments[0] != "p") return null
        if (path.endsWith("/")) return null
        val code = segments[1]
        return if (validTagRegex.matches(code)) code else null
    }
}
