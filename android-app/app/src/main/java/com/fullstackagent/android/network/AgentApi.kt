package com.fullstackagent.android.network

import com.fullstackagent.android.data.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AgentApi(private val settings: SecureSettings, private val client: OkHttpClient = OkHttpClient()) {
    suspend fun complete(messages: List<Message>, memory: String): String = withContext(Dispatchers.IO) {
        val c = settings.load()
        require(c.apiKey.isNotBlank()) { "Add an API key in Settings first." }
        if (c.type == "anthropic") anthropic(c, messages, memory) else openAi(c, messages, memory)
    }
    private fun system(c: ProviderConfig, memory: String) = "You are ${c.agentName}, a private mobile assistant. Be concise, helpful, and honest. Persistent memory:\n$memory"
    private fun openAi(c: ProviderConfig, messages: List<Message>, memory: String): String {
        val items = JSONArray().put(JSONObject().put("role", "system").put("content", system(c, memory)))
        messages.takeLast(30).forEach { items.put(JSONObject().put("role", it.role).put("content", it.content)) }
        val body = JSONObject().put("model", c.model).put("messages", items).toString()
        val req = Request.Builder().url("${c.endpoint}/chat/completions").header("Authorization", "Bearer ${c.apiKey}").post(body.toRequestBody(JSON)).build()
        return execute(req).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
    }
    private fun anthropic(c: ProviderConfig, messages: List<Message>, memory: String): String {
        val items = JSONArray(); messages.takeLast(30).forEach { items.put(JSONObject().put("role", it.role).put("content", it.content)) }
        val body = JSONObject().put("model", c.model).put("max_tokens", 2048).put("system", system(c, memory)).put("messages", items).toString()
        val req = Request.Builder().url("${c.endpoint}/messages").header("x-api-key", c.apiKey).header("anthropic-version", "2023-06-01").post(body.toRequestBody(JSON)).build()
        return execute(req).getJSONArray("content").getJSONObject(0).getString("text")
    }
    private fun execute(request: Request): JSONObject = client.newCall(request).execute().use { response ->
        val text = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw IOException("Provider returned ${response.code}: ${runCatching { JSONObject(text).optJSONObject("error")?.optString("message") }.getOrNull() ?: text.take(240)}")
        JSONObject(text)
    }
    companion object { private val JSON = "application/json; charset=utf-8".toMediaType() }
}
