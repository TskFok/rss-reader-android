package com.example.rssreader.core.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.rssreader.core.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

fun interface TokenProvider {
    fun getToken(): String?
}

class SessionStore(context: Context) : TokenProvider {
    private val prefs: SharedPreferences
    private val userAdapter = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(User::class.java)

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        prefs = EncryptedSharedPreferences.create(
            context,
            PREF_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun saveSession(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER, userAdapter.toJson(user))
            .apply()
    }

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUser(): User? = prefs.getString(KEY_USER, null)?.let(userAdapter::fromJson)

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREF_FILE = "secure_session"
        private const val KEY_TOKEN = "token"
        private const val KEY_USER = "user"
    }
}
