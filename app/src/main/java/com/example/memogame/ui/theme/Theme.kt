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

val PinkPrimary = Color(0xFFF8BBD0)
val PinkDark = Color(0xFFF48FB1)
val PinkLight = Color(0xFFFCE4EC)

val CardBorderPink = Color(0xFFF06292)

private val PinkLightColorScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = Color(0xFF442C2E),
    primaryContainer = PinkLight,
    onPrimaryContainer = Color(0xFF442C2E),

    secondary = Color(0xFFCE93D8),
    onSecondary = Color(0xFF442C2E),
    secondaryContainer = Color(0xFFF3E5F5),
    onSecondaryContainer = Color(0xFF442C2E),

    tertiary = Color(0xFFFFCC80),
    onTertiary = Color(0xFF442C2E),
    tertiaryContainer = Color(0xFFFFF3E0),
    onTertiaryContainer = Color(0xFF442C2E),

    background = Color(0xFFFEF6F7),
    onBackground = Color(0xFF442C2E),

    surface = Color.White,
    onSurface = Color(0xFF442C2E),

    surfaceVariant = Color(0xFFFFF0F5),
    onSurfaceVariant = Color(0xFF7B6F72),

    error = Color(0xFFEF9A9A),
    onError = Color(0xFF442C2E)
)

private val PinkDarkColorScheme = darkColorScheme(
    primary = PinkLight,
    onPrimary = Color(0xFF442C2E),
    primaryContainer = PinkPrimary,
    onPrimaryContainer = Color(0xFFFFEDF3),

    secondary = Color(0xFFE1BEE7),
    onSecondary = Color(0xFF442C2E),
    secondaryContainer = Color(0xFF7B4882),
    onSecondaryContainer = Color(0xFFF3E5F5),

    tertiary = Color(0xFFFFE0B2),
    onTertiary = Color(0xFF442C2E),
    tertiaryContainer = Color(0xFFE0884E),
    onTertiaryContainer = Color(0xFFFFF3E0),

    background = Color(0xFF362C30),
    onBackground = Color(0xFFF5F5F5),

    surface = Color(0xFF443238),
    onSurface = Color(0xFFF5F5F5),

    surfaceVariant = Color(0xFF514145),
    onSurfaceVariant = Color(0xFFE5E0E1),

    error = Color(0xFFFFABAB),
    onError = Color(0xFF442C2E)
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