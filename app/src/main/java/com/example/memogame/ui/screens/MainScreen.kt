package com.example.memogame.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.memogame.MemoGameApplication
import com.example.memogame.ui.theme.PinkDark
import com.example.memogame.ui.theme.PinkLight
import com.example.memogame.ui.theme.PinkPrimary
import com.example.memogame.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartClick: () -> Unit,
    application: MemoGameApplication
) {
    val viewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory(application.repository)
    )

    var isMusicEnabled by remember { mutableStateOf(application.audioManager.isMusicEnabled()) }

    val user by viewModel.user.collectAsState()
    val totalLevels by viewModel.totalLevels.collectAsState()
    val completedLevels by viewModel.completedLevels.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showNameEditorDialog by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }

    val context = LocalContext.current

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            PinkLight.copy(alpha = 0.4f),
            Color.White.copy(alpha = 0.9f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                PinkPrimary,
                                PinkDark
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Логотип гри",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Мемо Гра",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PinkDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Тренуйте свою пам'ять!",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Вітаємо, ${user.name}!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        IconButton(
                            onClick = {
                                newUserName = user.name
                                showNameEditorDialog = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Змінити ім'я",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Зібрано зірок:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = "${user.totalStars}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Пройдено рівнів:",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )

                            Text(
                                text = "$completedLevels/$totalLevels",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    "Почати гру",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                onClick = { showAboutDialog = true },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Про гру",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    "Про гру",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ElevatedButton(
                onClick = {
                    isMusicEnabled = !isMusicEnabled
                    application.audioManager.setMusicEnabled(isMusicEnabled)
                },
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    imageVector = if (isMusicEnabled) Icons.Default.MusicNote else Icons.Default.MusicOff,
                    contentDescription = if (isMusicEnabled) "Вимкнути музику" else "Увімкнути музику",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (isMusicEnabled) "Вимкнути музику" else "Увімкнути музику",
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Скинути прогрес"
                )

                Spacer(modifier = Modifier.width(4.dp))

                Text("Скинути прогрес")
            }
        }

        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Про додаток",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { showAboutDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрити"
                            )
                        }
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Мемо Гра - це захоплююча гра на тренування пам'яті.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "Версія: 1.0",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "Розробник: Новомлинець Поліна та Болобан Софія",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "© 2025 Всі права захищені",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Правила гри:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "1. Запам'ятайте розташування карток.\n" +
                                    "2. Знайдіть пари однакових карток.\n" +
                                    "3. Чим швидше ви знайдете всі пари, тим більше зірок отримаєте.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showAboutDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Зрозуміло")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        if (showResetDialog) {
            var isResetting by remember { mutableStateOf(false) }

            AlertDialog(
                onDismissRequest = {
                    if (!isResetting) showResetDialog = false
                },
                title = { Text("Скинути прогрес?") },
                text = {
                    Column {
                        Text("Ви дійсно хочете скинути весь прогрес гри? Цю дію неможливо скасувати.")

                        if (isResetting) {
                            Spacer(modifier = Modifier.height(16.dp))

                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Скидання...",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            isResetting = true

                            application.resetDatabase {
                                isResetting = false
                                showResetDialog = false

                                Toast.makeText(
                                    context,
                                    "Прогрес скинуто успішно",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = !isResetting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Так, скинути")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showResetDialog = false },
                        enabled = !isResetting
                    ) {
                        Text("Скасувати")
                    }
                }
            )
        }

        if (showNameEditorDialog) {
            AlertDialog(
                onDismissRequest = { showNameEditorDialog = false },
                title = { Text("Змінити ім'я") },
                text = {
                    Column {
                        Text(
                            "Введіть нове ім'я:",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = newUserName,
                            onValueChange = { newUserName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Ім'я") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (newUserName.isNotBlank()) {
                                        viewModel.updateUserName(newUserName)
                                        showNameEditorDialog = false
                                    }
                                }
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newUserName.isNotBlank()) {
                                viewModel.updateUserName(newUserName)
                                showNameEditorDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Зберегти")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showNameEditorDialog = false }) {
                        Text("Скасувати")
                    }
                }
            )
        }
    }
}