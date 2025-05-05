package com.example.memogame.util

import com.example.memogame.R
import com.example.memogame.model.Card

object CardGenerator {
    private val cardImages = listOf(
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground,
        R.drawable.ic_launcher_foreground
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