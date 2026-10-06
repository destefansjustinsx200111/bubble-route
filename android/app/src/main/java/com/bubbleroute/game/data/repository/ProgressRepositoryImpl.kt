package com.bubbleroute.game.data.repository

import com.bubbleroute.game.data.local.BubblePrefs
import com.bubbleroute.game.domain.model.GameMode
import com.bubbleroute.game.domain.repository.ProgressRepository

class ProgressRepositoryImpl(private val prefs: BubblePrefs) : ProgressRepository {

    override fun bestScore(): Int = prefs.readInt(BubblePrefs.KEY_BEST_SCORE, 0)

    override fun storeBestScore(value: Int) {
        prefs.writeInt(BubblePrefs.KEY_BEST_SCORE, value)
    }

    override fun levelReached(): Int = prefs.readInt(BubblePrefs.KEY_LEVEL_REACHED, 0)

    override fun storeLevelReached(value: Int) {
        prefs.writeInt(BubblePrefs.KEY_LEVEL_REACHED, value)
    }

    override fun tutorialSeen(): Boolean = prefs.readBoolean(BubblePrefs.KEY_TUTORIAL_SEEN, false)

    override fun storeTutorialSeen(value: Boolean) {
        prefs.writeBoolean(BubblePrefs.KEY_TUTORIAL_SEEN, value)
    }

    override fun lastMode(): GameMode =
        GameMode.fromName(prefs.readString(BubblePrefs.KEY_LAST_MODE, GameMode.CLASSIC.name))

    override fun storeLastMode(mode: GameMode) {
        prefs.writeString(BubblePrefs.KEY_LAST_MODE, mode.name)
    }
}
