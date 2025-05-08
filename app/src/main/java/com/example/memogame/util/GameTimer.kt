package com.example.memogame.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Клас для відстеження ігрового часу з покращеною обробкою помилок і потокобезпечністю
 */
class GameTimer(private val scope: CoroutineScope) {
    private var job: Job? = null
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    // Прапорець для безпечного керування станом таймера
    private val isRunning = AtomicBoolean(false)

    // Час початку для розрахунку
    private var startTime: Long = 0

    /**
     * Запускає таймер або перезапускає його, якщо вже запущений
     */
    fun start() {
        // Якщо таймер вже запущений, спочатку зупиняємо його
        if (isRunning.getAndSet(true)) {
            stop()
        }

        startTime = System.currentTimeMillis()

        job = scope.launch {
            try {
                while (isActive && isRunning.get()) {
                    _elapsedTime.value = System.currentTimeMillis() - startTime
                    delay(100) // Оновлюємо кожні 100мс
                }
            } catch (e: Exception) {
                // Обробка помилок корутини
                println("Error in timer: ${e.message}")
                isRunning.set(false)
            }
        }
    }

    /**
     * Зупиняє таймер і повертає поточний час
     * @return Час у мілісекундах
     */
    fun stop(): Long {
        isRunning.set(false)
        job?.cancel()
        job = null
        return _elapsedTime.value
    }

    /**
     * Скидає таймер до нуля
     */
    fun reset() {
        isRunning.set(false)
        job?.cancel()
        job = null
        _elapsedTime.value = 0L
    }

    /**
     * Перевіряє, чи таймер активний
     * @return true якщо таймер запущений
     */
    fun isActive(): Boolean {
        return isRunning.get() && job?.isActive == true
    }

    /**
     * Призупиняє таймер без скидання часу
     */
    fun pause() {
        isRunning.set(false)
        job?.cancel()
        job = null
    }

    /**
     * Відновлює таймер з поточного значення
     */
    fun resume() {
        if (!isRunning.getAndSet(true)) {
            val currentTime = _elapsedTime.value
            startTime = System.currentTimeMillis() - currentTime

            job = scope.launch {
                try {
                    while (isActive && isRunning.get()) {
                        _elapsedTime.value = System.currentTimeMillis() - startTime
                        delay(100)
                    }
                } catch (e: Exception) {
                    println("Error in timer: ${e.message}")
                    isRunning.set(false)
                }
            }
        }
    }
}