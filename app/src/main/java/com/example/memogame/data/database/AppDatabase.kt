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

                                // Створюємо рівні з новою кількістю карток
                                val levels = listOf(
                                    LevelEntity(1, "Початківець", 6),   // 3 пари, 2x3
                                    LevelEntity(2, "Легкий", 12),       // 6 пар, 3x4
                                    LevelEntity(3, "Середній", 16),     // 8 пар, 4x4
                                    LevelEntity(4, "Складний", 20),     // 10 пар, 4x5
                                    LevelEntity(5, "Експерт", 24)       // 12 пар, 4x6
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

        /**
         * Очищає поточний інстанс бази даних
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
