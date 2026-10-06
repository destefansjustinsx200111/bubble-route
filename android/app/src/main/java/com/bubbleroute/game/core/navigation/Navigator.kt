package com.bubbleroute.game.core.navigation

import androidx.fragment.app.FragmentManager
import com.bubbleroute.game.R
import com.bubbleroute.game.domain.model.GameMode
import com.bubbleroute.game.domain.model.RoundResult
import com.bubbleroute.game.presentation.game.GameFragment
import com.bubbleroute.game.presentation.gameover.GameOverFragment
import com.bubbleroute.game.presentation.menu.MenuFragment
import com.bubbleroute.game.presentation.splash.SplashFragment

class Navigator(
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) {

    fun showSplash(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.beginTransaction()
            .replace(containerId, SplashFragment(), TAG_SPLASH)
            .commit()
        return true
    }

    fun showMenu(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
            .replace(containerId, MenuFragment(), TAG_MENU)
            .commit()
        return true
    }

    fun showGame(mode: GameMode, levelIndex: Int): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, GameFragment.newInstance(mode, levelIndex), TAG_GAME)
            .addToBackStack(TAG_GAME)
            .commit()
        return true
    }

    fun showGameOver(result: RoundResult, mode: GameMode): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        fragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.scale_fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(containerId, GameOverFragment.newInstance(result, mode), TAG_GAMEOVER)
            .addToBackStack(TAG_GAMEOVER)
            .commit()
        return true
    }

    fun backToMenu(): Boolean {
        if (fragmentManager.isStateSaved) {
            return false
        }
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        return true
    }

    companion object {
        const val TAG_SPLASH = "splash"
        const val TAG_MENU = "menu"
        const val TAG_GAME = "game"
        const val TAG_GAMEOVER = "gameover"
    }
}
