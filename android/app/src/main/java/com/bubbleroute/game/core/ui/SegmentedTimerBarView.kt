package com.bubbleroute.game.core.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.bubbleroute.game.R
import com.bubbleroute.game.core.config.GameConfig

class SegmentedTimerBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val segmentRect = RectF()

    private var segments = GameConfig.TIMER_SEGMENTS
    private var accent = ContextCompat.getColor(context, R.color.bubble_primary)
    private var warnAccent = ContextCompat.getColor(context, R.color.bubble_accent)
    private var filled = GameConfig.TIMER_SEGMENTS
    private var warning = false

    init {
        val typed = context.obtainStyledAttributes(attrs, R.styleable.SegmentedTimerBarView)
        segments = typed.getInt(R.styleable.SegmentedTimerBarView_segmentCount, segments)
        accent = typed.getColor(R.styleable.SegmentedTimerBarView_barAccent, accent)
        warnAccent = typed.getColor(R.styleable.SegmentedTimerBarView_barWarnAccent, warnAccent)
        val initial = typed.getFloat(R.styleable.SegmentedTimerBarView_barProgress, 1f)
        typed.recycle()
        filled = segmentsFor(initial)
        trackPaint.color = ContextCompat.getColor(context, R.color.bubble_outline)
    }

    fun applyProgress(progress: Float, warn: Boolean): Boolean {
        val next = segmentsFor(progress)
        if (next == filled && warn == warning) {
            return false
        }
        filled = next
        warning = warn
        invalidate()
        return true
    }

    fun segmentsFilled(): Int = filled

    private fun segmentsFor(progress: Float): Int {
        val clamped = progress.coerceIn(0f, 1f)
        val raw = Math.ceil((clamped * segments).toDouble()).toInt()
        return raw.coerceIn(0, segments)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val count = segments.coerceAtLeast(1)
        val gap = height * GAP_RATIO
        val total = width.toFloat()
        val segmentWidth = (total - gap * (count - 1)) / count
        if (segmentWidth <= 0f) {
            return
        }
        fillPaint.color = if (warning) warnAccent else accent
        val radius = height / 2f
        for (index in 0 until count) {
            val left = index * (segmentWidth + gap)
            segmentRect.set(left, 0f, left + segmentWidth, height.toFloat())
            val paint = if (index < filled) fillPaint else trackPaint
            canvas.drawRoundRect(segmentRect, radius, radius, paint)
        }
    }

    companion object {
        private const val GAP_RATIO = 0.45f
    }
}
