package com.bubbleroute.game.data.local

import android.content.Context
import android.content.SharedPreferences

class BubblePrefs(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)

    fun readInt(key: String, fallback: Int): Int = prefs.getInt(key, fallback)

    fun writeInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    fun readBoolean(key: String, fallback: Boolean): Boolean = prefs.getBoolean(key, fallback)

    fun writeBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun readString(key: String, fallback: String): String = prefs.getString(key, fallback) ?: fallback

    fun writeString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    companion object {
        const val STORE_NAME = "bubble_route_store"
        const val KEY_BEST_SCORE = "best_score"
        const val KEY_LEVEL_REACHED = "level_reached"
        const val KEY_TUTORIAL_SEEN = "tutorial_seen"
        const val KEY_LAST_MODE = "last_mode"
    }
}
