package org.example.scol_chuker.plugin

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.scol_chuker.data.db.HttpTransaction
import org.example.scol_chuker.data.repository.TransactionRepository
import org.koin.mp.KoinPlatform

/**
 * Ktor client plugin that intercepts every request/response and saves it to the
 * Chucker SQLDelight database. Install it with:
 *
 *   val client = HttpClient { install(InspectorPlugin) }
 */
val InspectorPlugin = createClientPlugin("InspectorPlugin") {

    val repository: TransactionRepository by lazy {
        KoinPlatform.getKoin().get()
    }

    // Temporarily holds request data until the matching response arrives.
    var capturedRequest: PartialRequest? = null

    onRequest { request, body ->
        capturedRequest = PartialRequest(
            method = request.method.value,
            url = request.url.toString(),
            headers = request.headers.entries()
                .joinToString("\n") { (key, values) -> "$key: ${values.joinToString()}" },
            body = body?.toString(),
            requestTime = currentTimeMillis(),
        )
    }

    onResponse { response ->
        val req = capturedRequest ?: return@onResponse
        val responseTime = currentTimeMillis()

        val transaction = HttpTransaction(
            method = req.method,
            url = req.url,
            requestHeaders = req.headers,
            requestBody = req.body,
            requestTime = req.requestTime,
            statusCode = response.status.value,
            statusMessage = response.status.description,
            responseHeaders = response.headers.entries()
                .joinToString("\n") { (key, values) -> "$key: ${values.joinToString()}" },
            responseBody = runCatching { response.bodyAsText() }.getOrNull(),
            responseTime = responseTime,
            durationMs = responseTime - req.requestTime,
        )

        CoroutineScope(Dispatchers.Default).launch {
            runCatching { repository.insert(transaction) }
        }
    }
}

// ---------------------------------------------------------------------------
// Internal data holder — never leaves the plugin
// ---------------------------------------------------------------------------

private data class PartialRequest(
    val method: String,
    val url: String,
    val headers: String,
    val body: String?,
    val requestTime: Long,
)

// ---------------------------------------------------------------------------
// CMP-safe time helper
// ---------------------------------------------------------------------------
internal expect fun currentTimeMillis(): Long
