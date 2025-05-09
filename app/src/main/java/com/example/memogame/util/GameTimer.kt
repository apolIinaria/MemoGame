package com.example.memogame.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class GameTimer(private val scope: CoroutineScope) {
    private var job: Job? = null
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime

    private val isRunning = AtomicBoolean(false)
    private val isReset = AtomicBoolean(true)

    private var startTime: Long = 0

    fun start() {
        if (isRunning.getAndSet(true)) {
            stop()
        }

        isReset.set(false)
        startTime = System.currentTimeMillis()

        job = scope.launch {
            try {
                while (isActive && isRunning.get() && !isReset.get()) {
                    _elapsedTime.value = System.currentTimeMillis() - startTime
                    delay(100)
                }
            } catch (e: Exception) {
                println("Error in timer: ${e.message}")
                isRunning.set(false)
            }
        }
    }

    fun stop(): Long {
        isRunning.set(false)
        job?.cancel()
        job = null
        return _elapsedTime.value
    }

    fun reset() {
        isRunning.set(false)
        isReset.set(true)
        job?.cancel()
        job = null
        _elapsedTime.value = 0L
    }

    fun isActive(): Boolean {
        return isRunning.get() && job?.isActive == true && !isReset.get()
    }

    fun pause() {
        isRunning.set(false)
        job?.cancel()
        job = null
    }

    fun resume() {
        if (!isRunning.getAndSet(true) && !isReset.get()) {
            val currentTime = _elapsedTime.value
            startTime = System.currentTimeMillis() - currentTime

            job = scope.launch {
                try {
                    while (isActive && isRunning.get() && !isReset.get()) {
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