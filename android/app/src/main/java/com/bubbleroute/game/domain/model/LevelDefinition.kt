package com.bubbleroute.game.domain.model

data class LevelDefinition(
    val index: Int,
    val name: String,
    val bubbles: List<BubbleSpot>,
    val currents: List<Cell>
) {

    val pairCount: Int
        get() = bubbles.size / 2
}
