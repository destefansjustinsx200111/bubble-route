package com.bubbleroute.game.domain.usecase

import com.bubbleroute.game.domain.model.BoardState
import com.bubbleroute.game.domain.model.Cell

class FindRouteUseCase {

    operator fun invoke(board: BoardState, from: Cell, to: Cell): List<Cell>? {
        if (from == to) {
            return null
        }
        if (!board.inBounds(from) || !board.inBounds(to)) {
            return null
        }
        val blocked = HashSet<Cell>()
        blocked.addAll(board.currents)
        blocked.addAll(board.routeCells())
        for (key in board.bubbles.keys) {
            if (key != from && key != to) {
                blocked.add(key)
            }
        }
        val previous = HashMap<Cell, Cell>()
        val visited = HashSet<Cell>()
        val queue = ArrayDeque<Cell>()
        queue.addLast(from)
        visited.add(from)
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            if (current == to) {
                return tracePath(previous, from, to)
            }
            for (next in current.neighbours()) {
                if (!board.inBounds(next) || visited.contains(next)) {
                    continue
                }
                if (next != to && blocked.contains(next)) {
                    continue
                }
                visited.add(next)
                previous[next] = current
                queue.addLast(next)
            }
        }
        return null
    }

    private fun tracePath(previous: Map<Cell, Cell>, from: Cell, to: Cell): List<Cell> {
        val reversed = mutableListOf(to)
        var cursor = to
        while (cursor != from) {
            val step = previous[cursor] ?: return emptyList()
            reversed.add(step)
            cursor = step
        }
        return reversed.reversed()
    }
}
