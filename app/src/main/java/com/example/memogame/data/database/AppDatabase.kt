package com.example.memogame.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.memogame.data.dao.LevelDao
import com.example.memogame.data.dao.UserDao
import com.example.memogame.data.entity.LevelEntity
import com.example.memogame.data.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class, LevelEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun levelDao(): LevelDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "memo_game_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Ініціалізація початкових даних
                            scope.launch(Dispatchers.IO) {
                                val levelDao = getDatabase(context, scope).levelDao()
                                val userDao = getDatabase(context, scope).userDao()

                                // Створюємо користувача
                                userDao.insertUser(UserEntity())

                                // Створюємо рівні
                                val levels = listOf(
                                    LevelEntity(1, "Початківець", 6),
                                    LevelEntity(2, "Легкий", 8),
                                    LevelEntity(3, "Середній", 12),
                                    LevelEntity(4, "Складний", 16),
                                    LevelEntity(5, "Експерт", 20)
                                )
                                levelDao.insertAllLevels(levels)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}