package com.bubbleroute.game.presentation.menu

import androidx.lifecycle.ViewModel
import com.bubbleroute.game.domain.model.GameMode
import com.bubbleroute.game.domain.repository.LevelRepository
import com.bubbleroute.game.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(
    private val progressRepository: ProgressRepository,
    private val levelRepository: LevelRepository
) : ViewModel() {

    private val state = MutableStateFlow(snapshot())

    val uiState: StateFlow<MenuUiState> = state.asStateFlow()

    fun refresh() {
        state.value = snapshot()
    }

    fun selectMode(mode: GameMode) {
        progressRepository.storeLastMode(mode)
        refresh()
    }

    fun markTutorialSeen() {
        progressRepository.storeTutorialSeen(true)
        refresh()
    }

    fun startingLevel(): Int = progressRepository.levelReached()

    private fun snapshot(): MenuUiState = MenuUiState(
        bestScore = progressRepository.bestScore(),
        levelReached = progressRepository.levelReached(),
        totalLevels = levelRepository.levelCount(),
        mode = progressRepository.lastMode(),
        tutorialSeen = progressRepository.tutorialSeen()
    )
}
