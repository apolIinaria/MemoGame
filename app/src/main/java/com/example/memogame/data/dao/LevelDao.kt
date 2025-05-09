package com.example.memogame.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memogame.data.entity.LevelEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelDao {
    @Query("SELECT * FROM levels ORDER BY id")
    fun getAllLevels(): Flow<List<LevelEntity>>

    @Query("SELECT * FROM levels WHERE id = :levelId")
    fun getLevel(levelId: Int): Flow<LevelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLevel(level: LevelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLevels(levels: List<LevelEntity>)

    @Update
    suspend fun updateLevel(level: LevelEntity)

    @Query("UPDATE levels SET stars = :stars, bestTime = :time WHERE id = :levelId AND (stars < :stars OR (stars = :stars AND bestTime > :time))")
    suspend fun updateLevelScore(levelId: Int, stars: Int, time: Long): Int
}
