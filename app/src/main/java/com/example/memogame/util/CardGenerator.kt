package com.example.memogame.util

import com.example.memogame.R
import com.example.memogame.model.Card
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random

object CardGenerator {

    private val idGenerator = AtomicInteger(1)

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
        try {
            val adjustedCount = if (count % 2 != 0) count + 1 else count
            val pairsNeeded = adjustedCount / 2

            if (pairsNeeded > cardImages.size) {

                val actualPairsCount = cardImages.size
                return createCardPairs(actualPairsCount)
            }

            val levelLayout = when (count) {
                6 -> Pair(2, 3)
                12 -> Pair(3, 4)
                16 -> Pair(4, 4)
                20 -> Pair(4, 5)
                24 -> Pair(4, 6)
                else -> null
            }

            return if (levelLayout != null) {
                createCardPairsForLayout(pairsNeeded, levelLayout.first, levelLayout.second)
            } else {
                createCardPairs(pairsNeeded)
            }
        } catch (e: Exception) {
            println("Error generating cards: ${e.message}")
            return createMinimalDeck()
        }
    }

    private fun createCardPairsForLayout(pairsCount: Int, columns: Int, rows: Int): List<Card> {
        try {

            val shuffledImages = cardImages.shuffled()
            val selectedImages = shuffledImages.take(pairsCount)

            val cards = ArrayList<Card>(pairsCount * 2)

            for (imageRes in selectedImages) {

                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
            }

            return distributeCardsEvenly(cards, columns, rows)
        } catch (e: Exception) {
            println("Error creating card pairs for layout: ${e.message}")
            return createCardPairs(pairsCount)
        }
    }

    private fun distributeCardsEvenly(cards: List<Card>, columns: Int, rows: Int): List<Card> {
        val totalCells = columns * rows
        if (cards.size != totalCells) {
            return cards.shuffled()
        }

        try {

            val grid = Array(rows) { Array<Card?>(columns) { null } }
            val shuffledPairs = cards.chunked(2).shuffled()

            var pairIndex = 0
            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    if (pairIndex < shuffledPairs.size && grid[r][c] == null) {

                        grid[r][c] = shuffledPairs[pairIndex][0]
                        pairIndex++
                        if (pairIndex >= shuffledPairs.size) break
                    }
                }
                if (pairIndex >= shuffledPairs.size) break
            }

            pairIndex = 0

            val rowIndices = (0 until rows).shuffled()
            val colIndices = (0 until columns).shuffled()

            for (r in rowIndices) {
                for (c in colIndices) {
                    if (pairIndex < shuffledPairs.size && grid[r][c] == null) {
                        grid[r][c] = shuffledPairs[pairIndex][1]
                        pairIndex++
                        if (pairIndex >= shuffledPairs.size) break
                    }
                }
                if (pairIndex >= shuffledPairs.size) break
            }

            val result = mutableListOf<Card>()
            for (r in 0 until rows) {
                for (c in 0 until columns) {
                    grid[r][c]?.let { result.add(it) }
                }
            }

            return result
        } catch (e: Exception) {
            println("Error distributing cards: ${e.message}")
            return cards.shuffled()
        }
    }

    private fun createCardPairs(pairsCount: Int): List<Card> {
        try {
            val shuffledImages = cardImages.shuffled()
            val selectedImages = shuffledImages.take(pairsCount)

            val cards = ArrayList<Card>(pairsCount * 2)

            for (imageRes in selectedImages) {
                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
                cards.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
            }
            return cards.shuffled()

        } catch (e: Exception) {
            println("Error creating card pairs: ${e.message}")
            return createMinimalDeck()
        }
    }
    private fun createMinimalDeck(): List<Card> {
        val minimalDeck = ArrayList<Card>(4)

        for (i in 0..1) {
            val imageRes = cardImages.getOrElse(i) { R.drawable.card_1 }
            minimalDeck.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
            minimalDeck.add(Card(id = generateUniqueId(), imageRes = imageRes, isFlipped = true))
        }

        return minimalDeck.shuffled()
    }

    private fun generateUniqueId(): Int {
        return idGenerator.getAndIncrement() + Random.nextInt(1000) * 1000
    }

    fun resetIdGenerator() {
        idGenerator.set(1)
    }
}