package com.example.airadvise.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.airadvise.R
import com.example.airadvise.utils.AQIUtils
import java.util.jar.Attributes

class AQIGaugeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr){
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

    fun setAQI(aqi: Int) {
        aqiValue = aqi
        arcPaint.color = AQIUtils.getAQIColor(context, aqi)
        textPaint.color = AQIUtils.getAQIColor(context, aqi)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val padding = 60f

        // Draw background arc
        arcRect.set(padding, padding, width - padding, height - padding)
        canvas.drawArc(arcRect, 135f, 270f, false, backgroundArcPaint)

        // Calculate progress
        val maxAqi = 500f // Max AQI value
        val progress = (aqiValue / maxAqi).coerceAtMost(1f)
        val sweepAngle = 270f * progress

        // Draw progress arc
        canvas.drawArc(arcRect, 135f, sweepAngle, false, arcPaint)

        // Draw AQI value text
        canvas.drawText(
            aqiValue.toString(),
            width / 2,
            height / 2 + textPaint.textSize / 3,
            textPaint
        )

        // Draw AQI category text
        canvas.drawText(
            AQIUtils.getAQICategory(context, aqiValue),
            width / 2,
            height / 2 + textPaint.textSize + labelPaint.textSize,
            labelPaint
        )
    }
}