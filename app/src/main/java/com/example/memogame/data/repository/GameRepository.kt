package com.example.memogame.data.repository

import com.example.memogame.data.dao.LevelDao
import com.example.memogame.data.dao.UserDao
import com.example.memogame.data.entity.LevelEntity
import com.example.memogame.data.entity.UserEntity
import com.example.memogame.model.Level
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class GameRepository(
    private val userDao: UserDao,
    private val levelDao: LevelDao
) {
    // Користувач
    val user: Flow<UserEntity> = userDao.getUser()

    // Рівні
    val levels: Flow<List<Level>> = levelDao.getAllLevels().map { levelEntities ->
        levelEntities.map { entity ->
            Level(
                id = entity.id,
                name = entity.name,
                cardCount = entity.cardCount,
                stars = entity.stars,
                bestTime = entity.bestTime
            )
        }
    }

    fun getLevel(levelId: Int): Flow<Level> = levelDao.getLevel(levelId).map { entity ->
        Level(
            id = entity.id,
            name = entity.name,
            cardCount = entity.cardCount,
            stars = entity.stars,
            bestTime = entity.bestTime
        )
    }

    suspend fun updateLevelScore(levelId: Int, stars: Int, time: Long) {
        // Отримуємо поточний рівень
        val currentLevel = levelDao.getLevel(levelId).first()

        // Обчислюємо, скільки нових зірок було отримано
        val newStars = stars - currentLevel.stars

        // Оновлюємо рівень лише якщо результат кращий
        // (це забезпечується умовою в SQL-запиті у LevelDao)
        val updated = levelDao.updateLevelScore(levelId, stars, time)

        // Додаємо лише нові зірки до загального рахунку,
        // і тільки якщо результат реально покращився (updated > 0)
        if (newStars > 0 && updated > 0) {
            userDao.addStars(newStars)
        }
    }
}