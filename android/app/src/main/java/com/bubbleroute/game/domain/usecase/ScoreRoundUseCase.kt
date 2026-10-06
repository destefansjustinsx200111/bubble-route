package com.bubbleroute.game.domain.usecase

class ScoreRoundUseCase {

    fun pairScore(comboLevel: Int, routeLength: Int): Int {
        val combo = comboLevel.coerceIn(0, MAX_COMBO)
        val base = BASE_PAIR_SCORE + COMBO_STEP * combo
        val routeBonus = (ROUTE_BONUS_CAP - ROUTE_BONUS_STEP * routeLength).coerceAtLeast(0)
        return base + routeBonus
    }

    fun timeBonus(secondsLeft: Int): Int = secondsLeft.coerceAtLeast(0) * TIME_BONUS_STEP

    companion object {
        const val MAX_COMBO = 4
        private const val BASE_PAIR_SCORE = 100
        private const val COMBO_STEP = 25
        private const val ROUTE_BONUS_CAP = 60
        private const val ROUTE_BONUS_STEP = 6
        private const val TIME_BONUS_STEP = 8
    }
}
