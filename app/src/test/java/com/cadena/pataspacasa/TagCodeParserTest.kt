package com.cadena.pataspacasa

import com.cadena.pataspacasa.presentation.pet.TagCodeParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TagCodeParserTest {

    @Test
    fun `accepts raw tag preserving case`() {
        assertEquals("aBc123", TagCodeParser.parse("aBc123"))
    }

    @Test
    fun `accepts valid qa url`() {
        assertEquals("aBc123", TagCodeParser.parse("https://qa.pataspacasa.com/p/aBc123"))
    }

    @Test
    fun `rejects unsupported host`() {
        assertNull(TagCodeParser.parse("https://evil.com/p/aBc123"))
    }

    @Test
    fun `rejects unsupported route`() {
        assertNull(TagCodeParser.parse("https://pataspacasa.com/scan/aBc123"))
    }
}
