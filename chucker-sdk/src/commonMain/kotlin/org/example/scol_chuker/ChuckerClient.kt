package org.example.scol_chuker

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import org.example.scol_chuker.plugin.InspectorPlugin

/**
 * Creates a Ktor [HttpClient] with [InspectorPlugin] installed so traffic appears in the inspector.
 *
 * **Android:** call [CmpChucker.init] in `Application.onCreate` before any requests.
 *
 * You must also add a Ktor **engine** in the host app (not bundled in the SDK), for example:
 * `implementation("io.ktor:ktor-client-cio:…")` on Android/JVM.
 */
fun createChuckerHttpClient(
    config: HttpClientConfig<*>.() -> Unit = {},
): HttpClient = HttpClient {
    install(InspectorPlugin)
    config()
}
