package com.example.rssreader.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class AppSettings(
    val baseUrl: String = "https://example.com/api",
    val dynamicColorEnabled: Boolean = true,
)

class AppSettingsStore(context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { context.preferencesDataStoreFile("app_settings.preferences_pb") },
    )

    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            AppSettings(
                baseUrl = prefs[KEY_BASE_URL] ?: DEFAULT_BASE_URL,
                dynamicColorEnabled = prefs[KEY_DYNAMIC_COLOR] ?: true,
            )
        }

    suspend fun updateBaseUrl(baseUrl: String) {
        dataStore.edit { prefs ->
            prefs[KEY_BASE_URL] = normalizeBaseUrl(baseUrl)
        }
    }

    suspend fun updateDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://example.com/api"
        val KEY_BASE_URL: Preferences.Key<String> = stringPreferencesKey("base_url")
        val KEY_DYNAMIC_COLOR: Preferences.Key<Boolean> = booleanPreferencesKey("dynamic_color")

        fun normalizeBaseUrl(url: String): String {
            val trimmed = url.trim().trimEnd('/')
            return if (trimmed.endsWith("/api")) trimmed else "$trimmed/api"
        }

        fun isValidBaseUrl(url: String): Boolean {
            return url.startsWith("http://") || url.startsWith("https://")
        }
    }
}
