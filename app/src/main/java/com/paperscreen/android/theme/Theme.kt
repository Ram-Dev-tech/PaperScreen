package com.paperscreen.android.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PaperDarkPrimaryText,
    secondary = PaperDarkSecondaryText,
    tertiary = PaperDarkSecondaryText,
    background = PaperDarkBackground,
    surface = PaperDarkBackground,
    onPrimary = PaperDarkBackground,
    onSecondary = PaperDarkBackground,
    onTertiary = PaperDarkBackground,
    onBackground = PaperDarkPrimaryText,
    onSurface = PaperDarkPrimaryText,
    surfaceVariant = PaperDarkBackground,
    onSurfaceVariant = PaperDarkSecondaryText,
    outline = PaperDarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = PaperLightPrimaryText,
    secondary = PaperLightSecondaryText,
    tertiary = PaperLightSecondaryText,
    background = PaperLightBackground,
    surface = PaperLightBackground,
    onPrimary = PaperLightBackground,
    onSecondary = PaperLightBackground,
    onTertiary = PaperLightBackground,
    onBackground = PaperLightPrimaryText,
    onSurface = PaperLightPrimaryText,
    surfaceVariant = PaperLightBackground,
    onSurfaceVariant = PaperLightSecondaryText,
    outline = PaperLightBorder
)

@Composable
fun PaperScreenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
