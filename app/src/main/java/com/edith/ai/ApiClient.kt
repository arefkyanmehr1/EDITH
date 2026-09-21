package com.edith.ai

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

data class ApiResult(val ok: Boolean, val json: JSONObject, val error: String? = null)

class ApiClient(private val store: SecureStore) {
    private val client = OkHttpClient()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private fun request(path: String, method: String, body: JSONObject? = null): ApiResult {
        val builder = Request.Builder()
            .url(BuildConfig.API_BASE_URL + path)
            .addHeader("Accept", "application/json")

        store.sessionToken?.let { builder.addHeader("Authorization", "Bearer $it") }

        if (body != null) {
            builder.addHeader("Content-Type", "application/json")
            builder.method(method, body.toString().toRequestBody(jsonType))
        } else {
            builder.method(method, null)
        }

        return try {
            client.newCall(builder.build()).execute().use { response ->
                val text = response.body?.string().orEmpty()
                val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
                ApiResult(
                    response.isSuccessful && json.optBoolean("ok", response.isSuccessful),
                    json,
                    if (response.isSuccessful) null else json.optString("error", "HTTP ${response.code}")
                )
            }
        } catch (e: Exception) {
            ApiResult(false, JSONObject(), e.message ?: "Network error")
        }
    }

    fun login(email: String, password: String) =
        request("auth/login.php", "POST", JSONObject().put("email", email).put("password", password))

    fun register(name: String, email: String, password: String) =
        request("auth/register.php", "POST", JSONObject().put("name", name).put("email", email).put("password", password))

    fun me() = request("auth/me.php", "GET")
    fun logout() = request("auth/logout.php", "POST")
    fun connectInstagram() = request("instagram/connect.php", "GET")
    fun instagramStatus() = request("instagram/status.php", "GET")
    fun disconnectInstagram(accountId: Long) =
        request("instagram/disconnect.php", "POST", JSONObject().put("account_id", accountId))

    fun getAiSettings() = request("ai/settings.php", "GET")

    fun saveAiSettings(apiKey: String, responseModel: String, analysisModel: String, prompt: String, enabled: Boolean) =
        request("ai/settings.php", "POST", JSONObject()
            .put("gemini_api_key", apiKey)
            .put("response_model", responseModel)
            .put("analysis_model", analysisModel)
            .put("system_prompt", prompt)
            .put("temperature", 0.4)
            .put("max_output_tokens", 512)
            .put("ai_enabled", enabled))

    fun testAi() = request("ai/test.php", "POST")
}
