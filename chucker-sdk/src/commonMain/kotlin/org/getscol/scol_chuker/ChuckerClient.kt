package org.getscol.scol_chuker

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import org.getscol.scol_chuker.plugin.InspectorPlugin

fun createChuckerHttpClient(
    config: HttpClientConfig<*>.() -> Unit = {},
): HttpClient = HttpClient {
    install(InspectorPlugin)
    config()
}
