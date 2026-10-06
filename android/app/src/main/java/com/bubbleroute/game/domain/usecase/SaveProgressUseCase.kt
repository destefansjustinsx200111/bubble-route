package com.bubbleroute.game.domain.usecase

import com.bubbleroute.game.domain.repository.ProgressRepository

class SaveProgressUseCase(private val progressRepository: ProgressRepository) {

    operator fun invoke(score: Int, levelIndex: Int, isWin: Boolean, levelCount: Int): Boolean {
        val best = progressRepository.bestScore()
        val improved = score > best
        if (improved) {
            progressRepository.storeBestScore(score)
        }
        if (isWin) {
            val next = (levelIndex + 1).coerceAtMost(levelCount - 1)
            if (next > progressRepository.levelReached()) {
                progressRepository.storeLevelReached(next)
            }
        }
        return improved
    }
}
