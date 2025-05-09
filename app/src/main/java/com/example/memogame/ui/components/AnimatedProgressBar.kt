package com.example.memogame.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Анімований прогрес-бар із плавною зміною значення
 *
 * @param progress Поточний прогрес від 0.0f до 1.0f
 * @param modifier Модифікатор для прогрес-бару
 * @param height Висота прогрес-бару
 * @param backgroundColor Колір фону прогрес-бару
 * @param progressColor Колір заповнення прогрес-бару
 * @param cornerRadius Радіус закруглення кутів
 * @param animationSpec Специфікація анімації для зміни прогресу
 */
@Composable
fun AnimatedProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    cornerRadius: Dp = 3.dp,
    animationSpec: AnimationSpec<Float> = tween(
        durationMillis = 500,
        easing = FastOutSlowInEasing
    )
) {
    // Анімація значення прогресу
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = animationSpec,
        label = "progress_animation"
    )

    // Контейнер з фоном
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        // Індикатор прогресу
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(height)
                .background(progressColor)
        )
    }
}

/**
 * Анімований прогрес-бар, який змінює колір залежно від прогресу
 *
 * @param progress Поточний прогрес від 0.0f до 1.0f
 * @param modifier Модифікатор для прогрес-бару
 * @param height Висота прогрес-бару
 * @param backgroundColor Колір фону прогрес-бару
 */
@Composable
fun AnimatedColorProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    // Визначаємо колір заповнення залежно від прогресу
    val progressColor = when {
        progress >= 0.9f -> Color(0xFFFFD700) // Золотий для майже завершених
        progress >= 0.5f -> MaterialTheme.colorScheme.primary // Основний колір для середнього прогресу
        else -> MaterialTheme.colorScheme.secondary // Другорядний колір для початку
    }

    // Використовуємо базовий анімований прогрес-бар
    AnimatedProgressBar(
        progress = progress,
        modifier = modifier,
        height = height,
        backgroundColor = backgroundColor,
        progressColor = progressColor
    )
}