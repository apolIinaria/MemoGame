package com.example.memogame.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GameTimer(private val scope: CoroutineScope) {
    private var job: Job? = null
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    fun start() {
        if (job == null) {
            job = scope.launch {
                val startTime = System.currentTimeMillis()
                while (true) {
                    _elapsedTime.value = System.currentTimeMillis() - startTime
                    delay(100) // Оновлюємо кожні 100мс
                }
            }
        }
    }

    fun stop(): Long {
        job?.cancel()
        job = null
        return _elapsedTime.value
    }

    fun reset() {
        job?.cancel()
        job = null
        _elapsedTime.value = 0L
    }
}