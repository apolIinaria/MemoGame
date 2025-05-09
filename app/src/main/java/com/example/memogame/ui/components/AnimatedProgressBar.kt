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
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = animationSpec,
        label = "progress_animation"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(height)
                .background(progressColor)
        )
    }
}

@Composable
fun AnimatedColorProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 6.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val progressColor = when {
        progress >= 0.9f -> Color(0xFFFFD700)
        progress >= 0.5f -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    AnimatedProgressBar(
        progress = progress,
        modifier = modifier,
        height = height,
        backgroundColor = backgroundColor,
        progressColor = progressColor
    )
}