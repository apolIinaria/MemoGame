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

// Світла кольорова схема - більш свіжа та грайлива
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1E88E5),        // Яскравий синій
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0E4FF),
    onPrimaryContainer = Color(0xFF0A467E),
    secondary = Color(0xFFFF6D00),      // Теплий помаранчевий
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE5D0),
    onSecondaryContainer = Color(0xFF772D00),
    tertiary = Color(0xFF4CAF50),       // Зелений для успіху
    background = Color(0xFFF5F5F5),     // Світлий сірий фон
    surface = Color.White,
    onSurface = Color(0xFF1E1E1E),
    surfaceVariant = Color(0xFFEEF1F4),
    onSurfaceVariant = Color(0xFF4A4A4A),
    error = Color(0xFFE53935)           // Яскравий червоний для помилок
)

// Темна кольорова схема - елегантна та зручна для очей
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF64B5F6),        // Яскраво-блакитний
    onPrimary = Color(0xFF002D58),
    primaryContainer = Color(0xFF0A467E),
    onPrimaryContainer = Color(0xFFD0E4FF),
    secondary = Color(0xFFFFAB40),      // Теплий помаранчевий
    onSecondary = Color(0xFF4A2700),
    secondaryContainer = Color(0xFF703E00),
    onSecondaryContainer = Color(0xFFFFE0B2),
    tertiary = Color(0xFF81C784),       // М'який зелений
    background = Color(0xFF121212),     // Темний фон
    surface = Color(0xFF1E1E1E),        // Трохи світліший за фон
    onSurface = Color(0xFFE0E0E0),
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color(0xFFBDBDBD),
    error = Color(0xFFEF5350)           // Яскравий червоний для помилок
)

@Composable
fun MemoGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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

    // Налаштовуємо колір статус бару більш безпечним способом
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()

            // Більш безпечний спосіб налаштування зовнішнього вигляду статус-бару
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val flags = window.decorView.systemUiVisibility
                if (!darkTheme) {
                    // Встановлюємо світлі іконки для темного фону і навпаки
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