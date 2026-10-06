package com.bubbleroute.game.presentation.gameover

import com.bubbleroute.game.domain.model.RoundResult

data class GameOverUiState(
    val result: RoundResult,
    val bestScore: Int,
    val levelReached: Int,
    val totalLevels: Int
)
