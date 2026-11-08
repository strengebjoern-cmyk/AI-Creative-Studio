package de.aicreativestudio.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class Project(val title: String, val content: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("projects", MODE_PRIVATE)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                var topic by remember { mutableStateOf("") }
                var output by remember { mutableStateOf("") }
                var endpoint by remember { mutableStateOf("http://10.0.2.2:11434") }
                var model by remember { mutableStateOf("llama3.2") }
                var busy by remember { mutableStateOf(false) }
                var tab by remember { mutableStateOf(0) }
                var projects by remember { mutableStateOf(loadProjects(prefs.getString("data", "[]") ?: "[]")) }
                val scope = rememberCoroutineScope()
                val context = LocalContext.current
                Scaffold(topBar = { Surface { Text("AI Creative Studio · Free", modifier = Modifier.padding(20.dp), style = MaterialTheme.typography.titleLarge) } }) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                        TabRow(selectedTabIndex = tab) {
                            listOf("Studio", "Projekte", "Einstellungen").forEachIndexed { i, name ->
                                Tab(selected = tab == i, onClick = { tab = i }, text = { Text(name) })
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        when (tab) {
                            0 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                item {
                                    Text("Dein nächster Social-Media-Content", style = MaterialTheme.typography.titleMedium)
                                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Thema oder Zielgruppe") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = { output = createStoryboard(topic) }, enabled = topic.isNotBlank()) { Text("3 Storylines entwerfen") }
                                    Spacer(Modifier.height(8.dp))
                                    Button(onClick = {
                                        busy = true
                                        scope.launch {
                                            output = try { askOllama(endpoint, model, topic) } catch (e: Exception) {
                                                "KI-Verbindung fehlgeschlagen: ${e.message}\n\nPrüfe Serveradresse und Modell in Einstellungen."
                                            }
                                            busy = false
                                        }
                                    }, enabled = topic.isNotBlank() && !busy) { Text(if (busy) "KI arbeitet …" else "Mit lokaler KI ausarbeiten") }
                                }
                                if (output.isNotBlank()) item {
                                    OutlinedTextField(value = output, onValueChange = { output = it }, label = { Text("Storyboard – bearbeitbar") }, minLines = 12, modifier = Modifier.fillMaxWidth())
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            projects = projects + Project(topic, output)
                                            prefs.edit().putString("data", saveProjects(projects)).apply()
                                        }) { Text("Speichern") }
                                        OutlinedButton(onClick = {
                                            val intent = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, output) }
                                            context.startActivity(Intent.createChooser(intent, "Storyboard teilen"))
                                        }) { Text("Teilen") }
                                    }
                                }
                            }
                            1 -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (projects.isEmpty()) item { Text("Noch keine Projekte gespeichert.") }
                                items(projects.indices.toList()) { i ->
                                    ElevatedCard(onClick = { topic = projects[i].title; output = projects[i].content; tab = 0 }) {
                                        Column(Modifier.padding(16.dp)) {
                                            Text(projects[i].title, style = MaterialTheme.typography.titleMedium)
                                            Text(projects[i].content.take(140) + "…")
                                            TextButton(onClick = {
                                                projects = projects.filterIndexed { index, _ -> index != i }
                                                prefs.edit().putString("data", saveProjects(projects)).apply()
                                            }) { Text("Löschen") }
                                        }
                                    }
                                }
                            }
                            2 -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("Optionale kostenlose KI über Ollama", style = MaterialTheme.typography.titleMedium)
                                Text("Ollama läuft auf deinem eigenen PC oder Server. Auf einem echten Smartphone trägst du dessen lokale Netzwerkadresse ein (z. B. http://192.168.1.10:11434). 10.0.2.2 funktioniert nur im Android-Emulator.")
                                OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, label = { Text("Ollama-Server") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modellname") }, modifier = Modifier.fillMaxWidth())
                                Text("Hinweis: HTTP ist für lokale Tests freigegeben. Keine öffentlich erreichbaren ungeschützten Ollama-Server verwenden.")
                                Text("Bild- und Videogenerierung sind in Version 0.1 noch nicht integriert.")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun createStoryboard(topic: String): String {
    val t = topic.trim()
    return listOf(
        "1. EMOTIONALER EINSTIEG\nHook: Was wäre, wenn $t deinen Alltag verändert?\nSzene 1 (0–5 s): Nahaufnahme, überraschender Moment.\nSzene 2 (5–20 s): Problem anhand einer Person zeigen.\nSzene 3 (20–35 s): Lösung anschaulich demonstrieren.\nSzene 4 (35–45 s): Ergebnis und Handlungsaufforderung.\nBildprompt: Cinematic documentary, natural lighting, vertical 9:16, topic: $t.",
        "2. WISSEN IN 30 SEKUNDEN\nHook: Drei Dinge, die du über $t wissen solltest.\nSzene 1: Dynamische Titelkarte.\nSzene 2: Erster Fakt mit Illustration.\nSzene 3: Zweiter Fakt als Beispiel.\nSzene 4: Dritter Fakt und Zusammenfassung.\nBildprompt: Clean editorial illustration, high contrast, vertical 9:16, topic: $t.",
        "3. ANIMIERTE MINI-STORY\nHook: Eine kleine Geschichte über $t.\nSzene 1: Figur und Ziel vorstellen.\nSzene 2: Hindernis mit visueller Metapher.\nSzene 3: Unerwartete Wendung.\nSzene 4: Positive Auflösung und Frage ans Publikum.\nBildprompt: High-quality stylized 3D animation, consistent character, vertical 9:16, topic: $t."
    ).joinToString("\n\n────────────────\n\n")
}

private suspend fun askOllama(base: String, model: String, topic: String): String = withContext(Dispatchers.IO) {
    require(base.startsWith("http://") || base.startsWith("https://")) { "Ungültige Serveradresse" }
    val connection = URL(base.trimEnd('/') + "/api/generate").openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 10000
        connection.readTimeout = 120000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        val prompt = "Antworte auf Deutsch. Erstelle drei originelle Social-Media-Storylines für das Thema: $topic. Für jede: Hook, Zielgruppe, 4 Szenen mit Zeitangaben, Sprechertext, Bildprompt, Videoprompt und CTA. Keine unbelegten Tatsachen erfinden."
        val body = JSONObject().put("model", model).put("prompt", prompt).put("stream", false).toString()
        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val response = (if (code in 200..299) connection.inputStream else connection.errorStream).bufferedReader().use { it.readText() }
        if (code !in 200..299) error("HTTP $code: ${response.take(300)}")
        JSONObject(response).getString("response")
    } finally { connection.disconnect() }
}

private fun loadProjects(raw: String): List<Project> = try {
    val array = JSONArray(raw)
    (0 until array.length()).map { val p = array.getJSONObject(it); Project(p.getString("title"), p.getString("content")) }
} catch (_: Exception) { emptyList() }

private fun saveProjects(projects: List<Project>): String = JSONArray().apply {
    projects.forEach { put(JSONObject().put("title", it.title).put("content", it.content)) }
}.toString()
