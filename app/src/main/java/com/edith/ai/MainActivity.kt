package com.edith.ai

import android.content.Intent
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
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private val Bg = Color(0xFF0B0D10)
private val Card = Color(0xFF14171C)
private val Accent = Color(0xFFE9EDF2)

class MainActivity : ComponentActivity() {
    private lateinit var appState: AppState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appState = AppState(this)
        setContent { EdithApp(appState) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (::appState.isInitialized) {
            kotlinx.coroutines.MainScope().launch { appState.refreshInstagram() }
        }
    }
}

@Composable
fun EdithApp(state: AppState) {
    val scope = rememberCoroutineScope()
    var booting by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        state.restore()
        booting = false
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Bg,
            surface = Card,
            primary = Accent,
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    ) {
        if (booting) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (!state.authenticated) {
            AuthScreen(
                onLogin = { email, password -> state.login(email, password) },
                onRegister = { name, email, password -> state.register(name, email, password) }
            )
        } else {
            MainShell(state, onLogout = { scope.launch { state.logout() } })
        }
    }
}

@Composable
private fun AuthScreen(
    onLogin: suspend (String, String) -> ApiResult,
    onRegister: suspend (String, String, String) -> ApiResult
) {
    val scope = rememberCoroutineScope()
    var registerMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center
    ) {
        Text("E.D.I.T.H.", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text("Instagram AI management system", color = Color.LightGray)
        Spacer(Modifier.height(28.dp))

        if (registerMode) {
            OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Name") }, singleLine = true)
            Spacer(Modifier.height(10.dp))
        }

        OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true)
        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
            password, { password = it }, Modifier.fillMaxWidth(),
            label = { Text("Password") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    loading = true
                    val result = if (registerMode) onRegister(name, email, password)
                    else onLogin(email, password)
                    loading = false
                    status = if (result.ok) "" else result.error ?: result.json.optString("error", "Request failed")
                }
            }
        ) {
            Text(if (loading) "Please wait…" else if (registerMode) "Create account" else "Login")
        }

        TextButton(onClick = { registerMode = !registerMode }) {
            Text(if (registerMode) "Already have an account? Login" else "Create a new account")
        }

        if (status.isNotEmpty()) Text(status, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun MainShell(state: AppState, onLogout: () -> Unit) {
    var selected by remember { mutableIntStateOf(0) }
    val items = listOf(
        Triple("Dashboard", Icons.Default.AutoAwesome, "dashboard"),
        Triple("Inbox", Icons.Default.Chat, "inbox"),
        Triple("Instagram", Icons.Default.Link, "instagram"),
        Triple("AI Brain", Icons.Default.AutoAwesome, "brain")
    )

    Scaffold(
        containerColor = Bg,
        bottomBar = {
            NavigationBar(containerColor = Card) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selected == index,
                        onClick = { selected = index },
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (selected) {
                0 -> DashboardScreen(state, onLogout)
                1 -> InboxScreen()
                2 -> InstagramScreen(state)
                3 -> AIBrainScreen(state)
            }
        }
    }
}

@Composable
private fun Screen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun DashboardScreen(state: AppState, onLogout: () -> Unit) {
    Screen("E.D.I.T.H.") {
        Text("AI Instagram manager", color = Color.LightGray)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("DMs", "0", Modifier.weight(1f))
            MetricCard("Comments", "0", Modifier.weight(1f))
        }
        MetricCard(
            "Instagram",
            if (state.instagramConnected) "@" + state.instagramUsername else "Not connected",
            Modifier.fillMaxWidth()
        )
        OutlinedButton(onClick = onLogout) {
            Icon(Icons.Default.Logout, null)
            Spacer(Modifier.width(8.dp))
            Text("Logout")
        }
    }
}

@Composable
private fun InboxScreen() {
    Screen("Inbox") {
        MetricCard("Conversations", "0", Modifier.fillMaxWidth())
        Text("Messages will appear here after the Instagram webhook is connected.", color = Color.LightGray)
    }
}

@Composable
private fun InstagramScreen(state: AppState) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    Screen("Instagram") {
        MetricCard(
            "Connection",
            if (state.instagramConnected) "Connected: @" + state.instagramUsername else "Not connected",
            Modifier.fillMaxWidth()
        )

        Button(
            enabled = !loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    loading = true
                    val intent = state.instagramAuthIntent()
                    if (intent != null) context.startActivity(intent)
                    else status = "Could not start Instagram authorization."
                    loading = false
                }
            }
        ) {
            Text(if (loading) "Preparing…" else if (state.instagramConnected) "Reconnect Instagram" else "Connect Instagram")
        }

        if (status.isNotEmpty()) Text(status, color = MaterialTheme.colorScheme.error)

        Text(
            "OAuth is handled by the backend. No Instagram access token is entered in the app.",
            color = Color.LightGray
        )
    }
}

@Composable
private fun AIBrainScreen(state: AppState) {
    val scope = rememberCoroutineScope()
    var apiKey by remember { mutableStateOf("") }
    var responseModel by remember { mutableStateOf("gemini-3.1-flash-lite") }
    var analysisModel by remember { mutableStateOf("gemini-3.8-flash") }
    var systemPrompt by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val result = state.loadAi()
        if (result.ok) {
            val settings = result.json.optJSONObject("settings")
            responseModel = settings?.optString("response_model", responseModel) ?: responseModel
            analysisModel = settings?.optString("analysis_model", analysisModel) ?: analysisModel
            systemPrompt = settings?.optString("system_prompt", "") ?: ""
            enabled = settings?.optBoolean("ai_enabled", true) ?: true
        }
    }

    Screen("AI Brain") {
        OutlinedTextField(
            apiKey, { apiKey = it }, Modifier.fillMaxWidth(),
            label = { Text("Gemini API Key") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(responseModel, { responseModel = it }, Modifier.fillMaxWidth(), label = { Text("Response model") }, singleLine = true)
        OutlinedTextField(analysisModel, { analysisModel = it }, Modifier.fillMaxWidth(), label = { Text("Analysis model") }, singleLine = true)
        OutlinedTextField(
            systemPrompt, { systemPrompt = it },
            Modifier.fillMaxWidth().height(150.dp),
            label = { Text("Permanent AI instruction") }
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("AI replies enabled", fontWeight = FontWeight.Bold)
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }

        Button(
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    busy = true
                    val result = state.saveAi(apiKey, responseModel, analysisModel, systemPrompt, enabled)
                    status = if (result.ok) "AI settings saved ✓" else result.error ?: "Save failed"
                    busy = false
                }
            }
        ) {
            Text(if (busy) "Saving…" else "Save AI Settings")
        }

        OutlinedButton(
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch {
                    busy = true
                    val result = state.testAi()
                    status = if (result.ok) "Gemini: " + result.json.optString("reply") else result.error ?: "AI test failed"
                    busy = false
                }
            }
        ) {
            Text("Test Gemini")
        }

        if (status.isNotEmpty()) Text(status, fontWeight = FontWeight.Bold)

        Text(
            "The Gemini key is sent over HTTPS and encrypted at rest on the backend. It is not hardcoded in the APK.",
            color = Color.LightGray
        )
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Color.LightGray)
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}
