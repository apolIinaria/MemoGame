package com.example.memogame.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.memogame.R
import com.example.memogame.model.Card

@Composable
fun CardItem(
    card: Card,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Перевіряємо, чи це малий екран для оптимізації розмірів
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 400

    // Зменшуємо відступи та розміри для малих екранів
    val innerPadding = if (isSmallScreen) 6.dp else 12.dp
    val elevation = if (card.isMatched) 1.dp else if (isSmallScreen) 2.dp else 4.dp

    // Оптимізуємо пропорції карток
    val aspectRatio = if (isSmallScreen) 0.80f else 0.75f

    // Механізм блокування кліків для уникнення швидких подвійних кліків
    val clickable = remember { mutableStateOf(true) }

    // Налаштування анімації з більш плавним переходом
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing
        ),
        label = "card_rotation"
    )

    // Затемнення картки, якщо вона вже знайдена (matched)
    val alpha = if (card.isMatched) 0.7f else 1.0f

    // Базовий модифікатор для картки
    val cardModifier = modifier
        .aspectRatio(aspectRatio)
        .shadow(elevation)
        .zIndex(if (card.isFlipped) 1f else 0f)
        .graphicsLayer {
            rotationY = rotation
            cameraDistance = 8f * density
            this.alpha = alpha
        }
        .clickable(
            enabled = !card.isMatched && !card.isFlipped && clickable.value,
            onClick = {
                if (clickable.value) {
                    clickable.value = false // Блокуємо повторні кліки
                    onClick()
                    // Розблоковуємо кліки через затримку
                    android.os.Handler().postDelayed({ clickable.value = true }, 700)
                }
            }
        )

    Card(
        modifier = cardModifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Візуалізуємо лицьову сторону (з зображенням)
            if (rotation > 90f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = card.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                // Зворотна сторона (закрита)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.card_back),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.8f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}