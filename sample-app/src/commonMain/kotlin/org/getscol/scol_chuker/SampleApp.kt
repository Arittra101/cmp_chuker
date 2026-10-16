package org.getscol.scol_chuker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.getscol.scol_chuker.ui.overlay.CmpChuckerOverlay

/** Sent as an object — the inspector must show the JSON, never this class's `toString()`. */
@Serializable
private data class NewPost(
    val title: String,
    val body: String,
    val userId: Int,
    val tags: List<String>,
    val draft: Boolean,
)

/**
 * The whole sample UI — shared by Android ([MainActivity]) and iOS (`MainViewController`).
 *
 * Note what the host app has to do: nothing beyond dropping [CmpChuckerOverlay] into the root
 * composable and using [createChuckerHttpClient] for its traffic. `CmpChucker.init(…)` runs on
 * each platform's entry point, because only there is the platform context available.
 */
@Composable
fun SampleApp() {
    MaterialTheme {
        val httpClient = remember {
            createChuckerHttpClient {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
        }
        val scope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }
        var loading by remember { mutableStateOf(false) }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "SCOL Chucker Demo",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fire a request — the bug button flashes and counts it. Tap it to inspect.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    if (loading) {
                        CircularProgressIndicator()
                    } else {
                        val fire: (suspend () -> Unit) -> Unit = { call ->
                            scope.launch {
                                loading = true
                                runCatching { call() }
                                    .onSuccess { snackbar.showSnackbar("✅ done — check the inspector") }
                                    .onFailure { snackbar.showSnackbar("❌ ${it.message}") }
                                loading = false
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SampleButton("GET /todos/1") {
                                fire {
                                    httpClient.get("https://jsonplaceholder.typicode.com/todos/1")
                                        .bodyAsText()
                                }
                            }
                            SampleButton("GET /posts (big JSON)") {
                                fire {
                                    httpClient.get("https://jsonplaceholder.typicode.com/posts")
                                        .bodyAsText()
                                }
                            }
                            SampleButton("POST /posts (object body)") {
                                fire {
                                    httpClient.post("https://jsonplaceholder.typicode.com/posts") {
                                        contentType(ContentType.Application.Json)
                                        setBody(
                                            NewPost(
                                                title = "Captured by SCOL Chucker",
                                                body = "Ktor serializes this before the inspector sees it.",
                                                userId = 7,
                                                tags = listOf("kmp", "ktor", "inspector"),
                                                draft = false,
                                            ),
                                        )
                                    }.bodyAsText()
                                }
                            }
                            SampleButton("GET /404 (error)") {
                                fire {
                                    httpClient.get("https://jsonplaceholder.typicode.com/nonexistent")
                                        .bodyAsText()
                                }
                            }
                            SampleButton("GET unreachable host (fail)") {
                                fire {
                                    httpClient.get("https://host.invalid/health").bodyAsText()
                                }
                            }
                        }
                    }
                }

                // 3️⃣  Drop the overlay into the root composable — that's it!
                CmpChuckerOverlay()
            }
        }
    }
}

@Composable
private fun SampleButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(label)
    }
}
