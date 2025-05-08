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

    // Визначаємо кількість колонок залежно від розміру екрану та кількості карток
    val columns = when {
        cards.size <= 8 && screenWidth < 400.dp -> 2
        cards.size <= 12 && screenWidth < 400.dp -> 3
        cards.size > 12 && screenWidth < 400.dp -> 3 // Менше колонок для малих екранів
        cards.size > 16 -> 4
        else -> 4
    }

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
                        .padding(8.dp) // Менші відступи для маленьких екранів
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
                                .padding(8.dp), // Менші відступи
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Час: ${formatTime(elapsedTime)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp // Менший шрифт
                                )
                                Text(
                                    text = "Ходи: $moves",
                                    fontSize = 12.sp // Менший шрифт
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Знайдено: ${if (cards.isNotEmpty()) cards.count { it.isMatched } / cards.size else 0}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp // Менший шрифт
                                )

                                // Прогрес-бар
                                if (cards.isNotEmpty()) {
                                    val progress = cards.count { it.isMatched }.toFloat() / cards.size
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .width(80.dp) // Менша ширина
                                            .height(6.dp) // Менша висота
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Сітка карток з адаптивним розміщенням
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp), // Менші відступи
                        verticalArrangement = Arrangement.spacedBy(4.dp),   // Менші відступи
                        contentPadding = PaddingValues(
                            bottom = if (gameFinished) 0.dp else 8.dp
                        )
                    ) {
                        items(cards) { card ->
                            CardItem(
                                card = card,
                                onClick = { viewModel.onCardClick(card) }
                            )
                        }
                    }
                }

                // Оверлей для показу карток на початку гри (менш нав'язливий)
                AnimatedVisibility(
                    visible = isShowingCards,
                    enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 300)),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Компактне повідомлення
                        Text(
                            text = "Запам'ятайте!",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
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
                        Card(
                            modifier = Modifier
                                .width(280.dp) // Менша ширина для маленьких екранів
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
                                    starSize = 20.dp, // Менші зірки
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