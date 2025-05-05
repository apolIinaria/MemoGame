package com.example.memogame.util

fun formatTime(timeInMillis: Long): String {
    val minutes = (timeInMillis / 60000).toInt()
    val seconds = ((timeInMillis % 60000) / 1000).toInt()
    val millis = ((timeInMillis % 1000) / 10).toInt()

    return String.format("%02d:%02d.%02d", minutes, seconds, millis)
}