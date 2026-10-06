package com.bubbleroute.game.core.di

import android.content.Context
import com.bubbleroute.game.data.local.BubblePrefs
import com.bubbleroute.game.data.repository.LevelRepositoryImpl
import com.bubbleroute.game.data.repository.ProgressRepositoryImpl
import com.bubbleroute.game.domain.repository.LevelRepository
import com.bubbleroute.game.domain.repository.ProgressRepository

object ServiceLocator {

    private var prefs: BubblePrefs? = null
    private var levels: LevelRepository? = null
    private var progress: ProgressRepository? = null

    fun init(context: Context) {
        val store = prefs ?: BubblePrefs(context.applicationContext).also { prefs = it }
        if (progress == null) {
            progress = ProgressRepositoryImpl(store)
        }
        if (levels == null) {
            levels = LevelRepositoryImpl()
        }
    }

    fun progressRepository(context: Context): ProgressRepository {
        init(context)
        val current = progress
        if (current != null) {
            return current
        }
        return ProgressRepositoryImpl(BubblePrefs(context.applicationContext))
    }

    fun levelRepository(context: Context): LevelRepository {
        init(context)
        val current = levels
        if (current != null) {
            return current
        }
        return LevelRepositoryImpl()
    }
}
