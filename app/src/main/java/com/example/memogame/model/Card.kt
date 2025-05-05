package com.example.memogame.model

data class Card(
    val id: Int,
    val imageRes: Int,
    var isFlipped: Boolean = false,
    var isMatched: Boolean = false
)