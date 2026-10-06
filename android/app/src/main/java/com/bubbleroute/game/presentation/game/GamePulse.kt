package com.bubbleroute.game.presentation.game

import com.bubbleroute.game.domain.model.BubbleColor
import com.bubbleroute.game.domain.model.Cell

data class GamePulse(
    val id: Long,
    val merged: List<Cell>,
    val color: BubbleColor?,
    val blocked: Boolean
)
