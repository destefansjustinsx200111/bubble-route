package com.bubbleroute.game.data.repository

import com.bubbleroute.game.data.sample.SampleData
import com.bubbleroute.game.domain.model.LevelDefinition
import com.bubbleroute.game.domain.repository.LevelRepository

class LevelRepositoryImpl : LevelRepository {

    override fun levels(): List<LevelDefinition> = SampleData.LEVELS

    override fun levelAt(index: Int): LevelDefinition {
        val all = SampleData.LEVELS
        val safe = index.coerceIn(0, all.size - 1)
        return all[safe]
    }

    override fun levelCount(): Int = SampleData.LEVELS.size
}
