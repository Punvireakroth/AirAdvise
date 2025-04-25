package com.example.airadvise.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.example.airadvise.R
import com.example.airadvise.utils.AQIUtils
import kotlin.math.cos
import kotlin.math.sin

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
    private val needlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val needleBasePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val arcRect = RectF()
    
    // AQI segment colors
    private val segmentColors = arrayOf(
        Color.parseColor("#8BC34A"),  // Green - Good
        Color.parseColor("#FDD835"),  // Yellow - Moderate
        Color.parseColor("#FB8C00"),  // Orange - Unhealthy for sensitive groups
        Color.parseColor("#E53935"),  // Red - Unhealthy
        Color.parseColor("#8E24AA"),  // Purple - Very unhealthy
        Color.parseColor("#6D4C41")   // Brown - Hazardous
    )
    
    // AQI segment thresholds
    private val thresholds = arrayOf(0, 51, 101, 151, 201, 301, 501)

    init {
        arcPaint.style = Paint.Style.STROKE
        arcPaint.strokeWidth = 50f
        arcPaint.strokeCap = Paint.Cap.ROUND

        needlePaint.style = Paint.Style.FILL
        needlePaint.color = Color.DKGRAY
        needlePaint.strokeWidth = 5f
        needlePaint.isAntiAlias = true

        needleBasePaint.style = Paint.Style.FILL
        needleBasePaint.color = Color.DKGRAY
        needleBasePaint.isAntiAlias = true

        textPaint.textSize = 100f
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.color = Color.BLACK
        textPaint.isFakeBoldText = true

        labelPaint.textSize = 45f
        labelPaint.textAlign = Paint.Align.CENTER
        labelPaint.color = Color.GRAY
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
                invalidate()
            }
            
            // Start the animation
            animator.start()
        } else {
            // Immediate update without animation
            currentValue = aqi
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height - 80f  // Move center point lower for half-circle
        
        // Size of the half-circle gauge
        val radius = Math.min(width, height * 1.6f) / 2 - 40f
        
        // Draw colored segments - half circle
        arcRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        
        // Half-circle parameters
        val totalArcAngle = 180f            // Full half-circle
        val startAngle = 180f               // Start from left (180 degrees)
        
        // Calculate total range for scaling
        val totalRange = thresholds.last() - thresholds.first()
        
        // Draw each segment
        for (i in 0 until segmentColors.size) {
            val segmentStartValue = thresholds[i]
            val segmentEndValue = thresholds[i + 1]
            val segmentRange = segmentEndValue - segmentStartValue
            
            // Calculate angles for half-circle
            val segmentStartAngle = startAngle + totalArcAngle * (segmentStartValue.toFloat() / totalRange)
            val sweepAngle = totalArcAngle * (segmentRange.toFloat() / totalRange)
            
            // Set color for this segment
            arcPaint.color = segmentColors[i]
            
            // Draw segment
            canvas.drawArc(arcRect, segmentStartAngle, sweepAngle, false, arcPaint)
        }
        
        // Calculate needle angle based on current value (for half-circle)
        val currentValueCapped = currentValue.coerceAtMost(thresholds.last())
        val angleRad = Math.toRadians(
            startAngle + (totalArcAngle * currentValueCapped / totalRange).toDouble()
        )
        
        // Calculate needle endpoints
        val needleLength = radius - 70f  // Slightly shorter than radius
        val needleEndX = centerX + (needleLength * cos(angleRad)).toFloat()
        val needleEndY = centerY + (needleLength * sin(angleRad)).toFloat()
        
        // Draw needle
        val needleBaseRadius = 15f
        
        // Draw base circle of needle
        canvas.drawCircle(centerX, centerY, needleBaseRadius, needleBasePaint)
        
        // Create and draw needle
        val needlePath = Path()
        needlePath.moveTo(centerX, centerY)
        needlePath.lineTo(needleEndX, needleEndY)
        canvas.drawPath(needlePath, needlePaint)
        
        // Draw arrow at end of needle
        val arrowSize = 42f
        val arrowPath = Path()
        arrowPath.moveTo(needleEndX, needleEndY)
        arrowPath.lineTo(
            needleEndX + arrowSize * cos(angleRad + Math.PI * 3/4).toFloat(), 
            needleEndY + arrowSize * sin(angleRad + Math.PI * 3/4).toFloat()
        )
        arrowPath.lineTo(
            needleEndX + arrowSize * cos(angleRad - Math.PI * 3/4).toFloat(), 
            needleEndY + arrowSize * sin(angleRad - Math.PI * 3/4).toFloat()
        )
        arrowPath.close()
        canvas.drawPath(arrowPath, needlePaint)
        
        // Draw AQI value text (moved slightly up from center)
        canvas.drawText(
            currentValue.toString(),
            centerX,
            centerY - radius/2 + 40,
            textPaint
        )

        // Draw AQI category text (below number)
        canvas.drawText(
            AQIUtils.getAQICategory(context, currentValue),
            centerX,
            centerY - radius/2 + 110,
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