package com.bubbleroute.game.presentation.gameover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.core.content.ContextCompat
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
import com.bubbleroute.game.databinding.FragmentGameoverBinding
import com.bubbleroute.game.domain.model.GameMode
import com.bubbleroute.game.domain.model.RoundResult
import kotlinx.coroutines.launch

class GameOverFragment : Fragment() {

    private var binding: FragmentGameoverBinding? = null

    private val mode: GameMode
        get() = GameMode.fromName(arguments?.getString(ARG_MODE))

    private val result: RoundResult
        get() = RoundResult(
            levelIndex = arguments?.getInt(ARG_LEVEL_INDEX, 0) ?: 0,
            levelName = arguments?.getString(ARG_LEVEL_NAME) ?: "",
            score = arguments?.getInt(ARG_SCORE, 0) ?: 0,
            pairsLinked = arguments?.getInt(ARG_PAIRS, 0) ?: 0,
            totalPairs = arguments?.getInt(ARG_TOTAL_PAIRS, 1) ?: 1,
            secondsLeft = arguments?.getInt(ARG_SECONDS, 0) ?: 0,
            isWin = arguments?.getBoolean(ARG_WIN, false) ?: false,
            isNewBest = arguments?.getBoolean(ARG_NEW_BEST, false) ?: false
        )

    private val viewModel: GameOverViewModel by viewModels {
        ViewModelFactory(requireContext().applicationContext, mode, result)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentGameoverBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SystemBars.applyLightSurface(activity, true)
        val current = binding ?: return
        current.resultPlayAgain.setOnClickListener { replay() }
        current.resultMenuButton.setOnClickListener { backToMenu() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: GameOverUiState) {
        val current = binding ?: return
        val context = current.root.context
        val outcome = state.result
        current.resultHeadline.setText(
            if (outcome.isWin) R.string.result_win else R.string.result_lose
        )
        current.resultHeadline.setTextColor(
            ContextCompat.getColor(
                context,
                if (outcome.isWin) R.color.bubble_primary_dark else R.color.bubble_text
            )
        )
        current.resultScoreCard.bind(
            ScoreFormatter.grouped(outcome.score),
            getString(R.string.result_score_caption)
        )
        current.resultPairsCard.bind(
            getString(R.string.game_pairs_value, outcome.pairsLinked, outcome.totalPairs),
            getString(R.string.result_pairs_caption)
        )
        if (outcome.secondsLeft > 0) {
            current.resultTimeCard.bind(
                getString(
                    R.string.result_time_value,
                    outcome.secondsLeft / SECONDS_PER_MINUTE,
                    outcome.secondsLeft % SECONDS_PER_MINUTE
                ),
                getString(R.string.result_time_caption)
            )
        } else {
            current.resultTimeCard.visibility = View.GONE
        }
        current.resultLagoonProgress.bind(state.levelReached + 1, state.totalLevels)
        current.resultNewBest.visibility = if (outcome.isNewBest) View.VISIBLE else View.GONE
        if (outcome.isNewBest) {
            current.resultNewBest.alpha = 0f
            current.resultNewBest.animate().alpha(1f).setDuration(BEST_FADE_MS).start()
        }
        playBadge(outcome.isWin)
    }

    private fun playBadge(isWin: Boolean) {
        val current = binding ?: return
        if (!isWin) {
            current.resultBadge.alpha = LOSE_BADGE_ALPHA
            return
        }
        current.resultBadge.alpha = 1f
        current.resultBadge.scaleX = BADGE_START_SCALE
        current.resultBadge.scaleY = BADGE_START_SCALE
        current.resultBadge.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(GameConfig.BADGE_DURATION_MS)
            .setInterpolator(OvershootInterpolator(BADGE_OVERSHOOT))
            .start()
    }

    private fun replay() {
        if (!isAdded) {
            return
        }
        val host = activity as? MainActivity ?: return
        host.navigator.showGame(mode, viewModel.nextLevelIndex())
    }

    private fun backToMenu() {
        if (!isAdded) {
            return
        }
        val host = activity as? MainActivity ?: return
        host.navigator.backToMenu()
    }

    override fun onDestroyView() {
        val current = binding
        if (current != null) {
            current.resultBadge.animate().cancel()
            current.resultNewBest.animate().cancel()
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_MODE = "arg_mode"
        private const val ARG_LEVEL_INDEX = "arg_level_index"
        private const val ARG_LEVEL_NAME = "arg_level_name"
        private const val ARG_SCORE = "arg_score"
        private const val ARG_PAIRS = "arg_pairs"
        private const val ARG_TOTAL_PAIRS = "arg_total_pairs"
        private const val ARG_SECONDS = "arg_seconds"
        private const val ARG_WIN = "arg_win"
        private const val ARG_NEW_BEST = "arg_new_best"
        private const val SECONDS_PER_MINUTE = 60
        private const val BEST_FADE_MS = 400L
        private const val LOSE_BADGE_ALPHA = 0.45f
        private const val BADGE_START_SCALE = 0.6f
        private const val BADGE_OVERSHOOT = 1.2f

        fun newInstance(result: RoundResult, mode: GameMode): GameOverFragment {
            val fragment = GameOverFragment()
            val args = Bundle()
            args.putString(ARG_MODE, mode.name)
            args.putInt(ARG_LEVEL_INDEX, result.levelIndex)
            args.putString(ARG_LEVEL_NAME, result.levelName)
            args.putInt(ARG_SCORE, result.score)
            args.putInt(ARG_PAIRS, result.pairsLinked)
            args.putInt(ARG_TOTAL_PAIRS, result.totalPairs)
            args.putInt(ARG_SECONDS, result.secondsLeft)
            args.putBoolean(ARG_WIN, result.isWin)
            args.putBoolean(ARG_NEW_BEST, result.isNewBest)
            fragment.arguments = args
            return fragment
        }
    }
}
