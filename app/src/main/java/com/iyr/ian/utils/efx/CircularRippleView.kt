package com.iyr.ian.utils.efx

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.iyr.ian.R


class CircularRippleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rippleRadius = 0f
    private var maxRippleRadius = 0f
    var initialDiameter = 0f
        set(value) {
            field = value
            startRippleAnimation()
        }
    var rippleSpeed = 2000L
        set(value) {
            field = value
            startRippleAnimation()
        }

    init {
        paint.style = Paint.Style.FILL
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CircularRippleView,
            0, 0
        ).apply {
            try {
                initialDiameter = getDimension(R.styleable.CircularRippleView_initialDiameter, 0f)
                rippleSpeed = getInt(R.styleable.CircularRippleView_rippleSpeed, 2000).toLong()
            } finally {
                recycle()
            }
        }
        startRippleAnimation()
    }

    private fun startRippleAnimation() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = rippleSpeed
        animator.repeatCount = ValueAnimator.INFINITE
        animator.repeatMode = ValueAnimator.REVERSE
        animator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Float
            rippleRadius = initialDiameter / 2 + (maxRippleRadius - initialDiameter / 2) * animatedValue
            invalidate()
        }
        animator.start()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        maxRippleRadius = (w.coerceAtMost(h) / 2).toFloat()

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f

        val gradient = RadialGradient(
            centerX, centerY, rippleRadius,
            intArrayOf(
                ContextCompat.getColor(context, R.color.map_ripple_fill_color),
                ContextCompat.getColor(context, R.color.map_ripple_stroke_color)
            ),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient

        canvas.drawCircle(centerX, centerY, rippleRadius, paint)
    }
}

