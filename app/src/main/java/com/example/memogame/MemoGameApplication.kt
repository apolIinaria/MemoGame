package com.example.memogame

import android.app.Application
import android.content.Context
import android.widget.Toast
import com.example.memogame.data.database.AppDatabase
import com.example.memogame.data.repository.GameRepository
import com.example.memogame.util.CardGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MemoGameApplication : Application() {
    // Використовуємо SupervisorJob для кращої обробки помилок
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Лінива ініціалізація бази даних
    val database by lazy {
        try {
            AppDatabase.getDatabase(this, applicationScope)
        } catch (e: Exception) {
            // Логування помилки та показ повідомлення
            showErrorToast("Помилка ініціалізації бази даних")
            throw e
        }
    }

    // Лінива ініціалізація репозиторію
    val repository by lazy {
        try {
            GameRepository(database.userDao(), database.levelDao())
        } catch (e: Exception) {
            // Логування помилки та показ повідомлення
            showErrorToast("Помилка ініціалізації репозиторію")
            throw e
        }
    }

    override fun onCreate() {
        super.onCreate()

        // Скидаємо генератор ID при запуску програми
        CardGenerator.resetIdGenerator()

        // Попередня ініціалізація бази даних у фоні
        applicationScope.launch {
            try {
                preloadDatabase()
            } catch (e: Exception) {
                println("Error preloading database: ${e.message}")
            }
        }
    }

    /**
     * Попередньо завантажує базу даних для швидшого доступу в майбутньому
     */
    private suspend fun preloadDatabase() {
        // Доступ до бази даних для ініціалізації
        database.openHelper.writableDatabase
    }

    /**
     * Показує повідомлення про помилку користувачу
     */
    private fun showErrorToast(message: String) {
        try {
            Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // Ігноруємо помилки при показі повідомлення
            println("Error showing toast: ${e.message}")
        }
    }

    /**
     * Отримує репозиторій з контексту
     */
    companion object {
        fun getRepository(context: Context): GameRepository {
            return (context.applicationContext as MemoGameApplication).repository
        }
    }
}