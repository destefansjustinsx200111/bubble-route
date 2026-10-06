package com.bubbleroute.game.core.ui

import android.app.Activity
import androidx.core.view.WindowCompat

object SystemBars {

    fun applyLightSurface(activity: Activity?, lightSurface: Boolean) {
        if (activity == null || activity.isFinishing) {
            return
        }
        try {
            val window = activity.window ?: return
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.isAppearanceLightStatusBars = lightSurface
            controller.isAppearanceLightNavigationBars = lightSurface
        } catch (e: Exception) {
            return
        }
    }
}
