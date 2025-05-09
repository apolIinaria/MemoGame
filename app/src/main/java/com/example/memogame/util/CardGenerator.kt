package com.example.memogame.util

import com.example.memogame.R
import com.example.memogame.model.Card
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

object CardGenerator {
    // Генератор унікальних ID з AtomicInteger для потокобезпечності
    private val idGenerator = AtomicInteger(1)

    // Список доступних зображень для карток
    private val cardImages = listOf(
        R.drawable.card_1,
        R.drawable.card_2,
        R.drawable.card_3,
        R.drawable.card_4,
        R.drawable.card_5,
        R.drawable.card_6,
        R.drawable.card_7,
        R.drawable.card_8,
        R.drawable.card_9,
        R.drawable.card_10,
        R.drawable.card_11,
        R.drawable.card_12,
        R.drawable.card_13,
        R.drawable.card_14,
        R.drawable.card_15,
        R.drawable.card_16,
        R.drawable.card_17,
        R.drawable.card_18,
        R.drawable.card_19,
        R.drawable.card_20
    )

    /**
     * Генерує набір карток для гри з обробкою можливих помилок
     *
     * @param count Кількість карток для генерації (автоматично коригується до парного числа)
     * @return Список карток для гри
     */
    fun generateCards(count: Int): List<Card> {
        try {
            // Забезпечуємо парну кількість карток
            val adjustedCount = if (count % 2 != 0) count + 1 else count
            val pairsNeeded = adjustedCount / 2

            // Перевіряємо чи маємо достатньо зображень
            if (pairsNeeded > cardImages.size) {
                // Повертаємо менший набір карток, якщо запитано більше, ніж є зображень
                val actualPairsCount = cardImages.size
                return createCardPairs(actualPairsCount)
            }

            // Створюємо пари карток - специфічно для кожного рівня
            val levelLayout = when (count) {
                6 -> Pair(2, 3)    // 6 карток: 2 колонки x 3 рядки
                12 -> Pair(3, 4)   // 12 карток: 3 колонки x 4 рядки
                16 -> Pair(4, 4)   // 16 карток: 4 колонки x 4 рядки
                20 -> Pair(4, 5)   // 20 карток: 4 колонки x 5 рядків
                24 -> Pair(4, 6)   // 24 картки: 4 колонки x 6 рядків
                else -> null      // Інші розміри не оптимізуємо спеціально
            }

            // Якщо маємо спеціальний макет для цього рівня, використовуємо його
            return if (levelLayout != null) {
                createCardPairsForLayout(pairsNeeded, levelLayout.first, levelLayout.second)
            } else {
                createCardPairs(pairsNeeded)
            }
        } catch (e: Exception) {
            // Обробка будь-яких помилок - повертаємо мінімальний набір
            println("Error generating cards: ${e.message}")
            return createMinimalDeck()
        }
    }

    /**
     * Створює пари карток оптимізовані для конкретного макету (колонки x рядки)
     */
    private fun createCardPairsForLayout(pairsCount: Int, columns: Int, rows: Int): List<Card> {
        try {
            // Перемішуємо зображення і вибираємо необхідну кількість
            val shuffledImages = cardImages.shuffled()
            val selectedImages = shuffledImages.take(pairsCount)

            // Створюємо пари карток з однаковими зображеннями
            val cards = ArrayList<Card>(pairsCount * 2)

            for (imageRes in selectedImages) {
                // Додаємо дві картки з однаковим зображенням
                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
            }

            // Перемішуємо картки, але використовуємо спеціальний алгоритм для більш рівномірного розподілу
            return distributeCardsEvenly(cards, columns, rows)
        } catch (e: Exception) {
            // У випадку помилки повертаємо стандартний набір
            println("Error creating card pairs for layout: ${e.message}")
            return createCardPairs(pairsCount)
        }
    }

    /**
     * Розподіляє картки по сітці більш рівномірно, щоб пари не були надто близько
     */
    private fun distributeCardsEvenly(cards: List<Card>, columns: Int, rows: Int): List<Card> {
        val totalCells = columns * rows
        if (cards.size != totalCells) {
            // Якщо розмір не співпадає, просто повертаємо перемішані картки
            return cards.shuffled()
        }

        try {
            // Створюємо сітку потрібного розміру
            val grid = Array(rows) { Array<Card?>(columns) { null } }
            val shuffledPairs = cards.chunked(2).shuffled()

            // Спочатку розміщуємо першу картку з кожної пари
            var pairIndex = 0
            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    if (pairIndex < shuffledPairs.size && grid[r][c] == null) {
                        // Розміщуємо першу картку з пари
                        grid[r][c] = shuffledPairs[pairIndex][0]
                        pairIndex++
                        if (pairIndex >= shuffledPairs.size) break
                    }
                }
                if (pairIndex >= shuffledPairs.size) break
            }

            // Тепер розміщуємо другу картку з кожної пари, намагаючись розмістити їх далеко від першої
            pairIndex = 0
            // Перемішуємо порядок обходу сітки для другої картки
            val rowIndices = (0 until rows).shuffled()
            val colIndices = (0 until columns).shuffled()

            // Розміщуємо другі картки з кожної пари
            for (r in rowIndices) {
                for (c in colIndices) {
                    if (pairIndex < shuffledPairs.size && grid[r][c] == null) {
                        // Розміщуємо другу картку з пари
                        grid[r][c] = shuffledPairs[pairIndex][1]
                        pairIndex++
                        if (pairIndex >= shuffledPairs.size) break
                    }
                }
                if (pairIndex >= shuffledPairs.size) break
            }

            // Перетворюємо сітку назад на плоский список
            val result = mutableListOf<Card>()
            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    grid[r][c]?.let { result.add(it) }
                }
            }

            return result
        } catch (e: Exception) {
            // У випадку помилки повертаємо просто перемішані картки
            println("Error distributing cards: ${e.message}")
            return cards.shuffled()
        }
    }

    /**
     * Створює пари карток
     */
    private fun createCardPairs(pairsCount: Int): List<Card> {
        try {
            // Перемішуємо зображення і вибираємо необхідну кількість
            val shuffledImages = cardImages.shuffled()
            val selectedImages = shuffledImages.take(pairsCount)

            // Створюємо пари карток з однаковими зображеннями
            val cards = ArrayList<Card>(pairsCount * 2)

            for (imageRes in selectedImages) {
                // Додаємо дві картки з однаковим зображенням
                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
            }

            // Перемішуємо картки для різноманітного розташування
            return cards.shuffled()
        } catch (e: Exception) {
            // У випадку помилки повертаємо мінімальний набір
            println("Error creating card pairs: ${e.message}")
            return createMinimalDeck()
        }
    }

    /**
     * Створює мінімальний набір карток для випадку помилки
     */
    private fun createMinimalDeck(): List<Card> {
        val minimalDeck = ArrayList<Card>(4)

        // Використовуємо перші два зображення
        for (i in 0..1) {
            val imageRes = cardImages.getOrElse(i) { R.drawable.card_1 }
            minimalDeck.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
            minimalDeck.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
        }

        return minimalDeck.shuffled()
    }

    /**
     * Генерує гарантовано унікальний ID для карток
     */
    private fun generateUniqueId(): Int {
        // Використовуємо AtomicInteger для потокобезпечного інкременту
        return idGenerator.getAndIncrement() + Random.nextInt(1000) * 1000
    }

    /**
     * Скидає генератор ID (для тестів або перезапуску програми)
     */
    fun resetIdGenerator() {
        idGenerator.set(1)
    }
}