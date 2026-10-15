package org.getscol.scol_chuker.plugin

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.util.AttributeKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.getscol.scol_chuker.data.db.HttpTransaction
import org.getscol.scol_chuker.data.repository.TransactionRepository
import org.koin.mp.KoinPlatform

val InspectorPlugin = createClientPlugin("InspectorPlugin") {

    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
                body = body.toString(),
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

        coroutineScope.launch {
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

internal expect fun currentTimeMillis(): Long
