package com.bubbleroute.game.presentation.menu

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
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
import com.bubbleroute.game.databinding.FragmentMenuBinding
import com.bubbleroute.game.domain.model.GameMode
import com.bubbleroute.game.presentation.dialog.TutorialDialog
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class MenuFragment : Fragment() {

    private var binding: FragmentMenuBinding? = null

    private val viewModel: MenuViewModel by viewModels {
        ViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentMenuBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SystemBars.applyLightSurface(activity, true)
        val current = binding ?: return
        current.menuPlayButton.setOnClickListener { startRound() }
        current.menuTutorialButton.setOnClickListener { openTutorial() }
        current.menuModeClassic.setOnClickListener { viewModel.selectMode(GameMode.CLASSIC) }
        current.menuModeChallenge.setOnClickListener { viewModel.selectMode(GameMode.CHALLENGE) }
        playEntrance(current)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun render(state: MenuUiState) {
        val current = binding ?: return
        val context = current.root.context
        current.menuLagoonProgress.bind(state.levelReached + 1, state.totalLevels)
        if (state.bestScore > 0) {
            current.menuBestSlot.visibility = View.VISIBLE
            current.menuBestCard.bind(
                ScoreFormatter.grouped(state.bestScore),
                getString(R.string.menu_best_caption)
            )
        } else {
            current.menuBestSlot.visibility = View.INVISIBLE
        }
        val accentClassic = ContextCompat.getColor(context, R.color.bubble_primary)
        val accentChallenge = ContextCompat.getColor(context, R.color.bubble_accent)
        applyChip(current.menuModeClassic, state.mode == GameMode.CLASSIC, accentClassic)
        applyChip(current.menuModeChallenge, state.mode == GameMode.CHALLENGE, accentChallenge)
    }

    private fun applyChip(button: MaterialButton, selected: Boolean, accent: Int) {
        val context = button.context
        val idle = ContextCompat.getColor(context, R.color.bubble_outline_soft)
        val text = ContextCompat.getColor(context, R.color.bubble_text)
        button.strokeColor = ColorStateList.valueOf(if (selected) accent else idle)
        button.backgroundTintList = ColorStateList.valueOf(
            if (selected) withAlpha(accent) else Color.TRANSPARENT
        )
        button.setTextColor(if (selected) accent else text)
        ViewCompat.setStateDescription(
            button,
            getString(if (selected) R.string.state_selected else R.string.state_not_selected)
        )
    }

    private fun withAlpha(color: Int): Int =
        Color.argb(CHIP_FILL_ALPHA, Color.red(color), Color.green(color), Color.blue(color))

    private fun playEntrance(current: FragmentMenuBinding) {
        val targets = listOf(
            current.menuTitle,
            current.menuPlayTile,
            current.menuTileRow,
            current.menuModeRow,
            current.menuTutorialButton
        )
        for (index in targets.indices) {
            val animation = AnimationUtils.loadAnimation(current.root.context, R.anim.tile_rise)
            animation.startOffset = GameConfig.TILE_STAGGER_MS * index
            targets[index].startAnimation(animation)
        }
    }

    private fun startRound() {
        if (!isAdded) {
            return
        }
        val host = activity as? MainActivity ?: return
        val state = viewModel.uiState.value
        host.navigator.showGame(state.mode, viewModel.startingLevel())
    }

    private fun openTutorial() {
        if (!isAdded) {
            return
        }
        viewModel.markTutorialSeen()
        TutorialDialog().show(parentFragmentManager, TutorialDialog.TAG)
    }

    override fun onDestroyView() {
        val current = binding
        if (current != null) {
            current.menuTitle.clearAnimation()
            current.menuPlayTile.clearAnimation()
            current.menuTileRow.clearAnimation()
            current.menuModeRow.clearAnimation()
            current.menuTutorialButton.clearAnimation()
        }
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val CHIP_FILL_ALPHA = 26
    }
}
