package com.bubbleroute.game.core.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.bubbleroute.game.R
import com.bubbleroute.game.databinding.ViewLagoonProgressBinding

class LagoonProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewLagoonProgressBinding.inflate(LayoutInflater.from(context), this)

    fun bind(current: Int, total: Int) {
        val safeTotal = total.coerceAtLeast(1)
        val safeCurrent = current.coerceIn(1, safeTotal)
        binding.progressValue.text = context.getString(
            R.string.game_pairs_value,
            safeCurrent,
            safeTotal
        )
        binding.progressValue.setTextColor(
            ContextCompat.getColor(context, R.color.bubble_primary)
        )
        buildDots(safeCurrent, safeTotal)
    }

    private fun buildDots(current: Int, total: Int) {
        binding.progressDots.removeAllViews()
        val density = resources.displayMetrics.density
        val size = (density * DOT_SIZE_DP).toInt()
        val gap = (density * DOT_GAP_DP).toInt()
        for (index in 0 until total) {
            val dot = View(context)
            val params = LinearLayout.LayoutParams(size, size)
            if (index > 0) {
                params.marginStart = gap
            }
            dot.layoutParams = params
            val drawable = if (index < current) R.drawable.dot_level_done else R.drawable.dot_level_todo
            dot.background = ContextCompat.getDrawable(context, drawable)
            binding.progressDots.addView(dot)
        }
    }

    companion object {
        private const val DOT_SIZE_DP = 10f
        private const val DOT_GAP_DP = 6f
    }
}
