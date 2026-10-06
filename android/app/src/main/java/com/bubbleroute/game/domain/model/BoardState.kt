package com.bubbleroute.game.domain.model

data class BoardState(
    val rows: Int,
    val cols: Int,
    val levelIndex: Int,
    val levelName: String,
    val bubbles: Map<Cell, BubbleColor>,
    val currents: Set<Cell>,
    val links: List<RouteLink>,
    val selected: Cell?,
    val totalPairs: Int
) {

    val pairsLinked: Int
        get() = links.size

    val isComplete: Boolean
        get() = bubbles.isEmpty()

    fun routeCells(): Set<Cell> {
        val used = mutableSetOf<Cell>()
        for (link in links) {
            used.addAll(link.path)
        }
        return used
    }

    fun inBounds(cell: Cell): Boolean =
        cell.row in 0 until rows && cell.col in 0 until cols

    fun partnersOf(cell: Cell): Set<Cell> {
        val color = bubbles[cell] ?: return emptySet()
        val result = mutableSetOf<Cell>()
        for (entry in bubbles) {
            if (entry.value == color && entry.key != cell) {
                result.add(entry.key)
            }
        }
        return result
    }
}
