package com.example.memogame.model

import androidx.compose.ui.Modifier

data class Card(
    val id: Int,
    val imageRes: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)