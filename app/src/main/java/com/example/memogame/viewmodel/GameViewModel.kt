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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
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

    // Створюємо і експонуємо свій StateFlow для часу, щоб могти його змінювати
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()

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

    // Глобальне блокування кліків для запобігання швидким подвійним клікам
    private val clickLock = AtomicBoolean(false)

    // Job для затримки перевороту карток назад
    private var flipBackJob: Job? = null

    // Job для відстеження часу
    private var timerJob: Job? = null

    init {
        initGame()
        // Запускаємо Job для оновлення відображуваного часу
        startTimerTracking()
    }

    private fun startTimerTracking() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            gameTimer.elapsedTime.collect { time ->
                _elapsedTime.value = time
            }
        }
    }

    private fun initGame() {
        // Скасовуємо будь-які активні затримки
        flipBackJob?.cancel()

        // Скидаємо стани
        firstCard = null
        secondCard = null
        isProcessingMove.set(false)
        clickLock.set(false)

        viewModelScope.launch {
            try {
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
                                level.cardCount <= 8 -> 2000L
                                level.cardCount <= 12 -> 3000L
                                else -> 4000L
                            }

                            delay(memorizeTime)

                            // Перевіряємо чи корутина все ще активна
                            if (!isActive) return@collect

                            _isShowingCards.value = false

                            // Перевертаємо всі картки і запускаємо таймер
                            _cards.value = _cards.value.map { it.copy(isFlipped = false) }

                            // Запускаємо таймер після того, як картки перевернулись
                            delay(400) // Затримка для анімації перевороту

                            // Перевіряємо чи корутина все ще активна
                            if (!isActive) return@collect

                            gameTimer.start()
                        } catch (e: Exception) {
                            // Обробка помилок генерації карток
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
        // Використовуємо глобальне блокування для запобігання швидким подвійним клікам
        if (!clickLock.compareAndSet(false, true)) {
            return // Якщо блокування вже встановлено, виходимо
        }

        // Встановлюємо таймер для розблокування кліків
        viewModelScope.launch {
            delay(500)
            clickLock.set(false)
        }

        // Блокуємо доступ для забезпечення атомарності операції
        synchronized(this) {
            // Перевіряємо можливість перевороту
            if (isProcessingMove.get() || card.isMatched || card.isFlipped || _gameFinished.value) {
                return
            }

            // Встановлюємо блокування перед будь-якими операціями
            isProcessingMove.set(true)
        }

        try {
            // Знаходимо індекс картки зі запобіжними перевірками
            val currentCards = _cards.value.toMutableList()
            val clickedCardIndex = currentCards.indexOfFirst { it.id == card.id }

            if (clickedCardIndex == -1 || clickedCardIndex >= currentCards.size) {
                isProcessingMove.set(false)  // Розблоковуємо у випадку помилки
                return
            }

            // Перевертаємо картку
            val updatedCard = currentCards[clickedCardIndex].copy(isFlipped = true)
            currentCards[clickedCardIndex] = updatedCard

            // FIX: Інкрементуємо лічильник ходів для КОЖНОЇ перевернутої картки
            _moves.value = _moves.value + 1

            when {
                firstCard == null -> {
                    // Перша картка в парі
                    firstCard = updatedCard
                    _cards.value = currentCards
                    // Розблоковуємо для наступного кліку
                    isProcessingMove.set(false)
                }
                secondCard == null && firstCard?.id != updatedCard.id -> {
                    // Друга картка в парі - ВЖЕ інкрементували хід вище
                    secondCard = updatedCard
                    _cards.value = currentCards

                    // Перевіряємо співпадіння з затримкою
                    viewModelScope.launch {
                        try {
                            delay(500) // Затримка для показу другої картки
                            checkMatch()
                        } catch (e: Exception) {
                            // Обробка помилок у корутині
                            println("Помилка в корутині перевірки співпадіння: ${e.message}")
                            // Забезпечуємо розблокування у випадку помилки
                            firstCard = null
                            secondCard = null
                            isProcessingMove.set(false)
                        }
                    }
                }
                else -> {
                    // Неочікуваний стан - розблоковуємо
                    isProcessingMove.set(false)
                }
            }
        } catch (e: Exception) {
            // Обробка можливих помилок
            println("Помилка при обробці кліку по картці: ${e.message}")
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
                // Належно скасовуємо попередню корутину перед запуском нової
                flipBackJob?.cancel()

                flipBackJob = viewModelScope.launch {
                    try {
                        delay(500) // Затримка перед переворотом

                        // Перевіряємо чи корутина все ще активна після затримки
                        if (!isActive) return@launch

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

                        // Перевіряємо чи корутина все ще активна
                        if (isActive) {
                            isProcessingMove.set(false)
                        }
                    } catch (e: Exception) {
                        // Обробка помилок у корутині
                        println("Помилка в корутині перевороту карток: ${e.message}")
                        // Забезпечуємо розблокування у випадку помилки
                        firstCard = null
                        secondCard = null
                        isProcessingMove.set(false)
                    }
                }
            }
        } catch (e: Exception) {
            // Обробка можливих помилок
            println("Помилка при перевірці співпадіння карток: ${e.message}")
            // Скидаємо стан у разі помилки
            firstCard = null
            secondCard = null
            isProcessingMove.set(false)
        }
    }

    private fun finishGame() {
        // FIX: Зупиняємо таймер, зберігаємо і фіксуємо фінальний час
        val finishTime = gameTimer.stop()
        _elapsedTime.value = finishTime  // Фіксуємо фінальний час
        _gameFinished.value = true

        // Обчислюємо кількість зірок на основі часу та кількості ходів
        level.value?.let { level ->
            val stars = calculateStars(finishTime, _moves.value, level.cardCount)
            _stars.value = stars

            // Зберігаємо результат
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
            // Отримуємо поточний рівень
            val currentLevel = repository.getLevel(levelId).first()

            // Обчислюємо, скільки нових зірок було отримано
            val starsGained = stars - currentLevel.stars

            // Оновлюємо рівень, лише якщо результат кращий
            repository.updateLevelScore(levelId, stars, time)

            // Додаємо лише нові зірки до загального рахунку, і лише якщо їх кількість позитивна
            if (starsGained > 0) {
                // Логуємо для відлагодження
                println("Додаємо $starsGained нових зірок. Було: ${currentLevel.stars}, Стало: $stars")
            }
        } catch (e: Exception) {
            println("Помилка при оновленні результату рівня: ${e.message}")
        }
    }

    private fun calculateStars(time: Long, moves: Int, cardCount: Int): Int {
        // Базовий час і ходи для різних рівнів складності
        val pairsCount = cardCount / 2

        // FIX: Оновлена логіка для оптимальної кількості ходів
        // Кожна картка - це один хід, але для ідеальної гри потрібно:
        // - pairsCount * 2 ходів (всі пари знайдені з першого разу)
        val perfectMoves = cardCount // Ідеальна гра - всі картки перевернуті по одному разу
        val goodMoves = perfectMoves * 1.5f // Хороша гра
        val okMoves = perfectMoves * 2.5f // Нормальна гра

        // Збільшуємо часові пороги для отримання зірок
        // Оцінка за часом (з урахуванням складності)
        val timeWeight = 0.6 // Ваговий коефіцієнт для часу
        val timeThreshold = pairsCount * 3000L // Базовий час у мілісекундах
        val timeRating = when {
            time < timeThreshold -> 3          // Відмінний час
            time < timeThreshold * 2.0 -> 2    // Хороший час
            else -> 1                         // Нормальний час
        }

        // Оцінка за ходами з урахуванням нової системи підрахунку ходів
        val movesWeight = 0.4 // Ваговий коефіцієнт для ходів
        val movesRating = when {
            moves <= perfectMoves -> 3        // Ідеальна гра - максимальна оцінка
            moves <= goodMoves -> 2           // Хороша гра
            else -> 1                        // Нормальна гра
        }

        // Виводимо інформацію для налагодження
        println("Оцінка гри: Час=$time (поріг=$timeThreshold, рейтинг=$timeRating), " +
                "Ходи=$moves (ідеальні=$perfectMoves, рейтинг=$movesRating)")

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
        clickLock.set(false)

        // Запускаємо нову гру
        initGame()
    }

    override fun onCleared() {
        super.onCleared()
        // Скасовуємо активні задачі при знищенні ViewModel
        timerJob?.cancel()
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