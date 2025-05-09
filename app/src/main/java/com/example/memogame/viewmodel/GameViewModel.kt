package com.example.memogame.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.memogame.model.Card
import com.example.memogame.model.Level
import com.example.memogame.data.repository.GameRepository
import com.example.memogame.util.AudioManager
import com.example.memogame.util.CardGenerator
import com.example.memogame.util.GameTimer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class GameViewModel(
    private val repository: GameRepository,
    private val audioManager: AudioManager,
    internal val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val levelId: Int = savedStateHandle.get<Int>("levelId") ?: 1
    private val gameTimer = GameTimer(viewModelScope)

    val level: StateFlow<Level?> = repository.getLevel(levelId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards = _cards.asStateFlow()

    private val _isShowingCards = MutableStateFlow(true)
    val isShowingCards = _isShowingCards.asStateFlow()

    private val _gameFinished = MutableStateFlow(false)
    val gameFinished = _gameFinished.asStateFlow()

    private val _gameCompleted = MutableStateFlow(false)
    val gameCompleted = _gameCompleted.asStateFlow()

    private val _stars = MutableStateFlow(0)
    val stars = _stars.asStateFlow()

    private val _moves = MutableStateFlow(0)
    val moves = _moves.asStateFlow()

    private val _finalMoves = MutableStateFlow(0)
    val finalMoves = _finalMoves.asStateFlow()

    private var firstCard: Card? = null
    private var secondCard: Card? = null

    private val isProcessingMove = AtomicBoolean(false)

    private val clickLock = AtomicBoolean(false)

    private var flipBackJob: Job? = null

    private var timerJob: Job? = null

    init {
        initGame()
        startTimerTracking()
    }

    private fun startTimerTracking() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            gameTimer.elapsedTime.collect { time ->
                if (!_gameCompleted.value) {
                    _elapsedTime.value = time
                }
            }
        }
    }

    private fun initGame() {
        flipBackJob?.cancel()

        firstCard = null
        secondCard = null
        isProcessingMove.set(false)
        clickLock.set(false)
        _gameCompleted.value = false

        viewModelScope.launch {
            try {
                level.collect { level ->
                    if (level != null) {
                        try {
                            _cards.value = CardGenerator.generateCards(level.cardCount)
                            _moves.value = 0
                            _finalMoves.value = 0

                            _isShowingCards.value = true

                            val memorizeTime = when {
                                level.cardCount <= 8 -> 2000L
                                level.cardCount <= 12 -> 3000L
                                else -> 4000L
                            }

                            delay(memorizeTime)

                            if (!isActive) return@collect

                            _isShowingCards.value = false

                            _cards.value = _cards.value.map { it.copy(isFlipped = false) }

                            delay(400)

                            if (!isActive) return@collect

                            gameTimer.start()
                        } catch (e: Exception) {
                            println("Помилка ініціалізації гри: ${e.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                println("Помилка колекції рівнів: ${e.message}")
            }
        }
    }

    fun onCardClick(card: Card) {
        if (_gameCompleted.value) return

        if (!clickLock.compareAndSet(false, true)) {
            return
        }

        audioManager.playCardFlipSound()

        viewModelScope.launch {
            delay(500)
            if (!_gameCompleted.value) {
                clickLock.set(false)
            }
        }

        synchronized(this) {
            if (isProcessingMove.get() || card.isMatched || card.isFlipped || _gameFinished.value) {
                return
            }

            isProcessingMove.set(true)
        }

        try {
            val currentCards = _cards.value.toMutableList()
            val clickedCardIndex = currentCards.indexOfFirst { it.id == card.id }

            if (clickedCardIndex == -1 || clickedCardIndex >= currentCards.size) {
                isProcessingMove.set(false)
                return
            }

            val updatedCard = currentCards[clickedCardIndex].copy(isFlipped = true)
            currentCards[clickedCardIndex] = updatedCard

            _moves.value = _moves.value + 1

            when {
                firstCard == null -> {

                    firstCard = updatedCard
                    _cards.value = currentCards

                    isProcessingMove.set(false)
                }
                secondCard == null && firstCard?.id != updatedCard.id -> {
                    secondCard = updatedCard
                    _cards.value = currentCards

                    viewModelScope.launch {
                        try {
                            delay(500)
                            if (!_gameCompleted.value) {
                                checkMatch()
                            }
                        } catch (e: Exception) {
                            println("Помилка в корутині перевірки співпадіння: ${e.message}")
                            firstCard = null
                            secondCard = null
                            isProcessingMove.set(false)
                        }
                    }
                }
                else -> {
                    isProcessingMove.set(false)
                }
            }
        } catch (e: Exception) {

            println("Помилка при обробці кліку по картці: ${e.message}")

            isProcessingMove.set(false)
        }
    }

    private fun checkMatch() {
        if (_gameCompleted.value) return

        try {
            val currentCards = _cards.value.toMutableList()

            if (firstCard?.imageRes == secondCard?.imageRes) {
                audioManager.playMatchSound()

                val updatedCards = currentCards.map {
                    if (it.id == firstCard?.id || it.id == secondCard?.id) {
                        it.copy(isMatched = true)
                    } else {
                        it
                    }
                }
                _cards.value = updatedCards

                if (updatedCards.all { it.isMatched }) {
                    println("Всі картки знайдено: ходи = ${_moves.value}")
                    // Відтворюємо звук перемоги
                    audioManager.playWinSound()
                    finishGame()
                } else {
                    firstCard = null
                    secondCard = null

                    viewModelScope.launch {
                        delay(300)
                        if (!_gameCompleted.value) {
                            isProcessingMove.set(false)
                        }
                    }
                }
            } else {
                flipBackJob?.cancel()

                flipBackJob = viewModelScope.launch {
                    try {
                        delay(500)

                        if (!isActive || _gameCompleted.value) return@launch

                        val updatedCards = currentCards.map {
                            if (it.id == firstCard?.id || it.id == secondCard?.id) {
                                it.copy(isFlipped = false)
                            } else {
                                it
                            }
                        }

                        _cards.value = updatedCards

                        firstCard = null
                        secondCard = null

                        delay(300)

                        if (isActive && !_gameCompleted.value) {
                            isProcessingMove.set(false)
                        }
                    } catch (e: Exception) {
                        println("Помилка в корутині перевороту карток: ${e.message}")
                        firstCard = null
                        secondCard = null
                        if (!_gameCompleted.value) {
                            isProcessingMove.set(false)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Помилка при перевірці співпадіння карток: ${e.message}")

            firstCard = null
            secondCard = null
            if (!_gameCompleted.value) {
                isProcessingMove.set(false)
            }
        }
    }

    private fun finishGame() {
        if (_gameCompleted.value) return

        val currentMoves = _moves.value

        _finalMoves.value = maxOf(currentMoves, 1)

        _gameCompleted.value = true

        val finishTime = gameTimer.stop()
        _elapsedTime.value = finishTime

        _gameFinished.value = true

        isProcessingMove.set(true)
        clickLock.set(true)

        flipBackJob?.cancel()
        timerJob?.cancel()

        viewModelScope.launch {
            try {
                delay(100)
                gameTimer.reset()

                if (_finalMoves.value <= 0) {
                    println("КОРЕКЦІЯ: finalMoves після затримки = ${_finalMoves.value}, встановлюємо = $currentMoves")
                    _finalMoves.value = maxOf(currentMoves, 1)
                }
            } catch (e: Exception) {
                println("Error resetting timer: ${e.message}")
            }
        }

        level.value?.let { level ->
            val stars = calculateStars(finishTime, maxOf(currentMoves, 1), level.cardCount)
            _stars.value = stars

            viewModelScope.launch {
                try {
                    updateLevelScore(levelId, stars, finishTime)
                } catch (e: Exception) {
                    println("Error updating level score: ${e.message}")
                }
            }
        }
    }

    private suspend fun updateLevelScore(levelId: Int, stars: Int, time: Long) {
        try {
            val currentLevel = repository.getLevel(levelId).first()

            val starsGained = stars - currentLevel.stars

            repository.updateLevelScore(levelId, stars, time)

            if (starsGained > 0) {
                println("Додаємо $starsGained нових зірок. Було: ${currentLevel.stars}, Стало: $stars")
            }
        } catch (e: Exception) {
            println("Помилка при оновленні результату рівня: ${e.message}")
        }
    }

    private fun calculateStars(time: Long, moves: Int, cardCount: Int): Int {

        val pairsCount = cardCount / 2

        val perfectMoves = cardCount
        val goodMoves = perfectMoves * 1.5f
        val okMoves = perfectMoves * 2.5f

        val timeWeight = 0.6
        val timeThreshold = pairsCount * 3000L
        val timeRating = when {
            time < timeThreshold -> 3
            time < timeThreshold * 2.0 -> 2
            else -> 1
        }

        val movesWeight = 0.4
        val movesRating = when {
            moves <= perfectMoves -> 3
            moves <= goodMoves -> 2
            else -> 1
        }

        println("Оцінка гри: Час=$time (поріг=$timeThreshold, рейтинг=$timeRating), " +
                "Ходи=$moves (ідеальні=$perfectMoves, рейтинг=$movesRating)")

        return (timeRating * timeWeight + movesRating * movesWeight).toInt().coerceIn(1, 3)
    }

    fun restartGame() {
        gameTimer.reset()
        _gameFinished.value = false
        _gameCompleted.value = false
        _stars.value = 0
        _moves.value = 0
        _finalMoves.value = 0

        flipBackJob?.cancel()
        timerJob?.cancel()

        firstCard = null
        secondCard = null
        isProcessingMove.set(false)
        clickLock.set(false)

        initGame()
        startTimerTracking()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        flipBackJob?.cancel()
        gameTimer.reset()
    }

    class Factory(
        private val repository: GameRepository,
        private val audioManager: AudioManager,
        private val savedStateHandle: SavedStateHandle
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
                return GameViewModel(
                    repository = repository,
                    audioManager = audioManager,
                    savedStateHandle = savedStateHandle
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}