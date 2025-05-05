package com.example.memogame.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.memogame.model.Level
import com.example.memogame.util.formatTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelItem(
    level: Level,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = level.name,
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "Кількість карток: ${level.cardCount}")

            Spacer(modifier = Modifier.height(8.dp))

            Row {
                for (i in 1..3) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (i <= level.stars) Color(0xFFFFD700) else Color.LightGray,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }

            if (level.bestTime > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Найкращий час: ${formatTime(level.bestTime)}")
            }
        }
    }
}