package com.bubbleroute.game.core.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.bubbleroute.game.R
import com.bubbleroute.game.core.config.GameConfig
import com.bubbleroute.game.domain.model.BoardState
import com.bubbleroute.game.domain.model.BubbleColor
import com.bubbleroute.game.domain.model.Cell
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class RouteBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onCellTapped: ((Cell) -> Unit)? = null

    private val cols = GameConfig.BOARD_COLS
    private val rows = GameConfig.BOARD_ROWS
    private val framePx = dp(BOARD_FRAME_DP)
    private val maxWidthPx = dp(BOARD_MAX_WIDTH_DP)
    private val cornerPx = dp(BOARD_CORNER_DP).toFloat()

    private val surfacePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val routePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val routeInnerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val burstPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val boardRect = RectF()
    private val routePath = Path()

    private var cellPx = 0
    private var board: BoardState? = null

    private var mergeCells: List<Cell> = emptyList()
    private var mergeColor: BubbleColor? = null
    private var mergeProgress = 0f
    private var mergeAnimator: ValueAnimator? = null
    private var shakeAnimator: ObjectAnimator? = null

    private var downX = 0f
    private var downY = 0f

    private val sprites = HashMap<BubbleColor, Drawable?>()
    private var currentSprite: Drawable? = null

    init {
        surfacePaint.color = ContextCompat.getColor(context, R.color.bubble_board_fill)
        borderPaint.color = ContextCompat.getColor(context, R.color.bubble_outline)
        borderPaint.style = Paint.Style.STROKE
        borderPaint.strokeWidth = dp(BOARD_STROKE_DP).toFloat()
        gridPaint.color = ContextCompat.getColor(context, R.color.bubble_grid_line)
        gridPaint.strokeWidth = dp(1f).toFloat()
        routePaint.style = Paint.Style.STROKE
        routePaint.strokeCap = Paint.Cap.ROUND
        routePaint.strokeJoin = Paint.Join.ROUND
        routeInnerPaint.style = Paint.Style.STROKE
        routeInnerPaint.strokeCap = Paint.Cap.ROUND
        routeInnerPaint.strokeJoin = Paint.Join.ROUND
        routeInnerPaint.color = Color.argb(150, 255, 255, 255)
        ringPaint.style = Paint.Style.STROKE
        ringPaint.strokeWidth = dp(3f).toFloat()
        haloPaint.style = Paint.Style.FILL
        burstPaint.style = Paint.Style.FILL
        sprites[BubbleColor.CYAN] = ContextCompat.getDrawable(context, R.drawable.sprite_bubble_cyan)
        sprites[BubbleColor.PINK] = ContextCompat.getDrawable(context, R.drawable.sprite_bubble_pink)
        sprites[BubbleColor.YELLOW] =
            ContextCompat.getDrawable(context, R.drawable.sprite_bubble_yellow)
        sprites[BubbleColor.BLUE] = ContextCompat.getDrawable(context, R.drawable.sprite_bubble_blue)
        currentSprite = ContextCompat.getDrawable(context, R.drawable.sprite_current_swirl)
        isClickable = true
        isFocusable = true
    }

    fun setBoard(state: BoardState) {
        board = state
        invalidate()
    }

    fun playMerge(cells: List<Cell>, color: BubbleColor) {
        mergeAnimator?.cancel()
        mergeCells = cells
        mergeColor = color
        mergeProgress = 0f
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = GameConfig.MERGE_DURATION_MS
        animator.addUpdateListener { value ->
            mergeProgress = value.animatedValue as Float
            invalidate()
        }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                try {
                    mergeCells = emptyList()
                    mergeColor = null
                    mergeProgress = 0f
                    invalidate()
                } catch (e: Exception) {
                    mergeProgress = 0f
                }
            }
        })
        mergeAnimator = animator
        animator.start()
    }

    fun playBlocked() {
        shakeAnimator?.cancel()
        val offset = dp(SHAKE_OFFSET_DP).toFloat()
        val animator = ObjectAnimator.ofFloat(
            this,
            View.TRANSLATION_X,
            0f,
            offset,
            -offset,
            offset * 0.5f,
            0f
        )
        animator.duration = GameConfig.SHAKE_DURATION_MS
        shakeAnimator = animator
        animator.start()
    }

    fun clearAnimations() {
        mergeAnimator?.cancel()
        mergeAnimator = null
        shakeAnimator?.cancel()
        shakeAnimator = null
        translationX = 0f
        mergeCells = emptyList()
        mergeColor = null
        mergeProgress = 0f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availableHeight = MeasureSpec.getSize(heightMeasureSpec)
        val limitWidth = if (availableWidth > 0) min(availableWidth, maxWidthPx) else maxWidthPx
        val limitHeight = if (availableHeight > 0) availableHeight else maxWidthPx * rows / cols
        val byWidth = (limitWidth - 2 * framePx) / cols
        val byHeight = (limitHeight - 2 * framePx) / rows
        val raw = min(byWidth, byHeight)
        cellPx = if (raw < MIN_CELL_PX) MIN_CELL_PX else raw
        setMeasuredDimension(cellPx * cols + 2 * framePx, cellPx * rows + 2 * framePx)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                return true
            }
            MotionEvent.ACTION_UP -> {
                performClick()
                val slop = dp(TAP_SLOP_DP).toFloat()
                if (abs(event.x - downX) <= slop && abs(event.y - downY) <= slop) {
                    dispatchTap(event.x, event.y)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun dispatchTap(x: Float, y: Float) {
        if (cellPx <= 0) {
            return
        }
        val col = ((x - framePx) / cellPx).toInt()
        val row = ((y - framePx) / cellPx).toInt()
        if (row < 0 || col < 0 || row >= rows || col >= cols) {
            return
        }
        onCellTapped?.invoke(Cell(row, col))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val state = board ?: return
        if (cellPx <= 0) {
            return
        }
        val inset = borderPaint.strokeWidth / 2f
        boardRect.set(inset, inset, width - inset, height - inset)
        canvas.drawRoundRect(boardRect, cornerPx, cornerPx, surfacePaint)
        canvas.drawRoundRect(boardRect, cornerPx, cornerPx, borderPaint)
        drawGrid(canvas)
        drawRoutes(canvas, state)
        drawCurrents(canvas, state)
        drawBubbles(canvas, state)
        drawSelection(canvas, state)
        drawBurst(canvas)
    }

    private fun drawGrid(canvas: Canvas) {
        val left = framePx.toFloat()
        val top = framePx.toFloat()
        val right = (framePx + cellPx * cols).toFloat()
        val bottom = (framePx + cellPx * rows).toFloat()
        for (index in 1 until cols) {
            val x = left + index * cellPx
            canvas.drawLine(x, top, x, bottom, gridPaint)
        }
        for (index in 1 until rows) {
            val y = top + index * cellPx
            canvas.drawLine(left, y, right, y, gridPaint)
        }
    }

    private fun drawRoutes(canvas: Canvas, state: BoardState) {
        if (state.links.isEmpty()) {
            return
        }
        routePaint.strokeWidth = cellPx * ROUTE_WIDTH_RATIO
        routeInnerPaint.strokeWidth = cellPx * ROUTE_INNER_RATIO
        for (link in state.links) {
            if (link.path.size < 2) {
                continue
            }
            routePath.reset()
            val first = link.path[0]
            routePath.moveTo(centerX(first), centerY(first))
            for (index in 1 until link.path.size) {
                val step = link.path[index]
                routePath.lineTo(centerX(step), centerY(step))
            }
            routePaint.color = colorFor(link.color)
            routePaint.alpha = ROUTE_ALPHA
            canvas.drawPath(routePath, routePaint)
            canvas.drawPath(routePath, routeInnerPaint)
        }
    }

    private fun drawCurrents(canvas: Canvas, state: BoardState) {
        val sprite = currentSprite ?: return
        val size = (cellPx * CURRENT_RATIO).toInt()
        for (cell in state.currents) {
            sprite.alpha = CURRENT_ALPHA
            drawSprite(canvas, sprite, cell, size)
        }
    }

    private fun drawBubbles(canvas: Canvas, state: BoardState) {
        val baseSize = cellPx * BUBBLE_RATIO
        for (entry in state.bubbles) {
            val sprite = sprites[entry.value] ?: continue
            sprite.alpha = 255
            drawSprite(canvas, sprite, entry.key, baseSize.toInt())
        }
        val popped = mergeCells
        val popColor = mergeColor
        if (popped.isEmpty() || popColor == null) {
            return
        }
        val sprite = sprites[popColor] ?: return
        val size = (baseSize * popScale(mergeProgress)).toInt()
        if (size <= 1) {
            return
        }
        sprite.alpha = (255 * (1f - mergeProgress).coerceIn(0f, 1f)).toInt()
        for (cell in popped) {
            drawSprite(canvas, sprite, cell, size)
        }
        sprite.alpha = 255
    }

    private fun drawSelection(canvas: Canvas, state: BoardState) {
        val selected = state.selected ?: return
        val color = state.bubbles[selected] ?: return
        ringPaint.color = colorFor(color)
        canvas.drawCircle(centerX(selected), centerY(selected), cellPx * RING_RATIO, ringPaint)
        haloPaint.color = colorFor(color)
        haloPaint.alpha = HALO_ALPHA
        for (partner in state.partnersOf(selected)) {
            canvas.drawCircle(centerX(partner), centerY(partner), cellPx * HALO_RATIO, haloPaint)
        }
    }

    private fun drawBurst(canvas: Canvas) {
        val popped = mergeCells
        val popColor = mergeColor
        if (popped.isEmpty() || popColor == null || mergeProgress <= 0f) {
            return
        }
        burstPaint.color = colorFor(popColor)
        burstPaint.alpha = (200 * (1f - mergeProgress)).toInt().coerceIn(0, 255)
        val radius = dp(BURST_RADIUS_DP) * mergeProgress
        val dotRadius = cellPx * BURST_DOT_RATIO * (1f - mergeProgress)
        if (dotRadius <= 0f) {
            return
        }
        for (cell in popped) {
            val cx = centerX(cell)
            val cy = centerY(cell)
            for (index in 0 until BURST_COUNT) {
                val angle = (2.0 * Math.PI * index / BURST_COUNT).toFloat()
                canvas.drawCircle(cx + cos(angle) * radius, cy + sin(angle) * radius, dotRadius, burstPaint)
            }
        }
    }

    private fun drawSprite(canvas: Canvas, sprite: Drawable, cell: Cell, size: Int) {
        if (size <= 0) {
            return
        }
        val intrinsicWidth = sprite.intrinsicWidth
        val intrinsicHeight = sprite.intrinsicHeight
        var drawWidth = size
        var drawHeight = size
        if (intrinsicWidth > 0 && intrinsicHeight > 0) {
            if (intrinsicWidth >= intrinsicHeight) {
                drawHeight = size * intrinsicHeight / intrinsicWidth
            } else {
                drawWidth = size * intrinsicWidth / intrinsicHeight
            }
        }
        val left = (centerX(cell) - drawWidth / 2f).toInt()
        val top = (centerY(cell) - drawHeight / 2f).toInt()
        sprite.setBounds(left, top, left + drawWidth, top + drawHeight)
        sprite.draw(canvas)
    }

    private fun popScale(progress: Float): Float {
        if (progress < POP_PEAK) {
            return 1f + (POP_MAX - 1f) * (progress / POP_PEAK)
        }
        val tail = (progress - POP_PEAK) / (1f - POP_PEAK)
        return POP_MAX * (1f - tail)
    }

    private fun centerX(cell: Cell): Float = framePx + cell.col * cellPx + cellPx / 2f

    private fun centerY(cell: Cell): Float = framePx + cell.row * cellPx + cellPx / 2f

    private fun colorFor(color: BubbleColor): Int {
        val resource = when (color) {
            BubbleColor.CYAN -> R.color.bubble_primary
            BubbleColor.PINK -> R.color.bubble_accent
            BubbleColor.YELLOW -> R.color.bubble_secondary_dark
            BubbleColor.BLUE -> R.color.bubble_blue_soft
        }
        return ContextCompat.getColor(context, resource)
    }

    private fun dp(value: Float): Int = (resources.displayMetrics.density * value).toInt()

    companion object {
        private const val BOARD_FRAME_DP = 10f
        private const val BOARD_MAX_WIDTH_DP = 380f
        private const val BOARD_CORNER_DP = 24f
        private const val BOARD_STROKE_DP = 2f
        private const val SHAKE_OFFSET_DP = 6f
        private const val TAP_SLOP_DP = 24f
        private const val BURST_RADIUS_DP = 18f
        private const val MIN_CELL_PX = 24
        private const val BURST_COUNT = 8
        private const val ROUTE_WIDTH_RATIO = 0.18f
        private const val ROUTE_INNER_RATIO = 0.07f
        private const val ROUTE_ALPHA = 230
        private const val CURRENT_RATIO = 0.62f
        private const val CURRENT_ALPHA = 150
        private const val BUBBLE_RATIO = 0.78f
        private const val RING_RATIO = 0.48f
        private const val HALO_RATIO = 0.44f
        private const val HALO_ALPHA = 60
        private const val POP_PEAK = 0.35f
        private const val POP_MAX = 1.25f
        private const val BURST_DOT_RATIO = 0.09f
    }
}
