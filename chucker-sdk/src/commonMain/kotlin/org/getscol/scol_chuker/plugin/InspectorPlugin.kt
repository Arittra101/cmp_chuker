package org.getscol.scol_chuker.plugin

import io.ktor.client.call.HttpClientCall
import io.ktor.client.call.save
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentLength
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.getscol.scol_chuker.data.db.HttpTransaction
import org.getscol.scol_chuker.data.repository.TransactionRepository
import org.koin.mp.KoinPlatform

/** Above this, a body is referenced rather than copied — the inspector must never OOM a host app. */
private const val MAX_CAPTURED_BODY_BYTES = 2L * 1024 * 1024

/**
 * Captures traffic from the `Send` hook rather than `onRequest`/`onResponse`, for two reasons:
 *
 *  - by `Send` the request body has already been through the serialization pipeline, so what gets
 *    stored is the JSON actually put on the wire — not the `toString()` of the caller's data class;
 *  - the response is handed on as a *saved* call, so reading the body here cannot steal it from the
 *    host app (a plain `bodyAsText()` in `onResponse` makes the caller's own read fail).
 */
val InspectorPlugin = createClientPlugin("InspectorPlugin") {

    val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val repository: TransactionRepository by lazy {
        KoinPlatform.getKoin().get()
    }

    fun persist(transaction: HttpTransaction) {
        coroutineScope.launch { runCatching { repository.insert(transaction) } }
    }

    on(Send) { request ->
        val captured = captureRequest(request)

        val call = try {
            proceed(request)
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            persist(captured.toFailure(cause))
            throw cause
        }

        val response = captureResponse(call)
        persist(captured.toTransaction(response))
        response.call
    }
}

// ---------------------------------------------------------------------------
// Capture — nothing below leaves the plugin
// ---------------------------------------------------------------------------

private class CapturedRequest(
    val method: String,
    val url: String,
    val headers: String,
    val body: String?,
    val bodySize: Long,
    val requestTime: Long,
)

private class CapturedResponse(
    val call: HttpClientCall,
    val statusCode: Int,
    val statusMessage: String,
    val headers: String,
    val body: String?,
    val bodySize: Long,
    val responseTime: Long,
)

private suspend fun captureRequest(request: HttpRequestBuilder): CapturedRequest {
    val content = request.body as? OutgoingContent
    val (body, size) = readOutgoingBody(content)
    val headers = buildString {
        request.headers.entries().forEach { (name, values) ->
            appendLine("$name: ${values.joinToString()}")
        }
        // Content-Type/Length live on the rendered content, not on the builder's header map.
        if (request.headers[HttpHeaders.ContentType] == null) {
            content?.contentType?.let { appendLine("${HttpHeaders.ContentType}: $it") }
        }
        if (request.headers[HttpHeaders.ContentLength] == null) {
            content?.contentLength?.let { appendLine("${HttpHeaders.ContentLength}: $it") }
        }
    }.trim()

    return CapturedRequest(
        method = request.method.value,
        url = request.url.buildString(),
        headers = headers,
        body = body,
        bodySize = size,
        requestTime = currentTimeMillis(),
    )
}

private suspend fun captureResponse(call: HttpClientCall): CapturedResponse {
    val response = call.response
    val headers = response.headers.entries()
        .joinToString("\n") { (name, values) -> "$name: ${values.joinToString()}" }
    val contentType = runCatching { response.headers[HttpHeaders.ContentType]?.let(ContentType::parse) }
        .getOrNull()
    val declaredLength = response.contentLength()

    fun capture(body: String?, size: Long, target: HttpClientCall) = CapturedResponse(
        call = target,
        statusCode = response.status.value,
        statusMessage = response.status.description,
        headers = headers,
        body = body,
        bodySize = size,
        responseTime = currentTimeMillis(),
    )

    // Binary or oversized payload: note it and leave the caller's stream completely untouched.
    if (!isTextual(contentType) || (declaredLength != null && declaredLength > MAX_CAPTURED_BODY_BYTES)) {
        val note = "(not captured — ${contentType ?: "unknown type"}" +
            (declaredLength?.let { ", ${it} bytes" } ?: "") + ")"
        return capture(note.takeIf { declaredLength != 0L }, declaredLength ?: 0L, call)
    }

    val saved = runCatching { call.save() }.getOrNull()
        ?: return capture(null, declaredLength ?: 0L, call)
    val body = runCatching { saved.response.bodyAsText() }.getOrNull()
    return capture(body, body?.utf8Size() ?: declaredLength ?: 0L, saved)
}

private suspend fun readOutgoingBody(content: OutgoingContent?): Pair<String?, Long> =
    when (content) {
        null, is OutgoingContent.NoContent -> null to 0L

        is OutgoingContent.ByteArrayContent -> {
            val bytes = content.bytes()
            val text = if (isTextual(content.contentType)) {
                bytes.decodeToString()
            } else {
                "(binary body — ${bytes.size} bytes not captured)"
            }
            text to bytes.size.toLong()
        }

        is OutgoingContent.ContentWrapper -> readOutgoingBody(content.delegate())

        is OutgoingContent.ProtocolUpgrade -> "(protocol upgrade)" to 0L

        // Streamed: reading it here would consume the request the engine is about to send.
        else -> "(streamed body — not captured)" to (content.contentLength ?: 0L)
    }

/** Only text-shaped payloads are worth storing as text; the rest would be mojibake. */
private fun isTextual(type: ContentType?): Boolean {
    if (type == null) return true
    val subtype = type.contentSubtype.lowercase()
    return when (type.contentType.lowercase()) {
        "text" -> true
        "application" -> subtype in textualApplicationSubtypes ||
            subtype.endsWith("+json") ||
            subtype.endsWith("+xml")
        else -> false
    }
}

private val textualApplicationSubtypes = setOf(
    "json",
    "xml",
    "javascript",
    "ecmascript",
    "graphql",
    "x-www-form-urlencoded",
    "x-ndjson",
    "problem+json",
)

private fun String.utf8Size(): Long = encodeToByteArray().size.toLong()

private fun CapturedRequest.toTransaction(response: CapturedResponse) = HttpTransaction(
    method = method,
    url = url,
    requestHeaders = headers,
    requestBody = body,
    requestBodySize = bodySize,
    requestTime = requestTime,
    statusCode = response.statusCode,
    statusMessage = response.statusMessage,
    responseHeaders = response.headers,
    responseBody = response.body,
    responseBodySize = response.bodySize,
    responseTime = response.responseTime,
    durationMs = response.responseTime - requestTime,
)

/** A call that never came back is stored with status `0` — the list renders it as `FAIL`. */
private fun CapturedRequest.toFailure(cause: Throwable): HttpTransaction {
    val failedAt = currentTimeMillis()
    return HttpTransaction(
        method = method,
        url = url,
        requestHeaders = headers,
        requestBody = body,
        requestBodySize = bodySize,
        requestTime = requestTime,
        statusCode = 0,
        statusMessage = cause.message?.takeIf { it.isNotBlank() }
            ?: (cause::class.simpleName ?: "Request failed"),
        responseHeaders = "",
        responseBody = null,
        responseBodySize = 0,
        responseTime = failedAt,
        durationMs = failedAt - requestTime,
    )
}

internal expect fun currentTimeMillis(): Long
