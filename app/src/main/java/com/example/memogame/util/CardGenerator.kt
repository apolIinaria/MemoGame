package com.example.memogame.util

import com.example.memogame.R
import com.example.memogame.model.Card

object CardGenerator {
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

    fun generateCards(count: Int): List<Card> {
        val pairsNeeded = count / 2
        val selectedImages = cardImages.take(pairsNeeded)

        // Створюємо пари карток з однаковими зображеннями
        val cards = selectedImages.flatMap { imageRes ->
            listOf(
                Card(id = (0..1000).random(), imageRes = imageRes, isFlipped = true),
                Card(id = (0..1000).random(), imageRes = imageRes, isFlipped = true)
            )
        }

        // Перемішуємо картки
        return cards.shuffled()
    }
}