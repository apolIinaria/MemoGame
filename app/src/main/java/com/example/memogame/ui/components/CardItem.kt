package com.example.memogame.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.memogame.R
import com.example.memogame.model.Card
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Механізм блокування кліків з безпечним збереженням стану
    val clickable = rememberSaveable { mutableStateOf(true) }

    // Створюємо CoroutineScope, прив'язаний до життєвого циклу компонента
    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

    // Відстежуємо життєвий цикл для правильного очищення ресурсів
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                scope.cancel()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            scope.cancel()
        }
    }

    // Налаштування анімації з більш безпечними параметрами
    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 250, // Менша тривалість для зниження навантаження
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
            cameraDistance = 12f * density // Збільшено для плавнішої анімації
            this.alpha = alpha
        }
        .clickable(
            enabled = !card.isMatched && !card.isFlipped && clickable.value,
            onClick = {
                if (clickable.value) {
                    clickable.value = false // Блокуємо повторні кліки

                    // Викликаємо обробник кліку
                    onClick()

                    // Використовуємо CoroutineScope замість Handler для розблокування з затримкою
                    scope.launch {
                        try {
                            delay(800) // Достатньо часу для завершення анімації
                            clickable.value = true
                        } catch (e: Exception) {
                            // Розблоковуємо в разі помилки щоб уникнути "вічного" блокування
                            clickable.value = true
                        }
                    }
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
            // Використовуємо умову з анімованим порогом для уникнення мерехтіння
            if (rotation > 90f) {
                // Лицьова сторона (з зображенням)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .graphicsLayer {
                            rotationY = 180f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Використовуємо AsyncImage замість Image для ефективнішого завантаження
                    AsyncImage(
                        model = card.imageRes,
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
                    // Використовуємо AsyncImage для зворотної сторони також
                    AsyncImage(
                        model = R.drawable.card_back,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(0.8f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
