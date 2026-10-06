package com.bubbleroute.game.presentation.gameover

import androidx.lifecycle.ViewModel
import com.bubbleroute.game.domain.model.RoundResult
import com.bubbleroute.game.domain.repository.LevelRepository
import com.bubbleroute.game.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameOverViewModel(
    private val result: RoundResult,
    private val progressRepository: ProgressRepository,
    private val levelRepository: LevelRepository
) : ViewModel() {

    private val state = MutableStateFlow(
        GameOverUiState(
            result = result,
            bestScore = progressRepository.bestScore(),
            levelReached = progressRepository.levelReached(),
            totalLevels = levelRepository.levelCount()
        )
    )

    val uiState: StateFlow<GameOverUiState> = state.asStateFlow()

    fun nextLevelIndex(): Int = progressRepository.levelReached()
}
