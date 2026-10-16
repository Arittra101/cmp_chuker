package org.getscol.scol_chuker

import org.getscol.scol_chuker.data.db.HttpTransaction
import org.getscol.scol_chuker.ui.format.buildCurl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ExportTest {

    private fun transaction(
        method: String = "POST",
        headers: String = "Accept: application/json\nContent-Type: application/json\nContent-Length: 17",
        body: String? = """{"name":"it's"}""",
    ) = HttpTransaction(
        id = 1,
        method = method,
        url = "https://api.example.com/v1/users?page=2",
        requestHeaders = headers,
        requestBody = body,
        requestBodySize = 17,
        requestTime = 1_760_000_000_000,
        statusCode = 201,
        statusMessage = "Created",
        responseHeaders = "Content-Type: application/json",
        responseBody = """{"id":1}""",
        responseBodySize = 8,
        responseTime = 1_760_000_000_400,
        durationMs = 400,
    )

    @Test
    fun `curl is a runnable multi line command`() {
        val lines = buildCurl(transaction()).split("\n")

        assertEquals("curl -X POST \\", lines[0])
        assertEquals("  -H 'Accept: application/json' \\", lines[1])
        assertEquals("  -H 'Content-Type: application/json' \\", lines[2])
        // Content-Length is dropped: curl computes it, and the captured one may not match.
        assertEquals("""  --data-raw '{"name":"it'\''s"}' \""", lines[3])
        assertEquals("  'https://api.example.com/v1/users?page=2'", lines[4])
        assertEquals(5, lines.size, "no stray blank lines: $lines")
    }

    @Test
    fun `a bodyless request has no data flag`() {
        val curl = buildCurl(transaction(method = "GET", headers = "Accept: application/json", body = null))
        assertEquals(
            listOf(
                "curl -X GET \\",
                "  -H 'Accept: application/json' \\",
                "  'https://api.example.com/v1/users?page=2'",
            ),
            curl.split("\n"),
        )
        assertTrue("--data-raw" !in curl)
    }
}
