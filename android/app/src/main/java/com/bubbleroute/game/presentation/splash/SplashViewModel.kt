package com.bubbleroute.game.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubbleroute.game.core.config.GameConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {

    private val handOffState = MutableStateFlow(false)

    val handOffReady: StateFlow<Boolean> = handOffState.asStateFlow()

    private var armed = false

    fun arm() {
        if (armed) {
            return
        }
        armed = true
        viewModelScope.launch {
            delay(GameConfig.LOADER_DURATION_MS)
            handOffState.value = true
        }
    }

    fun consumeHandOff() {
        handOffState.value = false
    }
}
