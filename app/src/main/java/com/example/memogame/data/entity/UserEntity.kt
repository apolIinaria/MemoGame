package com.example.memogame.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // Оскільки гра для одного користувача, використовуємо фіксований ID
    val name: String = "Player",
    val totalStars: Int = 0
)