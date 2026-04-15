package com.example.rssreader

import com.example.rssreader.core.datastore.AppSettingsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BaseUrlValidationTest {
    @Test
    fun normalizeBaseUrl_appendsApiWhenMissing() {
        assertEquals("https://demo.host/api", AppSettingsStore.normalizeBaseUrl("https://demo.host"))
    }

    @Test
    fun normalizeBaseUrl_keepsApiWhenExists() {
        assertEquals("https://demo.host/api", AppSettingsStore.normalizeBaseUrl("https://demo.host/api/"))
    }

    @Test
    fun isValidBaseUrl_acceptsHttpAndHttpsOnly() {
        assertTrue(AppSettingsStore.isValidBaseUrl("https://demo.host"))
        assertTrue(AppSettingsStore.isValidBaseUrl("http://demo.host"))
        assertFalse(AppSettingsStore.isValidBaseUrl("ftp://demo.host"))
    }
}
