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

                            scope.launch(Dispatchers.IO) {
                                val levelDao = getDatabase(context, scope).levelDao()
                                val userDao = getDatabase(context, scope).userDao()

                                userDao.insertUser(UserEntity())

                                val levels = listOf(
                                    LevelEntity(1, "Початківець", 6),
                                    LevelEntity(2, "Легкий", 12),
                                    LevelEntity(3, "Середній", 16),
                                    LevelEntity(4, "Складний", 20),
                                    LevelEntity(5, "Експерт", 24)
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

        fun clearInstance() {
            INSTANCE = null
        }
    }
}
