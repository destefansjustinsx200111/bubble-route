package com.bubbleroute.game.presentation.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bubbleroute.game.MainActivity
import com.bubbleroute.game.R
import com.bubbleroute.game.core.config.GameConfig
import com.bubbleroute.game.core.di.ViewModelFactory
import com.bubbleroute.game.core.ui.ScoreFormatter
import com.bubbleroute.game.core.ui.SystemBars
import com.bubbleroute.game.databinding.FragmentGameBinding
import com.bubbleroute.game.domain.model.GameMode
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var binding: FragmentGameBinding? = null
    private var lastPulseId = 0L
    private var navigated = false

    private val mode: GameMode
        get() = GameMode.fromName(arguments?.getString(ARG_MODE))

    private val levelIndex: Int
        get() = arguments?.getInt(ARG_LEVEL, 0) ?: 0

    private val viewModel: GameViewModel by viewModels {
        ViewModelFactory(requireContext().applicationContext, mode, levelIndex)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SystemBars.applyLightSurface(activity, true)
        val current = binding ?: return
        current.gameBoard.onCellTapped = { cell -> viewModel.onCellTapped(cell) }
        current.gameBackButton.setOnClickListener { leaveToMenu() }
        current.gameUndoButton.setOnClickListener { viewModel.onUndo() }
        current.gameRestartButton.setOnClickListener { viewModel.onRestart() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: GameUiState) {
        val current = binding ?: return
        current.gameLevelLabel.text =
            getString(R.string.game_level_label, state.board.levelIndex + 1)
        current.gameScore.text = ScoreFormatter.grouped(state.score)
        current.gameHint.setText(state.hintRes)
        current.gameTimerBar.applyProgress(
            state.progress,
            state.millisLeft <= GameConfig.WARN_THRESHOLD_MS
        )
        ViewCompat.setStateDescription(
            current.gameTimerBar,
            getString(
                if (state.millisLeft <= GameConfig.WARN_THRESHOLD_MS) {
                    R.string.state_tide_rising
                } else {
                    R.string.state_tide_calm
                }
            )
        )
        current.gamePairsCard.bind(
            getString(R.string.game_pairs_value, state.board.pairsLinked, state.board.totalPairs),
            getString(R.string.game_pairs_caption)
        )
        current.gameUndoButton.isEnabled = state.canUndo
        current.gameUndoButton.alpha = if (state.canUndo) 1f else DISABLED_ALPHA
        ViewCompat.setStateDescription(
            current.gameUndoButton,
            getString(if (state.canUndo) R.string.state_undo_ready else R.string.state_undo_empty)
        )
        current.gameBoard.setBoard(state.board)
        applyPulse(state)
        val outcome = state.result
        if (outcome != null && !navigated) {
            val host = activity as? MainActivity
            if (host != null && host.navigator.showGameOver(outcome, mode)) {
                navigated = true
            }
        }
    }

    private fun applyPulse(state: GameUiState) {
        val current = binding ?: return
        val pulse = state.pulse ?: return
        if (pulse.id == lastPulseId) {
            return
        }
        lastPulseId = pulse.id
        if (pulse.blocked) {
            current.gameBoard.playBlocked()
            return
        }
        val color = pulse.color
        if (color != null && pulse.merged.isNotEmpty()) {
            current.gameBoard.playMerge(pulse.merged, color)
        }
    }

    private fun leaveToMenu() {
        if (!isAdded) {
            return
        }
        val host = activity as? MainActivity ?: return
        host.navigator.backToMenu()
    }

    override fun onDestroyView() {
        val current = binding
        if (current != null) {
            current.gameBoard.onCellTapped = null
            current.gameBoard.clearAnimations()
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_MODE = "arg_mode"
        private const val ARG_LEVEL = "arg_level"
        private const val DISABLED_ALPHA = 0.4f

        fun newInstance(mode: GameMode, levelIndex: Int): GameFragment {
            val fragment = GameFragment()
            val args = Bundle()
            args.putString(ARG_MODE, mode.name)
            args.putInt(ARG_LEVEL, levelIndex)
            fragment.arguments = args
            return fragment
        }
    }
}
