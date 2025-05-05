package com.example.memogame.model

data class Level(
    val id: Int,
    val name: String,
    val cardCount: Int,
    val stars: Int = 0,
    val bestTime: Long = 0
)