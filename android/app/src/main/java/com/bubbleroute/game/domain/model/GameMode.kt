package com.bubbleroute.game.domain.model

import com.bubbleroute.game.core.config.GameConfig

enum class GameMode(val durationMs: Long) {
    CLASSIC(GameConfig.ROUND_DURATION_MS),
    CHALLENGE(GameConfig.CHALLENGE_DURATION_MS);

    companion object {

        fun fromName(raw: String?): GameMode {
            val modes = values()
            for (mode in modes) {
                if (mode.name == raw) {
                    return mode
                }
            }
            return CLASSIC
        }
    }
}
