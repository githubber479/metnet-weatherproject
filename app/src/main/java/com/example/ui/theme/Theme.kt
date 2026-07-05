package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF90CAF9),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF81D4FA),
    onSecondary = Color(0xFF00334C),
    secondaryContainer = Color(0xFF004C6F),
    onSecondaryContainer = Color(0xFFBBE9FF),
    background = Color(0xFF0B1014),
    surface = Color(0xFF11171D),
    onBackground = Color(0xFFE2E2E5),
    onSurface = Color(0xFFE2E2E5),
    onSurfaceVariant = Color(0xFFBFC8D2),
    outline = Color(0xFF89929B)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0061A4),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF006590),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC9E6FF),
    onSecondaryContainer = Color(0xFF001E2F),
    background = Color(0xFFF8FAFD),
    surface = Color(0xFFF8FAFD),
    onBackground = Color(0xFF191C20),
    onSurface = Color(0xFF191C20),
    onSurfaceVariant = Color(0xFF42474E),
    outline = Color(0xFF72777F)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color to force Material Blue theme
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
