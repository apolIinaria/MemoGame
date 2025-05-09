package com.example.memogame

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.example.memogame.data.database.AppDatabase
import com.example.memogame.data.repository.GameRepository
import com.example.memogame.util.AudioManager
import com.example.memogame.util.CardGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MemoGameApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy {
        try {
            AppDatabase.getDatabase(this, applicationScope)
        } catch (e: Exception) {
            showErrorToast("Помилка ініціалізації бази даних")
            throw e
        }
    }
    val repository by lazy {
        try {
            GameRepository(database.userDao(), database.levelDao())
        } catch (e: Exception) {
            showErrorToast("Помилка ініціалізації репозиторію")
            throw e
        }
    }

    val audioManager by lazy { AudioManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()

        CardGenerator.resetIdGenerator()

        applicationScope.launch {
            try {
                preloadDatabase()
            } catch (e: Exception) {
                println("Error preloading database: ${e.message}")
            }
        }
    }

    private suspend fun preloadDatabase() {
        database.openHelper.writableDatabase
    }

    private fun showErrorToast(message: String) {
        try {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            println("Error showing toast: ${e.message}")
        }
    }

    companion object {
        fun getRepository(context: Context): GameRepository {
            return (context.applicationContext as MemoGameApplication).repository
        }
    }

    fun resetDatabase(onComplete: () -> Unit) {
        applicationScope.launch(Dispatchers.IO) {
            try {
                if (database.isOpen) {
                    database.close()
                }

                applicationContext.deleteDatabase("memo_game_database")

                AppDatabase.clearInstance()

                val newDb = AppDatabase.getDatabase(applicationContext, applicationScope)

                launch(Dispatchers.Main) {
                    onComplete()
                }
            } catch (e: Exception) {
                println("Error resetting database: ${e.message}")
                e.printStackTrace()

                launch(Dispatchers.Main) {
                    onComplete()
                    showErrorToast("Помилка при скиданні бази даних")
                }
            }
        }
    }
}