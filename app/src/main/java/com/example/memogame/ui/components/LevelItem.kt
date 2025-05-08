package com.example.memogame.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memogame.model.Level
import com.example.memogame.util.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelItem(
    level: Level,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = { if (!isLocked) onClick() },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isLocked) 1.dp else 4.dp
        ),
        border = if (level.stars == 3)
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ліва частина з основною інформацією
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = level.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Кількість карток: ${level.cardCount}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Прогрес проходження
                if (level.stars > 0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Прогрес:",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        LinearProgressIndicator(
                            progress = { level.stars / 3f },
                            modifier = Modifier
                                .width(100.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = when (level.stars) {
                                3 -> Color(0xFFFFD700) // Gold
                                2 -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.secondary
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }

                if (level.bestTime > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Найкращий час: ${formatTime(level.bestTime)}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Права частина зі зірками або іконкою замка
            if (isLocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Заблоковано",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(48.dp)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    StarRating(
                        stars = level.stars,
                        maxStars = 3,
                        starSize = 24.dp
                    )

                    if (level.stars == 3) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Завершено",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}