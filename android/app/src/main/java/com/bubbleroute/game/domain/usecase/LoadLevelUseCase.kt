package com.bubbleroute.game.domain.usecase

import com.bubbleroute.game.core.config.GameConfig
import com.bubbleroute.game.domain.model.BoardState
import com.bubbleroute.game.domain.model.BubbleColor
import com.bubbleroute.game.domain.model.Cell
import com.bubbleroute.game.domain.repository.LevelRepository

class LoadLevelUseCase(private val levelRepository: LevelRepository) {

    operator fun invoke(index: Int): BoardState {
        val level = levelRepository.levelAt(index)
        val bubbles = LinkedHashMap<Cell, BubbleColor>()
        for (spot in level.bubbles) {
            bubbles[spot.cell] = spot.color
        }
        return BoardState(
            rows = GameConfig.BOARD_ROWS,
            cols = GameConfig.BOARD_COLS,
            levelIndex = level.index,
            levelName = level.name,
            bubbles = bubbles,
            currents = level.currents.toSet(),
            links = emptyList(),
            selected = null,
            totalPairs = level.pairCount
        )
    }
}
