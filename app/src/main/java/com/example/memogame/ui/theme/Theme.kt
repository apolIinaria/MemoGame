package com.example.memogame.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

val PinkPrimary = Color(0xFFFF4081)
val PinkDark = Color(0xFFC60055)
val PinkLight = Color(0xFFFF79B0)

val CardBorderPink = Color(0xFFFF598E)

private val PinkLightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color.White,
    primaryContainer = PinkLight,
    onPrimaryContainer = Color(0xFF3F0021),

    secondary = Color(0xFF9C27B0),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8D5F0),
    onSecondaryContainer = Color(0xFF3E0046),

    tertiary = Color(0xFFFF9800),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    onTertiaryContainer = Color(0xFF522700),

    background = Color(0xFFFEF0F3),
    onBackground = Color(0xFF1A1A1A),

    surface = Color.White,
    onSurface = Color(0xFF1A1A1A),

    surfaceVariant = Color(0xFFFFE2EA),
    onSurfaceVariant = Color(0xFF4A4A4A),

    error = Color(0xFFE53935),
    onError = Color.White
)

private val PinkDarkColorScheme = darkColorScheme(
    primary = PinkLight,
    onPrimary = Color(0xFF3F0021),
    primaryContainer = PinkPrimary,
    onPrimaryContainer = Color(0xFFFFD9E3),

    secondary = Color(0xFFCE93D8),
    onSecondary = Color(0xFF3E0046),
    secondaryContainer = Color(0xFF6C1B85),
    onSecondaryContainer = Color(0xFFEED6F6),

    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF522700),
    tertiaryContainer = Color(0xFFB36A00),
    onTertiaryContainer = Color(0xFFFFE0B2),

    background = Color(0xFF2C1A21),
    onBackground = Color(0xFFF5F5F5),

    surface = Color(0xFF3A2530),
    onSurface = Color(0xFFF5F5F5),

    surfaceVariant = Color(0xFF442631),
    onSurfaceVariant = Color(0xFFD9D9D9),

    error = Color(0xFFEF5350),
    onError = Color.White
)

@Composable
fun MemoGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PinkDarkColorScheme
        else -> PinkLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flags = window.decorView.systemUiVisibility
                if (!darkTheme) {
                    window.decorView.systemUiVisibility = flags or android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                } else {
                    window.decorView.systemUiVisibility = flags and android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}