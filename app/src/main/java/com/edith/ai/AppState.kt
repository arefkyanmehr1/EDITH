package com.edith.ai

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppState(context: Context) {
    private val api = ApiClient()

    var serverOnline by mutableStateOf(false)
        private set
    var serverMessage by mutableStateOf("")
        private set
    var instagramConnected by mutableStateOf(false)
        private set
    var instagramUsername by mutableStateOf("")
        private set
    var conversationsCount by mutableStateOf(0)
        private set
    var aiRepliesCount by mutableStateOf(0)
        private set
    var activeRulesCount by mutableStateOf(0)
        private set

    suspend fun refresh(): ApiResult = withContext(Dispatchers.IO) {
        val health = api.serverHealth()
        serverOnline = health.ok
        serverMessage = health.json.optString("message", health.error.orEmpty())

        val status = api.instagramStatus()
        if (status.ok) {
            val data = status.data
            instagramConnected = data.optBoolean("connected", false)
            instagramUsername = data.optJSONObject("account")?.optString("username").orEmpty()
        } else {
            instagramConnected = false
            instagramUsername = ""
        }

        val stats = api.stats()
        if (stats.ok) {
            val data = stats.data
            conversationsCount = data.optInt("total_conversations", 0)
            aiRepliesCount = data.optInt("ai_replied_count", 0)
            activeRulesCount = data.optInt("active_rules_count", 0)
        }
        health
    }

    suspend fun disconnectInstagram(): ApiResult = withContext(Dispatchers.IO) {
        val result = api.disconnectInstagram()
        if (result.ok) {
            instagramConnected = false
            instagramUsername = ""
        }
        result
    }

    suspend fun saveKnowledge(title: String, content: String, category: String) =
        withContext(Dispatchers.IO) { api.saveKnowledge(title, content, category) }

    suspend fun loadConversations() = withContext(Dispatchers.IO) { api.conversations() }
}
