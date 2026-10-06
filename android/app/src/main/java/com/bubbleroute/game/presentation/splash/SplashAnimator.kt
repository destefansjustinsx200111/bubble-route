package com.bubbleroute.game.presentation.splash

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import com.bubbleroute.game.R

class SplashAnimator(private val density: Float) {

    private val running = mutableListOf<Animator>()

    fun bindCluster(large: View, medium: View, small: View) {
        running.add(bobber(large, BOB_LARGE_DP, BOB_LARGE_MS, 0L))
        running.add(bobber(medium, BOB_MEDIUM_DP, BOB_MEDIUM_MS, STAGGER_MEDIUM_MS))
        running.add(bobber(small, BOB_SMALL_DP, BOB_SMALL_MS, STAGGER_SMALL_MS))
        running.add(drifter(medium, DRIFT_DP, BOB_MEDIUM_MS))
        running.add(drifter(small, -DRIFT_DP, BOB_SMALL_MS))
    }

    fun bindHalo(halo: View) {
        val animation = AnimationUtils.loadAnimation(halo.context, R.anim.splash_halo_pulse)
        halo.alpha = HALO_ALPHA
        halo.startAnimation(animation)
    }

    fun introduceTitle(title: View, subtitle: View) {
        title.alpha = 0f
        title.translationY = dp(TITLE_RISE_DP)
        title.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(TITLE_DURATION_MS)
            .setInterpolator(DecelerateInterpolator(DECELERATE_FACTOR))
            .start()
        subtitle.alpha = 0f
        subtitle.translationY = dp(SUBTITLE_RISE_DP)
        subtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(SUBTITLE_DELAY_MS)
            .setDuration(TITLE_DURATION_MS)
            .setInterpolator(DecelerateInterpolator(DECELERATE_FACTOR))
            .start()
    }

    fun introduceFooter(indicator: View, caption: View) {
        indicator.alpha = 0f
        indicator.scaleX = FOOTER_START_SCALE
        indicator.scaleY = FOOTER_START_SCALE
        indicator.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setStartDelay(FOOTER_DELAY_MS)
            .setDuration(FOOTER_DURATION_MS)
            .setInterpolator(DecelerateInterpolator(DECELERATE_FACTOR))
            .start()
        caption.alpha = 0f
        caption.animate()
            .alpha(1f)
            .setStartDelay(FOOTER_DELAY_MS + CAPTION_EXTRA_DELAY_MS)
            .setDuration(FOOTER_DURATION_MS)
            .start()
    }

    fun release(views: List<View>) {
        for (animator in running) {
            animator.cancel()
        }
        running.clear()
        for (view in views) {
            view.animate().cancel()
            view.clearAnimation()
            view.translationX = 0f
            view.translationY = 0f
        }
    }

    private fun bobber(target: View, distanceDp: Float, duration: Long, startDelay: Long): Animator {
        val animator = ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, 0f, -dp(distanceDp), 0f)
        animator.duration = duration
        animator.startDelay = startDelay
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.RESTART
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
        return animator
    }

    private fun drifter(target: View, distanceDp: Float, duration: Long): Animator {
        val animator = ObjectAnimator.ofFloat(target, View.TRANSLATION_X, 0f, dp(distanceDp), 0f)
        animator.duration = duration + DRIFT_EXTRA_MS
        animator.repeatCount = ObjectAnimator.INFINITE
        animator.repeatMode = ObjectAnimator.RESTART
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
        return animator
    }

    private fun dp(value: Float): Float = density * value

    companion object {
        private const val BOB_LARGE_DP = 14f
        private const val BOB_MEDIUM_DP = 18f
        private const val BOB_SMALL_DP = 11f
        private const val BOB_LARGE_MS = 2200L
        private const val BOB_MEDIUM_MS = 2600L
        private const val BOB_SMALL_MS = 3000L
        private const val STAGGER_MEDIUM_MS = 260L
        private const val STAGGER_SMALL_MS = 520L
        private const val DRIFT_DP = 9f
        private const val DRIFT_EXTRA_MS = 900L
        private const val HALO_ALPHA = 0.9f
        private const val TITLE_RISE_DP = 24f
        private const val SUBTITLE_RISE_DP = 12f
        private const val TITLE_DURATION_MS = 620L
        private const val SUBTITLE_DELAY_MS = 180L
        private const val FOOTER_DELAY_MS = 320L
        private const val FOOTER_DURATION_MS = 520L
        private const val CAPTION_EXTRA_DELAY_MS = 140L
        private const val FOOTER_START_SCALE = 0.8f
        private const val DECELERATE_FACTOR = 1.6f
    }
}
