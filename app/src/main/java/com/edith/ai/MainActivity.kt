package com.edith.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val Bg = Color(0xFF0B0D10)
private val Card = Color(0xFF14171C)
private val Accent = Color(0xFFE9EDF2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { EdithApp() }
    }
}

@Composable
fun EdithApp() {
    var selected by remember { mutableIntStateOf(0) }
    val items = listOf(
        Triple("Dashboard", Icons.Default.AutoAwesome, "dashboard"),
        Triple("Inbox", Icons.Default.Chat, "inbox"),
        Triple("Instagram", Icons.Default.Link, "instagram"),
        Triple("AI Brain", Icons.Default.AutoAwesome, "brain"),
        Triple("Analytics", Icons.Default.Analytics, "analytics"),
        Triple("Settings", Icons.Default.Settings, "settings")
    )

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
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Card) {
                    items.take(4).forEachIndexed { index, item ->
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
                    0 -> DashboardScreen()
                    1 -> InboxScreen()
                    2 -> InstagramScreen()
                    else -> AIBrainScreen()
                }
            }
        }
    }
}

@Composable
private fun Screen(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            content()
        }
    )
}

@Composable
private fun DashboardScreen() {
    Screen("E.D.I.T.H.") {
        Text("Your AI Instagram manager", color = Color.LightGray)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard("DMs", "0", Modifier.weight(1f))
            MetricCard("Comments", "0", Modifier.weight(1f))
        }
        MetricCard("Status", "Instagram not connected", Modifier.fillMaxWidth())
        MetricCard("AI", "Configure your own Gemini API key", Modifier.fillMaxWidth())
    }
}

@Composable
private fun InboxScreen() {
    Screen("Inbox") {
        MetricCard("Conversations", "0", Modifier.fillMaxWidth())
        Text("Instagram conversations will appear here after the account is connected.", color = Color.LightGray)
    }
}

@Composable
private fun InstagramScreen() {
    Screen("Instagram") {
        MetricCard("Connection", "Not connected", Modifier.fillMaxWidth())
        Button(onClick = { }) { Text("Connect Instagram") }
        Text("Meta OAuth and webhook wiring will be added in the next stage.", color = Color.LightGray)
    }
}

@Composable
private fun AIBrainScreen() {
    Screen("AI Brain") {
        MetricCard("Response model", "Gemini 3.1 Flash-Lite", Modifier.fillMaxWidth())
        MetricCard("Analysis model", "Gemini 3.8 Flash", Modifier.fillMaxWidth())
        OutlinedTextField(
            value = "",
            onValueChange = {},
            label = { Text("Gemini API Key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Button(onClick = { }) { Text("Test & Save API Key") }
        Text("The key is intended to belong to the app user and will be stored securely on-device before backend integration.", color = Color.LightGray)
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
