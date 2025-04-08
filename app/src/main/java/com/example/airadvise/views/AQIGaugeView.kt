package com.example.airadvise.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.airadvise.R
import com.example.airadvise.utils.AQIUtils

class AQIGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    
    // Track the current animated value
    private var currentValue = 0
    
    // The animator for smooth transitions
    private val animator = ValueAnimator.ofInt(0, 0)
    
    private var aqiValue: Int = 0
    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val backgroundArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arcRect = RectF()

    init {
        arcPaint.style = Paint.Style.STROKE
        arcPaint.strokeWidth = 40f

        backgroundArcPaint.style = Paint.Style.STROKE
        backgroundArcPaint.strokeWidth = 40f
        backgroundArcPaint.color = ContextCompat.getColor(context, R.color.gray_light)

        textPaint.textSize = 80f
        textPaint.textAlign = Paint.Align.CENTER

        labelPaint.textSize = 30f
        labelPaint.textAlign = Paint.Align.CENTER
    }

    /**
     * Set the AQI value with optional animation
     * 
     * @param aqi The Air Quality Index value to display
     * @param animate Whether to animate the change (default: true)
     */
    fun setAQI(aqi: Int, animate: Boolean = true) {
        aqiValue = aqi
        
        if (animate && isAttachedToWindow && currentValue != aqi) {
            // Cancel any ongoing animation
            animator.cancel()
            
            // Set up a new animation from current to target value
            animator.setIntValues(currentValue, aqi)
            animator.duration = 1000
            animator.interpolator = AccelerateDecelerateInterpolator()
            
            // Update the view during animation
            animator.addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Int
                currentValue = animatedValue
                arcPaint.color = AQIUtils.getAQIColor(context, animatedValue)
                textPaint.color = AQIUtils.getAQIColor(context, animatedValue)
                invalidate()
            }
            
            // Start the animation
            animator.start()
        } else {
            // Immediate update without animation
            currentValue = aqi
            arcPaint.color = AQIUtils.getAQIColor(context, aqi)
            textPaint.color = AQIUtils.getAQIColor(context, aqi)
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 60f

        // Draw background arc
        arcRect.set(padding, padding, width - padding, height - padding)
        canvas.drawArc(arcRect, 135f, 270f, false, backgroundArcPaint)

        // Calculate progress using the current animated value
        val maxAqi = 500f // Max AQI value
        val progress = (currentValue / maxAqi).coerceAtMost(1f)
        val sweepAngle = 270f * progress

        // Draw progress arc
        canvas.drawArc(arcRect, 135f, sweepAngle, false, arcPaint)

        // Draw AQI value text using the current animated value
        canvas.drawText(
            currentValue.toString(),
            width / 2,
            height / 2 + textPaint.textSize / 3,
            textPaint
        )

        // Draw AQI category text using the current animated value
        canvas.drawText(
            AQIUtils.getAQICategory(context, currentValue),
            width / 2,
            height / 2 + textPaint.textSize + labelPaint.textSize,
            labelPaint
        )
    }
    
    /**
     * Clean up resources when the view is detached
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }
}