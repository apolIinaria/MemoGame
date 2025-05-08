package com.example.memogame.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    stars: Int,
    maxStars: Int = 3,
    modifier: Modifier = Modifier,
    starSize: Dp = 24.dp,
    animated: Boolean = true
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            val isStarFilled = i <= stars

            // Анімований масштаб для зірок
            val scale by animateFloatAsState(
                targetValue = if (isStarFilled && animated) 1.2f else 1f,
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing,
                    delayMillis = i * 100 // Поступова анімація кожної зірки
                ),
                label = "star_animation"
            )

            val starColor = when {
                isStarFilled -> Color(0xFFFFD700) // Золотий колір для заповнених зірок
                else -> Color.LightGray
            }

            // Використовуємо правильні іконки
            val starIcon = when {
                isStarFilled -> Icons.Filled.Star
                else -> Icons.Outlined.Star  // Правильна іконка з бібліотеки Icons.Outlined
            }

            Icon(
                imageVector = starIcon,
                contentDescription = null,
                tint = starColor,
                modifier = Modifier
                    .size(starSize)
                    .scale(if (animated) scale else 1f)
            )
        }
    }
}