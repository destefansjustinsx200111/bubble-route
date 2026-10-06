package com.bubbleroute.game.core.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.bubbleroute.game.R
import com.bubbleroute.game.databinding.ViewStatCardBinding

class StatCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStatCardBinding.inflate(LayoutInflater.from(context), this)

    private var accentColor: Int = ContextCompat.getColor(context, R.color.bubble_primary)
    private var compact: Boolean = false

    init {
        val typed = context.obtainStyledAttributes(attrs, R.styleable.StatCardView)
        accentColor = typed.getColor(R.styleable.StatCardView_statAccent, accentColor)
        compact = typed.getBoolean(R.styleable.StatCardView_statCompact, false)
        val label = typed.getString(R.styleable.StatCardView_statLabel)
        val value = typed.getString(R.styleable.StatCardView_statValue)
        typed.recycle()
        applyAccent()
        applyDensity()
        binding.statLabel.text = label ?: ""
        binding.statValue.text = value ?: ""
    }

    fun bind(value: String, label: String) {
        binding.statValue.text = value
        binding.statLabel.text = label
        visibility = View.VISIBLE
    }

    fun bindOrHide(value: Int, label: String) {
        if (value <= 0) {
            visibility = View.GONE
            return
        }
        bind(ScoreFormatter.grouped(value), label)
    }

    fun setAccent(color: Int) {
        accentColor = color
        applyAccent()
    }

    private fun applyAccent() {
        binding.statValue.setTextColor(accentColor)
        binding.statDot.backgroundTintList = android.content.res.ColorStateList.valueOf(accentColor)
        if (!compact) {
            binding.statCard.strokeColor = withAlpha(accentColor, STROKE_ALPHA)
        }
    }

    private fun applyDensity() {
        if (!compact) {
            return
        }
        binding.statCard.cardElevation = 0f
        binding.statCard.strokeWidth = 0
        binding.statCard.setCardBackgroundColor(Color.TRANSPARENT)
        val pad = (resources.displayMetrics.density * COMPACT_PADDING_DP).toInt()
        binding.statContent.setPadding(pad, pad, pad, pad)
        binding.statValue.textSize = COMPACT_VALUE_SP
    }

    private fun withAlpha(color: Int, alpha: Int): Int =
        Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))

    companion object {
        private const val STROKE_ALPHA = 85
        private const val COMPACT_PADDING_DP = 8f
        private const val COMPACT_VALUE_SP = 18f
    }
}
