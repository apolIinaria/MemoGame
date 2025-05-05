package com.example.memogame

import android.app.Application
import com.example.memogame.data.database.AppDatabase
import com.example.memogame.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MemoGameApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { GameRepository(database.userDao(), database.levelDao()) }
}