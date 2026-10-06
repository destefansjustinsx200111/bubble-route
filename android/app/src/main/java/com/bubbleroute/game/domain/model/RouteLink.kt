package com.bubbleroute.game.domain.model

data class RouteLink(val color: BubbleColor, val path: List<Cell>) {

    val length: Int
        get() = if (path.isEmpty()) 0 else path.size - 1
}
