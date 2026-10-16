package org.getscol.scol_chuker

import org.getscol.scol_chuker.ui.format.formatBytes
import org.getscol.scol_chuker.ui.format.formatDuration
import org.getscol.scol_chuker.ui.format.highlightJson
import org.getscol.scol_chuker.ui.format.parseHeaders
import org.getscol.scol_chuker.ui.format.parseJsonOrNull
import org.getscol.scol_chuker.ui.format.urlHost
import org.getscol.scol_chuker.ui.format.urlPath
import org.getscol.scol_chuker.ui.theme.chuckerDarkColors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BodyFormatTest {

    private fun render(raw: String): List<String> {
        val element = assertNotNull(parseJsonOrNull(raw), "expected $raw to parse as JSON")
        return highlightJson(element, chuckerDarkColors()).map { it.text }
    }

    @Test
    fun `nested json is laid out one token per line with two space indents`() {
        val lines = render("""{"id":7,"tags":["a","b"],"meta":{"ok":true,"note":null}}""")

        assertEquals(
            listOf(
                "{",
                """  "id": 7,""",
                """  "tags": [""",
                "    \"a\",",
                "    \"b\"",
                "  ],",
                """  "meta": {""",
                """    "ok": true,""",
                """    "note": null""",
                "  }",
                "}",
            ),
            lines,
        )
    }

    @Test
    fun `empty containers stay on one line`() {
        assertEquals(listOf("""{""", """  "a": {},""", """  "b": []""", "}"), render("""{"a":{},"b":[]}"""))
    }

    @Test
    fun `quotes and newlines inside values are escaped rather than emitted raw`() {
        val lines = render("""{"q":"say \"hi\"\nthen go"}""")
        assertEquals(3, lines.size)
        assertEquals("""  "q": "say \"hi\"\nthen go"""", lines[1])
    }

    @Test
    fun `non json bodies are not mistaken for json`() {
        assertNull(parseJsonOrNull("<html><body>nope</body></html>"))
        assertNull(parseJsonOrNull("plain text"))
        assertNull(parseJsonOrNull(""))
        assertNull(parseJsonOrNull("{not really json"))
    }

    @Test
    fun `headers split into name value pairs`() {
        val headers = parseHeaders("Content-Type: application/json; charset=utf-8\nX-Trace: abc:def")
        assertEquals("Content-Type" to "application/json; charset=utf-8", headers[0])
        assertEquals("X-Trace" to "abc:def", headers[1])
    }

    @Test
    fun `url splits into host and path`() {
        val url = "https://api.example.com/v1/users?page=2"
        assertEquals("api.example.com", urlHost(url))
        assertEquals("/v1/users?page=2", urlPath(url))
        assertEquals("api.example.com", urlHost("https://api.example.com"))
        assertEquals("/", urlPath("https://api.example.com"))
    }

    @Test
    fun `sizes and durations read like a dev tool`() {
        assertEquals("0 B", formatBytes(0))
        assertEquals("512 B", formatBytes(512))
        assertEquals("1.5 kB", formatBytes(1_536))
        assertEquals("812 ms", formatDuration(812))
        assertEquals("1.2 s", formatDuration(1_234))
        assertTrue(formatDuration(65_000).startsWith("1m"))
    }
}
