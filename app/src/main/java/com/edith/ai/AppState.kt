package com.edith.ai

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppState(context: Context) {
    private val store = SecureStore(context.applicationContext)
    private val api = ApiClient(store)

    var authenticated by mutableStateOf(store.sessionToken != null)
        private set
    var userName by mutableStateOf("")
        private set
    var instagramConnected by mutableStateOf(false)
        private set
    var instagramUsername by mutableStateOf("")
        private set

    suspend fun restore(): Boolean = withContext(Dispatchers.IO) {
        if (store.sessionToken == null) {
            authenticated = false
            return@withContext false
        }
        val result = api.me()
        if (!result.ok) {
            store.sessionToken = null
            authenticated = false
            return@withContext false
        }
        userName = result.json.optJSONObject("user")?.optString("name").orEmpty()
        refreshInstagram()
        authenticated = true
        true
    }

    suspend fun login(email: String, password: String): ApiResult = withContext(Dispatchers.IO) {
        val result = api.login(email, password)
        if (result.ok) {
            store.sessionToken = result.json.optString("token")
            userName = result.json.optJSONObject("user")?.optString("name").orEmpty()
            authenticated = true
            refreshInstagram()
        }
        result
    }

    suspend fun register(name: String, email: String, password: String): ApiResult = withContext(Dispatchers.IO) {
        val result = api.register(name, email, password)
        if (result.ok) {
            store.sessionToken = result.json.optString("token")
            userName = result.json.optJSONObject("user")?.optString("name").orEmpty()
            authenticated = true
            refreshInstagram()
        }
        result
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        api.logout()
        store.sessionToken = null
        authenticated = false
        userName = ""
        instagramConnected = false
        instagramUsername = ""
    }

    suspend fun refreshInstagram() = withContext(Dispatchers.IO) {
        val result = api.instagramStatus()
        if (result.ok) {
            val accounts = result.json.optJSONArray("accounts")
            instagramConnected = accounts != null && accounts.length() > 0
            instagramUsername = if (instagramConnected) accounts!!.optJSONObject(0)?.optString("username").orEmpty() else ""
        }
    }

    suspend fun instagramAuthIntent(): Intent? = withContext(Dispatchers.IO) {
        val result = api.connectInstagram()
        if (!result.ok) return@withContext null
        Intent(Intent.ACTION_VIEW, Uri.parse(result.json.optString("auth_url")))
    }

    suspend fun loadAi() = withContext(Dispatchers.IO) { api.getAiSettings() }

    suspend fun saveAi(key: String, responseModel: String, analysisModel: String, prompt: String, enabled: Boolean) =
        withContext(Dispatchers.IO) { api.saveAiSettings(key, responseModel, analysisModel, prompt, enabled) }

    suspend fun testAi() = withContext(Dispatchers.IO) { api.testAi() }
}
