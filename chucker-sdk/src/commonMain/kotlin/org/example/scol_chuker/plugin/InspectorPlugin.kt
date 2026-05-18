package org.example.scol_chuker.plugin

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.example.scol_chuker.data.db.HttpTransaction
import org.example.scol_chuker.data.repository.TransactionRepository
import org.koin.mp.KoinPlatform

/**
 * Ktor client plugin that records requests/responses into the inspector database.
 *
 * Prefer [createChuckerHttpClient] in host apps. Manual install:
 * ```
 * val client = HttpClient { install(InspectorPlugin) }
 * ```
 *
 * Requires Koin (started via [CmpChucker.init] on Android) before the first request.
 */
val InspectorPlugin = createClientPlugin("InspectorPlugin") {

    val repository: TransactionRepository by lazy {
        KoinPlatform.getKoin().get()
    }

    onRequest { request, body ->
        request.attributes.put(
            PartialRequestKey,
            PartialRequest(
                method = request.method.value,
                url = request.url.toString(),
                headers = request.headers.entries()
                    .joinToString("\n") { (key, values) -> "$key: ${values.joinToString()}" },
                body = body?.toString(),
                requestTime = currentTimeMillis(),
            ),
        )
    }

    onResponse { response ->
        val req = response.call.request.attributes.getOrNull(PartialRequestKey) ?: return@onResponse
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

private val PartialRequestKey = AttributeKey<PartialRequest>("ChuckerPartialRequest")

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
