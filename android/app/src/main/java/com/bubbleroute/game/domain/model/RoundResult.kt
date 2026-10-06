package com.bubbleroute.game.domain.model

data class RoundResult(
    val levelIndex: Int,
    val levelName: String,
    val score: Int,
    val pairsLinked: Int,
    val totalPairs: Int,
    val secondsLeft: Int,
    val isWin: Boolean,
    val isNewBest: Boolean
)
