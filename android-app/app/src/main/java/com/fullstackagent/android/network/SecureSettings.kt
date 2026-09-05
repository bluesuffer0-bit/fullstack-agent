package com.fullstackagent.android.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

data class ProviderConfig(val type: String, val endpoint: String, val model: String, val apiKey: String, val agentName: String)

class SecureSettings(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(context, "secure_settings", MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(), EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    fun load() = ProviderConfig(prefs.getString("type", "openai")!!, prefs.getString("endpoint", "https://api.openai.com/v1")!!, prefs.getString("model", "gpt-4o-mini")!!, prefs.getString("key", "")!!, prefs.getString("name", "Jarvis")!!)
    fun save(c: ProviderConfig) = prefs.edit().putString("type", c.type).putString("endpoint", c.endpoint.trimEnd('/')).putString("model", c.model).putString("key", c.apiKey).putString("name", c.agentName).apply()
}
