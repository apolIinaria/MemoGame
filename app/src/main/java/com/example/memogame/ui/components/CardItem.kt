package com.example.memogame.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Color
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
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenWidthDp < 400

    val elevation = if (card.isMatched) 1.dp else if (isSmallScreen) 2.dp else 4.dp

    val aspectRatio = 0.9f

    val clickable = rememberSaveable { mutableStateOf(true) }

    val scope = remember { CoroutineScope(SupervisorJob() + Dispatchers.Main) }

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

    val rotation by animateFloatAsState(
        targetValue = if (card.isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "card_rotation"
    )

    val alpha = if (card.isMatched) 0.7f else 1.0f

    val scale by animateFloatAsState(
        targetValue = if (card.isFlipped && !card.isMatched) 1.05f else 1.0f,
        animationSpec = tween(150),
        label = "card_scale"
    )

    val cardModifier = modifier
        .aspectRatio(aspectRatio)
        .shadow(elevation, shape = CardDefaults.shape)
        .zIndex(if (card.isFlipped) 1f else 0f)
        .graphicsLayer {
            rotationY = rotation
            scaleX = scale
            scaleY = scale
            cameraDistance = 12f * density
            this.alpha = alpha
        }
        .clickable(
            enabled = !card.isMatched && !card.isFlipped && clickable.value,
            onClick = {
                if (clickable.value) {
                    clickable.value = false

                    onClick()

                    scope.launch {
                        try {
                            delay(800)
                            clickable.value = true
                        } catch (e: Exception) {
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
            containerColor = Color(0xFFFFB4CC)
        ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (rotation > 90f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .graphicsLayer {
                            rotationY = 180f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = card.imageRes,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = R.drawable.card_back,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.FillBounds
                    )
                }
            }
        }
    }
}