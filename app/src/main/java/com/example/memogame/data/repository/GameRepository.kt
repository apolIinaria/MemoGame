package com.example.memogame.data.repository

import com.example.memogame.data.dao.LevelDao
import com.example.memogame.data.dao.UserDao
import com.example.memogame.data.entity.LevelEntity
import com.example.memogame.data.entity.UserEntity
import com.example.memogame.model.Level
import kotlinx.coroutines.flow.Flow
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
        levelDao.updateLevelScore(levelId, stars, time)
        if (stars > 0) {
            userDao.addStars(stars)
        }
    }
}