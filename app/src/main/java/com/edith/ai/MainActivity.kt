package com.edith.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val Bg = Color(0xFF080A0D)
private val Card = Color(0xFF12161B)
private val Muted = Color(0xFFAAB1BA)

class MainActivity : ComponentActivity() {
    private lateinit var state: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        state = AppState(this)
        setContent { EdithApp(state) }
    }
}

@Composable
private fun EdithApp(state: AppState) {
    var booting by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        state.refresh()
        booting = false
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = Card,
            primary = Color.White,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        if (booting) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            MainShell(state)
        }
    }
}

@Composable
private fun MainShell(state: AppState) {
    var selected by remember { mutableIntStateOf(0) }
    val items = listOf(
        Triple("Home", Icons.Default.AutoAwesome, 0),
        Triple("Inbox", Icons.Default.Chat, 1),
        Triple("Instagram", Icons.Default.Link, 2),
        Triple("Knowledge", Icons.Default.Storage, 3)
    )

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Card) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = selected == item.third,
                        onClick = { selected = item.third },
                        icon = { Icon(item.second, item.first) },
                        label = { Text(item.first) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selected) {
                0 -> Dashboard(state)
                1 -> Inbox(state)
                2 -> Instagram(state)
                3 -> Knowledge(state)
            }
        }
    }
}

@Composable
private fun Screen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun Dashboard(state: AppState) {
    val scope = rememberCoroutineScope()
    Screen("E.D.I.T.H.") {
        Text("Instagram AI control center", color = Muted)

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (state.serverOnline) "Backend ONLINE" else "Backend OFFLINE", fontWeight = FontWeight.Bold)
                Text(state.serverMessage.ifBlank { "Checking server..." }, color = Muted)
                OutlinedButton(onClick = { scope.launch { state.refresh() } }) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Refresh")
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Metric("Conversations", state.conversationsCount.toString(), Modifier.weight(1f))
            Metric("AI Replies", state.aiRepliesCount.toString(), Modifier.weight(1f))
        }

        Metric("Automation Rules", state.activeRulesCount.toString(), Modifier.fillMaxWidth())
    }
}

@Composable
private fun Inbox(state: AppState) {
    val scope = rememberCoroutineScope()
    var count by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val result = state.loadConversations()
        if (result.ok) count = result.dataArray.length()
    }

    Screen("Inbox") {
        Metric("Conversations", count.toString(), Modifier.fillMaxWidth())
        Text(
            "Inbox is connected to the conversations table on the existing backend.",
            color = Muted
        )
        OutlinedButton(onClick = { scope.launch { state.loadConversations() } }) {
            Icon(Icons.Default.Refresh, null)
            Spacer(Modifier.width(8.dp))
            Text("Refresh inbox")
        }
    }
}

@Composable
private fun Instagram(state: AppState) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    Screen("Instagram") {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    if (state.instagramConnected) "Connected" else "Not connected",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (state.instagramConnected) "@${state.instagramUsername}" else "No Instagram account is connected on the backend.",
                    color = Muted
                )
            }
        }

        if (state.instagramConnected) {
            Button(
                enabled = !busy,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        busy = true
                        val result = state.disconnectInstagram()
                        status = result.json.optString("message", result.error.orEmpty())
                        busy = false
                    }
                }
            ) {
                Icon(Icons.Default.LinkOff, null)
                Spacer(Modifier.width(8.dp))
                Text(if (busy) "Disconnecting..." else "Disconnect Instagram")
            }
        } else {
            Text(
                "اتصال OAuth واقعی از Backend انجام می‌شود؛ این نسخه دیگر رمز اینستاگرام را داخل اپ دریافت نمی‌کند.",
                color = Muted
            )
        }

        if (status.isNotEmpty()) Text(status, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun Knowledge(state: AppState) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("عمومی") }
    var status by remember { mutableStateOf("") }

    Screen("Knowledge Base") {
        OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth(), label = { Text("Title") }, singleLine = true)
        OutlinedTextField(content, { content = it }, Modifier.fillMaxWidth(), label = { Text("Permanent AI context") })
        OutlinedTextField(category, { category = it }, Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true)

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    val result = state.saveKnowledge(title, content, category)
                    status = result.json.optString("message", result.error.orEmpty())
                    if (result.ok) {
                        title = ""
                        content = ""
                    }
                }
            },
            enabled = title.isNotBlank() && content.isNotBlank()
        ) {
            Text("Save to current database")
        }

        if (status.isNotEmpty()) Text(status, fontWeight = FontWeight.Bold)

        Text(
            "این بخش مستقیماً از جدول knowledge_base فعلی استفاده می‌کند.",
            color = Muted
        )
    }
}

@Composable
private fun Metric(title: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(title, color = Muted)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        }
    }
}
