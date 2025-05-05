package com.example.memogame.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun StarRating(
    stars: Int,
    maxStars: Int = 3,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        for (i in 1..maxStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (i <= stars) Color(0xFFFFD700) else Color.LightGray,
                modifier = Modifier.width(24.dp)
            )
        }
    }
}