package com.example.memogame.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "levels")
data class LevelEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val cardCount: Int,
    val stars: Int = 0,
    val bestTime: Long = 0
)