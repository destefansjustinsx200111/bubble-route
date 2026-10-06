package com.bubbleroute.game.presentation.menu

import com.bubbleroute.game.domain.model.GameMode

data class MenuUiState(
    val bestScore: Int,
    val levelReached: Int,
    val totalLevels: Int,
    val mode: GameMode,
    val tutorialSeen: Boolean
)
