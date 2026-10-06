package com.bubbleroute.game.domain.model

data class Cell(val row: Int, val col: Int) {

    fun neighbours(): List<Cell> = listOf(
        Cell(row - 1, col),
        Cell(row + 1, col),
        Cell(row, col - 1),
        Cell(row, col + 1)
    )
}
