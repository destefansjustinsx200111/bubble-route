package com.bubbleroute.game.presentation.game

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bubbleroute.game.R
import com.bubbleroute.game.core.config.GameConfig
import com.bubbleroute.game.domain.model.BoardState
import com.bubbleroute.game.domain.model.Cell
import com.bubbleroute.game.domain.model.GameMode
import com.bubbleroute.game.domain.model.RoundResult
import com.bubbleroute.game.domain.model.RouteLink
import com.bubbleroute.game.domain.repository.LevelRepository
import com.bubbleroute.game.domain.usecase.FindRouteUseCase
import com.bubbleroute.game.domain.usecase.LoadLevelUseCase
import com.bubbleroute.game.domain.usecase.SaveProgressUseCase
import com.bubbleroute.game.domain.usecase.ScoreRoundUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.min

class GameViewModel(
    private val loadLevel: LoadLevelUseCase,
    private val findRoute: FindRouteUseCase,
    private val scoreRound: ScoreRoundUseCase,
    private val saveProgress: SaveProgressUseCase,
    private val levelRepository: LevelRepository,
    private val mode: GameMode,
    private val levelIndex: Int
) : ViewModel() {

    private val totalMillis = mode.durationMs
    private val undoBoards = ArrayDeque<BoardState>()
    private val undoScores = ArrayDeque<Int>()

    private var board = loadLevel(levelIndex)
    private var score = 0
    private var combo = 0
    private var pulseCounter = 0L
    private var lastLinkAt = 0L
    private var finished = false
    private var lastSegment = GameConfig.TIMER_SEGMENTS

    private val mountedAt = SystemClock.elapsedRealtime()
    private val startedAt = mountedAt
    private var lastInputAt = mountedAt

    private val state = MutableStateFlow(
        GameUiState(
            board = board,
            score = 0,
            millisLeft = totalMillis,
            totalMillis = totalMillis,
            canUndo = false,
            hintRes = R.string.game_hint_idle,
            pulse = null,
            result = null
        )
    )

    val uiState: StateFlow<GameUiState> = state.asStateFlow()

    init {
        runLoop()
    }

    fun levelLabelIndex(): Int = board.levelIndex + 1

    fun onCellTapped(cell: Cell) {
        if (finished) {
            return
        }
        lastInputAt = SystemClock.elapsedRealtime()
        val color = board.bubbles[cell]
        if (color == null) {
            board = board.copy(selected = null)
            publish(R.string.game_hint_idle, null)
            return
        }
        val selected = board.selected
        if (selected == null || selected == cell || board.bubbles[selected] != color) {
            board = board.copy(selected = cell)
            publish(R.string.game_hint_selected, null)
            return
        }
        val path = findRoute(board, selected, cell)
        if (path == null || path.size < 2) {
            pulseCounter += 1
            publish(R.string.game_hint_blocked, GamePulse(pulseCounter, emptyList(), null, true))
            return
        }
        undoBoards.addLast(board)
        undoScores.addLast(score)
        if (undoBoards.size > GameConfig.UNDO_DEPTH) {
            undoBoards.removeFirst()
            undoScores.removeFirst()
        }
        val now = SystemClock.elapsedRealtime()
        combo = if (now - lastLinkAt <= GameConfig.COMBO_WINDOW_MS) {
            min(combo + 1, ScoreRoundUseCase.MAX_COMBO)
        } else {
            0
        }
        lastLinkAt = now
        score += scoreRound.pairScore(combo, path.size - 1)
        val remaining = LinkedHashMap(board.bubbles)
        remaining.remove(selected)
        remaining.remove(cell)
        board = board.copy(
            bubbles = remaining,
            links = board.links + RouteLink(color, path),
            selected = null
        )
        pulseCounter += 1
        publish(
            R.string.game_hint_idle,
            GamePulse(pulseCounter, listOf(selected, cell), color, false)
        )
        if (board.isComplete) {
            finish(true)
        }
    }

    fun onUndo() {
        if (finished || undoBoards.isEmpty()) {
            return
        }
        lastInputAt = SystemClock.elapsedRealtime()
        board = undoBoards.removeLast().copy(selected = null)
        score = if (undoScores.isEmpty()) 0 else undoScores.removeLast()
        combo = 0
        publish(R.string.game_hint_idle, null)
    }

    fun onRestart() {
        if (finished) {
            return
        }
        lastInputAt = SystemClock.elapsedRealtime()
        board = loadLevel(levelIndex)
        undoBoards.clear()
        undoScores.clear()
        score = 0
        combo = 0
        publish(R.string.game_hint_idle, null)
    }

    private fun runLoop() {
        viewModelScope.launch {
            while (isActive && !finished) {
                delay(GameConfig.TICK_MS)
                tick()
            }
        }
    }

    private fun tick() {
        if (finished) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        val remaining = totalMillis - (now - startedAt)
        if (remaining <= 0L) {
            finish(false)
            return
        }
        if (now - mountedAt >= GameConfig.IDLE_FLOOR_MS &&
            now - lastInputAt >= GameConfig.IDLE_TIMEOUT_MS
        ) {
            finish(false)
            return
        }
        val segment = segmentOf(remaining)
        if (segment != lastSegment) {
            lastSegment = segment
            publish(state.value.hintRes, null)
        }
    }

    private fun segmentOf(remaining: Long): Int {
        val ratio = remaining.toDouble() / totalMillis.toDouble()
        val raw = ceil(ratio * GameConfig.TIMER_SEGMENTS).toInt()
        return raw.coerceIn(0, GameConfig.TIMER_SEGMENTS)
    }

    private fun millisLeft(): Long {
        val remaining = totalMillis - (SystemClock.elapsedRealtime() - startedAt)
        return remaining.coerceIn(0L, totalMillis)
    }

    private fun publish(hintRes: Int, pulse: GamePulse?) {
        state.value = state.value.copy(
            board = board,
            score = score,
            millisLeft = millisLeft(),
            totalMillis = totalMillis,
            canUndo = undoBoards.isNotEmpty(),
            hintRes = hintRes,
            pulse = pulse
        )
    }

    private fun finish(isWin: Boolean) {
        if (finished) {
            return
        }
        finished = true
        val secondsLeft = (millisLeft() / 1000L).toInt()
        val total = if (isWin) score + scoreRound.timeBonus(secondsLeft) else score
        val isNewBest = saveProgress(total, board.levelIndex, isWin, levelRepository.levelCount())
        val outcome = RoundResult(
            levelIndex = board.levelIndex,
            levelName = board.levelName,
            score = total,
            pairsLinked = board.pairsLinked,
            totalPairs = board.totalPairs,
            secondsLeft = if (isWin) secondsLeft else 0,
            isWin = isWin,
            isNewBest = isNewBest
        )
        viewModelScope.launch {
            delay(GameConfig.RESULT_DELAY_MS)
            state.value = state.value.copy(score = total, millisLeft = 0L, result = outcome)
        }
    }
}
