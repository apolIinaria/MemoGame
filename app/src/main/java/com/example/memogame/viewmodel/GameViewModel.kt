package com.example.memogame.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.memogame.model.Card
import com.example.memogame.model.Level
import com.example.memogame.data.repository.GameRepository
import com.example.memogame.util.CardGenerator
import com.example.memogame.util.GameTimer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GameViewModel(
    private val repository: GameRepository,
    internal val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val levelId: Int = savedStateHandle.get<Int>("levelId") ?: 1
    private val gameTimer = GameTimer(viewModelScope)

    val level: StateFlow<Level?> = repository.getLevel(levelId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val elapsedTime = gameTimer.elapsedTime

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards = _cards.asStateFlow()

    private val _isShowingCards = MutableStateFlow(true)
    val isShowingCards = _isShowingCards.asStateFlow()

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished = _gameFinished.asStateFlow()

    private val _stars = MutableStateFlow(0)
    val stars = _stars.asStateFlow()

    private var firstCard: Card? = null
    private var secondCard: Card? = null
    private var canFlip = false

    init {
        initGame()
    }

    private fun initGame() {
        viewModelScope.launch {
            level.collect { level ->
                if (level != null) {
                    _cards.value = CardGenerator.generateCards(level.cardCount)

                    // Показуємо картки на початку для запам'ятовування
                    _isShowingCards.value = true
                    delay(3000) // Показуємо картки на 3 секунди
                    _isShowingCards.value = false

                    // Перевертаємо всі картки і запускаємо таймер
                    _cards.value = _cards.value.map { it.copy(isFlipped = false) }
                    canFlip = true
                    gameTimer.start()
                }
            }
        }
    }

    fun onCardClick(card: Card) {
        if (!canFlip || card.isMatched || card.isFlipped) return

        val updatedCards = _cards.value.toMutableList()
        val clickedCardIndex = updatedCards.indexOfFirst { it.id == card.id }

        if (clickedCardIndex != -1) {
            // Перевертаємо картку
            val updatedCard = updatedCards[clickedCardIndex].copy(isFlipped = true)
            updatedCards[clickedCardIndex] = updatedCard

            when {
                firstCard == null -> {
                    firstCard = updatedCard
                }

                secondCard == null && firstCard?.id != updatedCard.id -> {
                    secondCard = updatedCard

                    // Перевіряємо чи пара співпадає
                    checkMatch(updatedCards)
                }
            }

            _cards.value = updatedCards
        }
    }

    private fun checkMatch(cards: MutableList<Card>) {
        // Тимчасово забороняємо перевертати картки
        canFlip = false

        viewModelScope.launch {
            delay(500) // Затримка для показу другої картки

            if (firstCard?.imageRes == secondCard?.imageRes) {
                // Картки співпадають
                val updatedCards = cards.map {
                    if (it.id == firstCard?.id || it.id == secondCard?.id) {
                        it.copy(isMatched = true)
                    } else {
                        it
                    }
                }
                _cards.value = updatedCards

                // Перевіряємо чи гра закінчена
                if (updatedCards.all { it.isMatched }) {
                    finishGame()
                }
            } else {
                // Картки не співпадають, перевертаємо їх назад
                val updatedCards = cards.map {
                    if (it.id == firstCard?.id || it.id == secondCard?.id) {
                        it.copy(isFlipped = false)
                    } else {
                        it
                    }
                }
                _cards.value = updatedCards
            }

            // Скидаємо вибрані картки
            firstCard = null
            secondCard = null

            // Дозволяємо перевертати картки знову
            canFlip = true
        }
    }

    private fun finishGame() {
        val finishTime = gameTimer.stop()
        _gameFinished.value = true

        // Обчислюємо кількість зірок на основі часу
        level.value?.let { level ->
            val stars = calculateStars(finishTime, level.cardCount)
            _stars.value = stars

            // Зберігаємо результат
            viewModelScope.launch {
                repository.updateLevelScore(levelId, stars, finishTime)
            }
        }
    }

    private fun calculateStars(time: Long, cardCount: Int): Int {
        // Різні часові пороги для різних рівнів складності
        val thresholdBase = cardCount * 1000L // Базовий час у мілісекундах

        return when {
            time < thresholdBase -> 3 // Найшвидший час - 3 зірки
            time < thresholdBase * 1.5 -> 2 // Середній час - 2 зірки
            else -> 1 // Повільний час - 1 зірка
        }
    }

    fun restartGame() {
        gameTimer.reset()
        _gameFinished.value = false
        _stars.value = 0
        firstCard = null
        secondCard = null
        initGame()
    }

    class Factory(
        private val repository: GameRepository,
        private val savedStateHandle: SavedStateHandle
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(
                    repository = repository,
                    savedStateHandle = savedStateHandle
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}