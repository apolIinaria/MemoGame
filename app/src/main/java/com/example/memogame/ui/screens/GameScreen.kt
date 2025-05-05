package com.example.memogame.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memogame.MemoGameApplication
import com.example.memogame.model.Card
import com.example.memogame.ui.components.CardItem
import com.example.memogame.ui.components.StarRating
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

    var showExitDialog by remember { mutableStateOf(false) }

    // Встановлюємо levelId в SavedStateHandle
    LaunchedEffect(levelId) {
        viewModel.savedStateHandle["levelId"] = levelId
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(level?.name ?: "Гра") },
                navigationIcon = {
                    IconButton(onClick = { showExitDialog = true }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Інформація про гру
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Час: ${formatTime(elapsedTime)}",
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Картки: ${if (cards.isNotEmpty()) cards.count { it.isMatched } else 0}/${cards.size}",
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Сітка карток
            val columns = when (cards.size) {
                6 -> 3 // 3x2
                8 -> 4 // 4x2
                12 -> 4 // 4x3
                16 -> 4 // 4x4
                20 -> 5 // 5x4
                else -> 4
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards) { card ->
                    CardItem(
                        card = card,
                        onClick = { viewModel.onCardClick(card) }
                    )
                }
            }
        }

        // Діалог результатів гри
        if (gameFinished) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Гру завершено!") },
                text = {
                    Column {
                        Text("Ви завершили рівень!")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Ваш час: ${formatTime(elapsedTime)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        StarRating(stars = stars)
                    }
                },
                confirmButton = {
                    Button(onClick = viewModel::restartGame) {
                        Text("Грати знову")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBack) {
                        Text("Назад до рівнів")
                    }
                }
            )
        }

        // Діалог виходу з гри
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Вийти з гри?") },
                text = { Text("Ви впевнені, що хочете вийти? Прогрес у поточній грі буде втрачено.") },
                confirmButton = {
                    Button(onClick = onBack) {
                        Text("Так, вийти")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Продовжити гру")
                    }
                }
            )
        }
    }
}