package com.bubbleroute.game.domain.repository

import com.bubbleroute.game.domain.model.LevelDefinition

interface LevelRepository {

    fun levels(): List<LevelDefinition>

    fun levelAt(index: Int): LevelDefinition

    fun levelCount(): Int
}
