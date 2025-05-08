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

            return createCardPairs(pairsNeeded)
        } catch (e: Exception) {
            // Обробка будь-яких помилок - повертаємо мінімальний набір
            println("Error generating cards: ${e.message}")
            return createMinimalDeck()
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