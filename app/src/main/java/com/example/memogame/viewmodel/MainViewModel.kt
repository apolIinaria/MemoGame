package com.example.memogame.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memogame.data.entity.UserEntity
import com.example.memogame.data.repository.GameRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val repository: GameRepository) : ViewModel() {
    val user: StateFlow<UserEntity> = repository.user.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        UserEntity()
    )

    // Додаємо інформацію про пройдені рівні
    val totalLevels: StateFlow<Int> = repository.levels.map { levels ->
        levels.size
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    val completedLevels: StateFlow<Int> = repository.levels.map { levels ->
        levels.count { it.stars > 0 }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0
    )

    class Factory(private val repository: GameRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}