package com.edith.ai

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class ApiResult(
    val ok: Boolean,
    val json: JSONObject = JSONObject(),
    val error: String? = null
) {
    val data: JSONObject get() = json.optJSONObject("data") ?: JSONObject()
    val dataArray: JSONArray get() = json.optJSONArray("data") ?: JSONArray()
}

class ApiClient {
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()
    private val base = BuildConfig.API_BASE_URL.trimEnd('/') + "/"

    private fun request(path: String, method: String = "GET", body: JSONObject? = null): ApiResult {
        val builder = Request.Builder().url(base + path).addHeader("Accept", "application/json")
        if (body != null) {
            builder.addHeader("Content-Type", "application/json")
            builder.method(method, body.toString().toRequestBody(jsonType))
        } else builder.method(method, null)

        return try {
            client.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
                val ok = response.isSuccessful && json.optString("status") == "success"
                ApiResult(ok, json, if (ok) null else json.optString("message", "HTTP ${
                    response.code
                }"))
            }
        } catch (e: Exception) {
            ApiResult(false, error = e.message ?: "Network error")
        }
    }

    fun serverHealth() = request("test-connection.php")
    fun stats() = request("stats.php")
    fun instagramStatus() = request("instagram/status.php")
    fun disconnectInstagram() = request("instagram/disconnect.php", "POST")
    fun conversations() = request("conversations.php")
    fun saveKnowledge(title: String, content: String, category: String) =
        request("knowledge.php", "POST", JSONObject().put("title", title).put("content", content).put("category", category))
}
