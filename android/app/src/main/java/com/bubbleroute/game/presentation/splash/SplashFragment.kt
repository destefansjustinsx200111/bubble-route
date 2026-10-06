package com.bubbleroute.game.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bubbleroute.game.MainActivity
import com.bubbleroute.game.core.ui.SystemBars
import com.bubbleroute.game.databinding.FragmentSplashBinding
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var binding: FragmentSplashBinding? = null
    private var animator: SplashAnimator? = null

    private val viewModel: SplashViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentSplashBinding.inflate(inflater, container, false)
        binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SystemBars.applyLightSurface(activity, false)
        val current = binding ?: return
        val helper = SplashAnimator(resources.displayMetrics.density)
        animator = helper
        helper.bindHalo(current.splashHalo)
        helper.bindCluster(current.splashBubbleLg, current.splashBubbleMd, current.splashBubbleSm)
        helper.introduceTitle(current.splashTitle, current.splashSubtitle)
        helper.introduceFooter(current.splashProgress, current.splashLoading)
        viewModel.arm()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.handOffReady.collect { ready ->
                    if (ready) {
                        handOff()
                    }
                }
            }
        }
    }

    private fun handOff() {
        if (!isAdded || binding == null) {
            return
        }
        val host = activity as? MainActivity ?: return
        if (!host.navigator.showMenu()) {
            return
        }
        viewModel.consumeHandOff()
    }

    override fun onDestroyView() {
        val current = binding
        if (current != null) {
            animator?.release(
                listOf(
                    current.splashHalo,
                    current.splashBubbleLg,
                    current.splashBubbleMd,
                    current.splashBubbleSm,
                    current.splashTitle,
                    current.splashSubtitle,
                    current.splashProgress,
                    current.splashLoading
                )
            )
        }
        animator = null
        binding = null
        super.onDestroyView()
    }
}
