package com.bubbleroute.game.domain.repository

import com.bubbleroute.game.domain.model.GameMode

interface ProgressRepository {

    fun bestScore(): Int

    fun storeBestScore(value: Int)

    fun levelReached(): Int

    fun storeLevelReached(value: Int)

    fun tutorialSeen(): Boolean

    fun storeTutorialSeen(value: Boolean)

    fun lastMode(): GameMode

    fun storeLastMode(mode: GameMode)
}
