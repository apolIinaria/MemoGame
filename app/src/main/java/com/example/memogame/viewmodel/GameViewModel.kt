package com.example.memogame.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memogame.model.Card
import com.example.memogame.model.Level
import com.example.memogame.data.repository.GameRepository
import com.example.memogame.util.CardGenerator
import com.example.memogame.util.GameTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

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

    // Додаємо лічильник ходів
    private val _moves = MutableStateFlow(0)
    val moves = _moves.asStateFlow()

    // Змінні для відстеження перевернутих карток
    private var firstCard: Card? = null
    private var secondCard: Card? = null

    // Використовуємо AtomicBoolean для надійного блокування взаємодії
    private val isProcessingMove = AtomicBoolean(false)

    // Job для затримки перевороту карток назад
    private var flipBackJob: Job? = null

    init {
        initGame()
    }

    private fun initGame() {
        // Скасовуємо будь-які активні затримки
        flipBackJob?.cancel()

        // Скидаємо стани
        firstCard = null
        secondCard = null
        isProcessingMove.set(false)

        viewModelScope.launch {
            level.collect { level ->
                if (level != null) {
                    try {
                        // Генеруємо картки і скидаємо лічильники
                        _cards.value = CardGenerator.generateCards(level.cardCount)
                        _moves.value = 0

                        // Показуємо картки на початку для запам'ятовування
                        _isShowingCards.value = true

                        // Затримка для запам'ятовування карток залежно від їх кількості
                        val memorizeTime = when {
                            level.cardCount <= 8 -> 2000L // 2 секунди для невеликої кількості
                            level.cardCount <= 12 -> 3000L // 3 секунди для середньої кількості
                            else -> 4000L // 4 секунди для великої кількості (зменшено з 5)
                        }

                        delay(memorizeTime)
                        _isShowingCards.value = false

                        // Перевертаємо всі картки і запускаємо таймер
                        _cards.value = _cards.value.map { it.copy(isFlipped = false) }

                        // Запускаємо таймер після того, як картки перевернулись
                        delay(400) // Затримка для анімації перевороту
                        gameTimer.start()
                    } catch (e: Exception) {
                        // Обробка помилок генерації карток
                        println("Error initializing game: ${e.message}")
                    }
                }
            }
        }
    }

    fun onCardClick(card: Card) {
        // Перевіряємо можливість перевороту - використовуємо AtomicBoolean для безпечного доступу
        if (isProcessingMove.get() || card.isMatched || card.isFlipped || _gameFinished.value) {
            return
        }

        // Знаходимо індекс картки зі запобіжними перевірками
        val currentCards = _cards.value.toMutableList()
        val clickedCardIndex = currentCards.indexOfFirst { it.id == card.id }

        if (clickedCardIndex == -1 || clickedCardIndex >= currentCards.size) {
            return // Картка не знайдена або індекс за межами масиву
        }

        try {
            // Перевертаємо картку
            val updatedCard = currentCards[clickedCardIndex].copy(isFlipped = true)
            currentCards[clickedCardIndex] = updatedCard

            when {
                firstCard == null -> {
                    // Перша картка в парі
                    firstCard = updatedCard
                    _cards.value = currentCards
                }
                secondCard == null && firstCard?.id != updatedCard.id -> {
                    // Друга картка в парі - блокуємо подальші дії
                    secondCard = updatedCard
                    _moves.value = _moves.value + 1
                    _cards.value = currentCards

                    // Блокуємо взаємодію
                    isProcessingMove.set(true)

                    // Перевіряємо співпадіння з затримкою
                    viewModelScope.launch {
                        delay(500) // Затримка для показу другої картки
                        checkMatch()
                    }
                }
            }
        } catch (e: Exception) {
            // Обробка можливих помилок
            println("Error handling card click: ${e.message}")
            // Скидаємо стан у разі помилки
            isProcessingMove.set(false)
        }
    }

    private fun checkMatch() {
        try {
            val currentCards = _cards.value.toMutableList()

            if (firstCard?.imageRes == secondCard?.imageRes) {
                // Картки співпадають - позначаємо їх як знайдені
                val updatedCards = currentCards.map {
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
                } else {
                    // Скидаємо вибрані картки
                    firstCard = null
                    secondCard = null

                    // Розблоковуємо взаємодію
                    viewModelScope.launch {
                        delay(300) // Невелика затримка для плавності
                        isProcessingMove.set(false)
                    }
                }
            } else {
                // Картки не співпадають - перевертаємо їх назад
                // Використовуємо Job для можливості скасувати операцію при рестарті
                flipBackJob = viewModelScope.launch {
                    delay(500) // Затримка перед переворотом

                    val updatedCards = currentCards.map {
                        if (it.id == firstCard?.id || it.id == secondCard?.id) {
                            it.copy(isFlipped = false)
                        } else {
                            it
                        }
                    }
                    _cards.value = updatedCards

                    // Скидаємо вибрані картки
                    firstCard = null
                    secondCard = null

                    // Розблоковуємо взаємодію з невеликою затримкою
                    delay(300)
                    isProcessingMove.set(false)
                }
            }
        } catch (e: Exception) {
            // Обробка можливих помилок
            println("Error checking card match: ${e.message}")
            // Скидаємо стан у разі помилки
            firstCard = null
            secondCard = null
            isProcessingMove.set(false)
        }
    }

    private fun finishGame() {
        val finishTime = gameTimer.stop()
        _gameFinished.value = true

        // Обчислюємо кількість зірок на основі часу та кількості ходів
        level.value?.let { level ->
            val stars = calculateStars(finishTime, _moves.value, level.cardCount)
            _stars.value = stars

            // Зберігаємо результат
            viewModelScope.launch {
                try {
                    repository.updateLevelScore(levelId, stars, finishTime)
                } catch (e: Exception) {
                    println("Error updating level score: ${e.message}")
                }
            }
        }
    }

    private fun calculateStars(time: Long, moves: Int, cardCount: Int): Int {
        // Базовий час і ходи для різних рівнів складності
        val pairsCount = cardCount / 2
        val optimalMoves = pairsCount * 2 // Оптимальна кількість ходів приблизно дорівнює кількості пар

        // Оцінка за часом (з урахуванням складності)
        val timeWeight = 0.6 // Ваговий коефіцієнт для часу
        val timeThreshold = pairsCount * 2000L // Базовий час у мілісекундах
        val timeRating = when {
            time < timeThreshold -> 3
            time < timeThreshold * 1.5 -> 2
            else -> 1
        }

        // Оцінка за ходами
        val movesWeight = 0.4 // Ваговий коефіцієнт для ходів
        val movesRating = when {
            moves <= optimalMoves -> 3
            moves <= optimalMoves * 1.5 -> 2
            else -> 1
        }

        // Зважена оцінка (округлена до цілого)
        return (timeRating * timeWeight + movesRating * movesWeight).toInt().coerceIn(1, 3)
    }

    fun restartGame() {
        gameTimer.reset()
        _gameFinished.value = false
        _stars.value = 0
        _moves.value = 0

        // Скасовуємо активну затримку перевороту карток, якщо така є
        flipBackJob?.cancel()

        // Скидаємо стани
        firstCard = null
        secondCard = null
        isProcessingMove.set(false)

        // Запускаємо нову гру
        initGame()
    }

    override fun onCleared() {
        super.onCleared()
        // Скасовуємо активні задачі при знищенні ViewModel
        flipBackJob?.cancel()
        gameTimer.reset()
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