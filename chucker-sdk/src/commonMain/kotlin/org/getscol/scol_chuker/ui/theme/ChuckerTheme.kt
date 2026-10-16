package org.getscol.scol_chuker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The inspector ships its own palette on purpose: it is a debugging surface that must look and
 * read the same inside any host app, no matter what theme (or lack of one) the host installs.
 */
@Immutable
internal data class ChuckerColors(
    val success: Color,
    val redirect: Color,
    val clientError: Color,
    val serverError: Color,
    val failure: Color,
    val pending: Color,
    val get: Color,
    val post: Color,
    val put: Color,
    val patch: Color,
    val delete: Color,
    val otherMethod: Color,
    val jsonKey: Color,
    val jsonString: Color,
    val jsonNumber: Color,
    val jsonBoolean: Color,
    val jsonNull: Color,
    val punctuation: Color,
    val gutter: Color,
    val codeBackground: Color,
    val codeBorder: Color,
    val chip: Color,
)

private val DarkChuckerColors = ChuckerColors(
    success = Color(0xFF3FB950),
    redirect = Color(0xFF58A6FF),
    clientError = Color(0xFFD29922),
    serverError = Color(0xFFF85149),
    failure = Color(0xFFFF7B72),
    pending = Color(0xFF8B949E),
    get = Color(0xFF58A6FF),
    post = Color(0xFF3FB950),
    put = Color(0xFFD29922),
    patch = Color(0xFFBC8CFF),
    delete = Color(0xFFF85149),
    otherMethod = Color(0xFF8B949E),
    jsonKey = Color(0xFF7EE0FF),
    jsonString = Color(0xFF9CE3A0),
    jsonNumber = Color(0xFFFFB86C),
    jsonBoolean = Color(0xFFFF7AB2),
    jsonNull = Color(0xFFB392F0),
    punctuation = Color(0xFF8A93A6),
    gutter = Color(0xFF555E6D),
    codeBackground = Color(0xFF11141B),
    codeBorder = Color(0xFF262C38),
    chip = Color(0xFF1C2230),
)

private val LightChuckerColors = ChuckerColors(
    success = Color(0xFF1A7F37),
    redirect = Color(0xFF0969DA),
    clientError = Color(0xFF9A6700),
    serverError = Color(0xFFCF222E),
    failure = Color(0xFFCF222E),
    pending = Color(0xFF6E7781),
    get = Color(0xFF0969DA),
    post = Color(0xFF1A7F37),
    put = Color(0xFF9A6700),
    patch = Color(0xFF8250DF),
    delete = Color(0xFFCF222E),
    otherMethod = Color(0xFF6E7781),
    jsonKey = Color(0xFF0550AE),
    jsonString = Color(0xFF0A7C3E),
    jsonNumber = Color(0xFFB45309),
    jsonBoolean = Color(0xFFA21CAF),
    jsonNull = Color(0xFF6D28D9),
    punctuation = Color(0xFF6B7280),
    gutter = Color(0xFF9AA2AF),
    codeBackground = Color(0xFFECEFF4),
    codeBorder = Color(0xFFDDE2EA),
    chip = Color(0xFFEDF1F6),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF6FA8FF),
    onPrimary = Color(0xFF06101F),
    primaryContainer = Color(0xFF1B2B45),
    onPrimaryContainer = Color(0xFFCFE2FF),
    secondary = Color(0xFF9FB4CF),
    onSecondary = Color(0xFF0B1016),
    tertiary = Color(0xFF3FB950),
    onTertiary = Color(0xFF04140A),
    background = Color(0xFF0D1017),
    onBackground = Color(0xFFE6E9EF),
    surface = Color(0xFF131722),
    onSurface = Color(0xFFE6E9EF),
    surfaceVariant = Color(0xFF1B2130),
    onSurfaceVariant = Color(0xFFA9B2C1),
    outline = Color(0xFF2E3646),
    outlineVariant = Color(0xFF232A37),
    error = Color(0xFFF85149),
    onError = Color(0xFF1A0505),
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFDCE8FF),
    onPrimaryContainer = Color(0xFF0B2B63),
    secondary = Color(0xFF4A5568),
    onSecondary = Color(0xFFFFFFFF),
    tertiary = Color(0xFF1A7F37),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF11151C),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF11151C),
    surfaceVariant = Color(0xFFEEF1F5),
    onSurfaceVariant = Color(0xFF4F5967),
    outline = Color(0xFFD6DAE2),
    outlineVariant = Color(0xFFE4E8EE),
    error = Color(0xFFCF222E),
    onError = Color(0xFFFFFFFF),
)

internal val LocalChuckerColors = staticCompositionLocalOf { DarkChuckerColors }

/** The palette outside composition — used by tests and by non-composable formatting helpers. */
internal fun chuckerDarkColors(): ChuckerColors = DarkChuckerColors

internal val chuckerColors: ChuckerColors
    @Composable get() = LocalChuckerColors.current

@Composable
internal fun ChuckerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalChuckerColors provides if (darkTheme) DarkChuckerColors else LightChuckerColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkScheme else LightScheme,
            content = content,
        )
    }
}

internal fun ChuckerColors.forMethod(method: String): Color = when (method.uppercase()) {
    "GET" -> get
    "POST" -> post
    "PUT" -> put
    "PATCH" -> patch
    "DELETE" -> delete
    else -> otherMethod
}

internal fun ChuckerColors.forStatus(code: Int): Color = when {
    code == 0 -> failure
    code in 100..199 -> pending
    code in 200..299 -> success
    code in 300..399 -> redirect
    code in 400..499 -> clientError
    else -> serverError
}

/** `0` is the inspector's marker for "the call never produced a response". */
internal fun statusLabel(code: Int): String = if (code == 0) "FAIL" else code.toString()
