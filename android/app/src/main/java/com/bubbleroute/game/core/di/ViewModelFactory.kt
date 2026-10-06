package com.bubbleroute.game.core.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bubbleroute.game.domain.model.GameMode
import com.bubbleroute.game.domain.model.RoundResult
import com.bubbleroute.game.domain.usecase.FindRouteUseCase
import com.bubbleroute.game.domain.usecase.LoadLevelUseCase
import com.bubbleroute.game.domain.usecase.SaveProgressUseCase
import com.bubbleroute.game.domain.usecase.ScoreRoundUseCase
import com.bubbleroute.game.presentation.game.GameViewModel
import com.bubbleroute.game.presentation.gameover.GameOverViewModel
import com.bubbleroute.game.presentation.menu.MenuViewModel
import com.bubbleroute.game.presentation.splash.SplashViewModel

class ViewModelFactory(
    private val context: Context,
    private val mode: GameMode = GameMode.CLASSIC,
    private val levelIndex: Int = 0,
    private val result: RoundResult? = null
) : ViewModelProvider.Factory {

    constructor(context: Context, mode: GameMode, outcome: RoundResult) :
        this(context, mode, outcome.levelIndex, outcome)

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = context.applicationContext
        val progress = ServiceLocator.progressRepository(app)
        val levels = ServiceLocator.levelRepository(app)
        val created: ViewModel = when {
            modelClass.isAssignableFrom(SplashViewModel::class.java) -> SplashViewModel()
            modelClass.isAssignableFrom(MenuViewModel::class.java) -> MenuViewModel(progress, levels)
            modelClass.isAssignableFrom(GameViewModel::class.java) -> GameViewModel(
                LoadLevelUseCase(levels),
                FindRouteUseCase(),
                ScoreRoundUseCase(),
                SaveProgressUseCase(progress),
                levels,
                mode,
                levelIndex
            )
            modelClass.isAssignableFrom(GameOverViewModel::class.java) -> GameOverViewModel(
                result ?: EMPTY_RESULT,
                progress,
                levels
            )
            else -> throw IllegalArgumentException(modelClass.name)
        }
        @Suppress("UNCHECKED_CAST")
        return created as T
    }

    companion object {
        private val EMPTY_RESULT = RoundResult(
            levelIndex = 0,
            levelName = "",
            score = 0,
            pairsLinked = 0,
            totalPairs = 1,
            secondsLeft = 0,
            isWin = false,
            isNewBest = false
        )
    }
}
