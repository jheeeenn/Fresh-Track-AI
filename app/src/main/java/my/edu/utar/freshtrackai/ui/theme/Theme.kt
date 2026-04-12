package my.edu.utar.freshtrackai.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = FreshDarkPrimary,
    onPrimary = FreshDarkOnPrimary,
    primaryContainer = FreshDarkPrimaryContainer,
    onPrimaryContainer = FreshDarkOnPrimaryContainer,
    secondary = FreshDarkSecondary,
    onSecondary = FreshDarkOnSecondary,
    secondaryContainer = FreshDarkSecondaryContainer,
    onSecondaryContainer = FreshDarkOnSecondaryContainer,
    tertiary = FreshDarkTertiary,
    onTertiary = FreshDarkOnTertiary,
    tertiaryContainer = FreshDarkTertiaryContainer,
    onTertiaryContainer = FreshDarkOnTertiaryContainer,
    background = FreshDarkBackground,
    onBackground = FreshDarkOnBackground,
    surface = FreshDarkSurface,
    onSurface = FreshDarkOnSurface,
    surfaceVariant = FreshDarkSurfaceVariant,
    onSurfaceVariant = FreshDarkOnSurfaceVariant,
    error = FreshError,
    onError = FreshOnError,
    errorContainer = FreshErrorContainer,
    onErrorContainer = FreshOnErrorContainer,
    outline = FreshDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = FreshPrimary,
    onPrimary = FreshOnPrimary,
    primaryContainer = FreshPrimaryContainer,
    onPrimaryContainer = FreshOnPrimaryContainer,
    secondary = FreshSecondary,
    onSecondary = FreshOnSecondary,
    secondaryContainer = FreshSecondaryContainer,
    onSecondaryContainer = FreshOnSecondaryContainer,
    tertiary = FreshTertiary,
    onTertiary = FreshOnTertiary,
    background = FreshBackground,
    onBackground = FreshOnBackground,
    surface = FreshSurface,
    onSurface = FreshOnSurface,
    surfaceVariant = FreshSurfaceVariant,
    onSurfaceVariant = FreshOnSurfaceVariant,
    error = FreshError,
    onError = FreshOnError,
    errorContainer = FreshErrorContainer,
    onErrorContainer = FreshOnErrorContainer,
    outline = FreshOutline,
    outlineVariant = FreshOutlineVariant
)

@Composable
fun FreshTrackAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep Stitch palette stable unless explicitly overridden.
    dynamicColor: Boolean = false,
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
