package org.example.scol_chuker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.launch
import org.example.scol_chuker.plugin.InspectorPlugin
import org.example.scol_chuker.ui.overlay.CmpChuckerOverlay

class MainActivity : ComponentActivity() {

    // 2️⃣  Install InspectorPlugin on the Ktor client
    private val httpClient = HttpClient {
        install(InspectorPlugin)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
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
                        // Main content
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Chucker SDK Demo",
                                style = MaterialTheme.typography.headlineSmall,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap a button to fire an HTTP request, then tap 🐛 to inspect it.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(32.dp))

                            if (loading) {
                                CircularProgressIndicator()
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    SampleButton("GET /todos/1") {
                                        scope.launch {
                                            loading = true
                                            runCatching {
                                                httpClient.get("https://jsonplaceholder.typicode.com/todos/1").bodyAsText()
                                            }.onSuccess {
                                                snackbar.showSnackbar("✅ 200 OK — check the inspector!")
                                            }.onFailure {
                                                snackbar.showSnackbar("❌ ${it.message}")
                                            }
                                            loading = false
                                        }
                                    }

                                    SampleButton("GET /posts") {
                                        scope.launch {
                                            loading = true
                                            runCatching {
                                                httpClient.get("https://jsonplaceholder.typicode.com/posts").bodyAsText()
                                            }.onSuccess {
                                                snackbar.showSnackbar("✅ 200 OK — check the inspector!")
                                            }.onFailure {
                                                snackbar.showSnackbar("❌ ${it.message}")
                                            }
                                            loading = false
                                        }
                                    }

                                    SampleButton("GET /404 (error)") {
                                        scope.launch {
                                            loading = true
                                            runCatching {
                                                httpClient.get("https://jsonplaceholder.typicode.com/nonexistent").bodyAsText()
                                            }.onSuccess {
                                                snackbar.showSnackbar("Response received — check the inspector!")
                                            }.onFailure {
                                                snackbar.showSnackbar("❌ ${it.message}")
                                            }
                                            loading = false
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
    }
}

@androidx.compose.runtime.Composable
private fun SampleButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(label)
    }
}