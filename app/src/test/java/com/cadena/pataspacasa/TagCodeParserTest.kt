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

    @Test
    fun `accepts claim universal link`() {
        assertEquals("AbC123", TagCodeParser.parse("https://pataspacasa.com/claim/AbC123"))
    }

    @Test
    fun `accepts exact custom scheme`() {
        assertEquals("AbC123", TagCodeParser.parse("pataspacasa://tag/AbC123"))
    }

    @Test
    fun `rejects query fragment and trailing slash`() {
        assertNull(TagCodeParser.parse("https://qa.pataspacasa.com/p/AbC123?x=1"))
        assertNull(TagCodeParser.parse("https://qa.pataspacasa.com/p/AbC123#x"))
        assertNull(TagCodeParser.parse("https://qa.pataspacasa.com/p/AbC123/"))
    }
}
