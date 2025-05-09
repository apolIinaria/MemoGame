package com.example.memogame.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memogame.MemoGameApplication
import com.example.memogame.ui.components.CardItem
import com.example.memogame.ui.components.StarRating
import com.example.memogame.ui.theme.MemoGameTheme
import com.example.memogame.util.formatTime
import com.example.memogame.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    levelId: Int,
    onBack: () -> Unit,
    application: MemoGameApplication
) {
    val viewModel: GameViewModel = viewModel(
        factory = GameViewModel.Factory(
            repository = application.repository,
            savedStateHandle = SavedStateHandle(mapOf("levelId" to levelId))
        )
    )

    val cards by viewModel.cards.collectAsState()
    val isShowingCards by viewModel.isShowingCards.collectAsState()
    val gameFinished by viewModel.gameFinished.collectAsState()
    val stars by viewModel.stars.collectAsState()
    val elapsedTime by viewModel.elapsedTime.collectAsState()
    val level by viewModel.level.collectAsState()
    val moves by viewModel.moves.collectAsState()

    var showExitDialog by remember { mutableStateOf(false) }

    // Отримуємо розміри екрану для адаптивного розміщення
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Встановлюємо levelId в SavedStateHandle
    LaunchedEffect(levelId) {
        viewModel.savedStateHandle["levelId"] = levelId
    }

    MemoGameTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = level?.name ?: "Гра",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { showExitDialog = true }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Назад",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.restartGame() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Почати заново",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    // Зменшена інформаційна панель з часом та прогресом
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Час: ${formatTime(elapsedTime)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Ходи: $moves",
                                    fontSize = 12.sp
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val matchedCards = cards.count { it.isMatched }
                                val totalCards = cards.size

                                Text(
                                    text = "Знайдено: $matchedCards/$totalCards",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )

                                // Прогрес-бар з правильним обчисленням прогресу
                                if (totalCards > 0) {
                                    val progress = matchedCards.toFloat() / totalCards.toFloat()
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Визначаємо кількість колонок і рядків для кожного рівня
                    val (columns, rows) = when (levelId) {
                        1 -> Pair(3, 2) // 6 карток: 2 рядки x 3 стовпці
                        2 -> Pair(4, 3) // 12 карток: 3 рядки x 4 стовпці
                        3 -> Pair(4, 4) // 16 карток: 4 рядки x 4 стовпці
                        4 -> Pair(5, 4) // 20 карток: 4 рядки x 5 стовпців
                        5 -> Pair(6, 4) // 24 картки: 4 рядки x 6 стовпців
                        else -> Pair(3, 2) // За замовчуванням
                    }

                    // Обчислюємо доступний простір з урахуванням інформаційної панелі
                    val availableHeight = screenHeight - 170.dp // Приблизна висота панелей та інформації
                    val availableWidth = screenWidth - 16.dp // З урахуванням відступів

                    // Відступи між картками
                    val horizontalSpacing = 4.dp
                    val verticalSpacing = 4.dp

                    // Обчислюємо розмір картки, який підходить для даного рівня
                    val cardWidth = (availableWidth - horizontalSpacing * (columns - 1)) / columns
                    val cardHeight = (availableHeight - verticalSpacing * (rows - 1)) / rows

                    // Обираємо менший розмір для створення квадратних карток
                    val cardSize = if (cardWidth < cardHeight) cardWidth else cardHeight

                    // Сітка карток з адаптивним розміщенням
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
                        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                        contentPadding = PaddingValues(
                            bottom = if (gameFinished) 0.dp else 8.dp
                        )
                    ) {
                        items(cards) { card ->
                            CardItem(
                                card = card,
                                onClick = { viewModel.onCardClick(card) },
                                modifier = Modifier.size(cardSize)
                            )
                        }
                    }
                }

                // Оверлей для показу карток на початку гри (повністю прозорий)
                AnimatedVisibility(
                    visible = isShowingCards,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Залишаємо Box повністю прозорим, без тексту та затемнення
                    Box(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Оверлей для результатів гри
                AnimatedVisibility(
                    visible = gameFinished,
                    enter = fadeIn(animationSpec = tween(durationMillis = 500)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Використовуємо звичайне порівняння для розміру карточки результатів
                        val resultWidth = if (280.dp < screenWidth * 0.8f) 280.dp else screenWidth * 0.8f

                        Card(
                            modifier = Modifier
                                .width(resultWidth)
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Вітаємо!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Рівень успішно завершено",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Час:")
                                    Text(
                                        text = formatTime(elapsedTime),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Ходи:")
                                    Text(
                                        text = "$moves",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                StarRating(
                                    stars = stars,
                                    maxStars = 3,
                                    starSize = 20.dp,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = viewModel::restartGame,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text("Грати знову")
                                    }

                                    Button(
                                        onClick = onBack,
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Text("До рівнів")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Діалог виходу з гри
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Вийти з гри?") },
                text = { Text("Ви впевнені, що хочете вийти? Прогрес у поточній грі буде втрачено.") },
                confirmButton = {
                    Button(
                        onClick = onBack,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Так, вийти")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showExitDialog = false }) {
                        Text("Продовжити гру")
                    }
                }
            )
        }
    }
}