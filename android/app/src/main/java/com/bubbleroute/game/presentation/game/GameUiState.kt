package com.bubbleroute.game.presentation.game

import com.bubbleroute.game.domain.model.BoardState
import com.bubbleroute.game.domain.model.RoundResult

data class GameUiState(
    val board: BoardState,
    val score: Int,
    val millisLeft: Long,
    val totalMillis: Long,
    val canUndo: Boolean,
    val hintRes: Int,
    val pulse: GamePulse?,
    val result: RoundResult?
) {

    val progress: Float
        get() = if (totalMillis <= 0L) 0f else millisLeft.toFloat() / totalMillis.toFloat()
}
