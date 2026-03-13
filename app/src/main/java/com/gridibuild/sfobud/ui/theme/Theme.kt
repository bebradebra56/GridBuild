package com.gridibuild.sfobud.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE5C2),
    onPrimaryContainer = Color(0xFF4A2800),
    secondary = Turquoise,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB8F5ED),
    onSecondaryContainer = Color(0xFF003731),
    tertiary = SaturatedBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6E4FF),
    onTertiaryContainer = Color(0xFF001849),
    background = LightBackground,
    onBackground = DarkBlueViolet,
    surface = LightBackground,
    onSurface = DarkBlueViolet,
    surfaceVariant = SoftSectionBg,
    onSurfaceVariant = OnSurfaceVariant,
    outline = GrayBeige,
    error = WarmRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColorScheme = darkColorScheme(
    primary = BrightYellow,
    onPrimary = Color(0xFF3D2800),
    primaryContainer = Color(0xFF573C00),
    onPrimaryContainer = Color(0xFFFFDF99),
    secondary = Turquoise,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = Color(0xFF73F9E9),
    tertiary = Color(0xFF90BFFF),
    onTertiary = Color(0xFF003062),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFCBC4D8),
    outline = Color(0xFF958F9F),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun GridBuildTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
