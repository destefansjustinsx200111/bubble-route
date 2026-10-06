package com.bubbleroute.game.data.sample

import com.bubbleroute.game.domain.model.BubbleColor
import com.bubbleroute.game.domain.model.BubbleSpot
import com.bubbleroute.game.domain.model.Cell
import com.bubbleroute.game.domain.model.LevelDefinition

object SampleData {

    val LEVELS: List<LevelDefinition> = listOf(
        LevelDefinition(
            index = 0,
            name = "SHALLOW BAY",
            bubbles = twins(1, 0, 6, BubbleColor.CYAN) +
                twins(3, 1, 5, BubbleColor.PINK) +
                twins(5, 0, 6, BubbleColor.YELLOW) +
                twins(7, 1, 5, BubbleColor.BLUE),
            currents = listOf(Cell(0, 3), Cell(6, 3))
        ),
        LevelDefinition(
            index = 1,
            name = "CORAL GATE",
            bubbles = twins(0, 0, 6, BubbleColor.CYAN) +
                twins(2, 0, 4, BubbleColor.PINK) +
                twins(3, 2, 6, BubbleColor.YELLOW) +
                twins(5, 0, 6, BubbleColor.BLUE) +
                twins(7, 1, 5, BubbleColor.CYAN),
            currents = listOf(Cell(1, 3), Cell(4, 3), Cell(6, 3))
        ),
        LevelDefinition(
            index = 2,
            name = "TWIN CURRENTS",
            bubbles = twins(0, 0, 4, BubbleColor.CYAN) +
                twins(1, 2, 6, BubbleColor.PINK) +
                twins(3, 0, 6, BubbleColor.YELLOW) +
                twins(4, 1, 5, BubbleColor.BLUE) +
                twins(6, 0, 4, BubbleColor.CYAN) +
                twins(7, 2, 6, BubbleColor.PINK),
            currents = listOf(Cell(0, 6), Cell(2, 3), Cell(5, 3), Cell(7, 0))
        ),
        LevelDefinition(
            index = 3,
            name = "DRIFTWOOD MAZE",
            bubbles = twins(0, 1, 5, BubbleColor.YELLOW) +
                twins(1, 0, 6, BubbleColor.CYAN) +
                twins(3, 0, 4, BubbleColor.BLUE) +
                twins(4, 2, 6, BubbleColor.PINK) +
                twins(6, 0, 6, BubbleColor.YELLOW) +
                twins(7, 1, 5, BubbleColor.CYAN),
            currents = listOf(Cell(2, 2), Cell(2, 4), Cell(3, 6), Cell(5, 1), Cell(5, 5))
        ),
        LevelDefinition(
            index = 4,
            name = "DEEP LAGOON",
            bubbles = twins(0, 0, 6, BubbleColor.CYAN) +
                twins(1, 0, 4, BubbleColor.PINK) +
                twins(2, 2, 6, BubbleColor.YELLOW) +
                twins(3, 0, 6, BubbleColor.BLUE) +
                twins(4, 1, 5, BubbleColor.CYAN) +
                twins(6, 0, 6, BubbleColor.PINK) +
                twins(7, 1, 5, BubbleColor.YELLOW),
            currents = listOf(
                Cell(1, 6),
                Cell(2, 0),
                Cell(5, 1),
                Cell(5, 3),
                Cell(5, 5),
                Cell(7, 0)
            )
        )
    )

    private fun twins(row: Int, left: Int, right: Int, color: BubbleColor): List<BubbleSpot> =
        listOf(
            BubbleSpot(Cell(row, left), color),
            BubbleSpot(Cell(row, right), color)
        )
}
