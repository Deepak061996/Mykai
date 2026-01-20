package com.mykaimeal.planner.di

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class MacroPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    data class ChartData(
        val label: String,
        val percentage: Float,
        val color: Int
    )

    // Add dynamic title property
    private var chartTitle = "Balanced Macro Distribution"

    // Make chartData mutable and provide default values
    private var chartData = listOf(
        ChartData("Fat", 25f, Color.parseColor("#4ECDC4")), // Teal
        ChartData("Protein", 13f, Color.parseColor("#B565A7")), // Purple
        ChartData("Carbs", 62f, Color.parseColor("#FF9F43")) // Orange
    )

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 35f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textAlign = Paint.Align.LEFT
        textSize = 42f
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 45f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val rect = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f

    // Public method to update chart data
    fun setChartData(newData: List<ChartData>,title:String) {
        chartData = newData
        chartTitle = title
        invalidate() // Trigger a redraw
    }

    // Convenience method to update with individual values
    fun updateMacros(fat: Float, protein: Float, carbs: Float) {
        val total = fat + protein + carbs
        if (total > 0) {
            chartData = listOf(
                ChartData("Fat", (fat / total) * 100f, Color.parseColor("#4ECDC4")),
                ChartData("Protein", (protein / total) * 100f, Color.parseColor("#B565A7")),
                ChartData("Carbs", (carbs / total) * 100f, Color.parseColor("#FF9F43"))
            )
            invalidate()
        }
    }

    // Method to update with percentages directly
    fun updateMacroPercentages(fatPercentage: Float, proteinPercentage: Float, carbsPercentage: Float) {
        chartData = listOf(
            ChartData("Fat", fatPercentage, Color.parseColor("#4ECDC4")),
            ChartData("Protein", proteinPercentage, Color.parseColor("#B565A7")),
            ChartData("Carbs", carbsPercentage, Color.parseColor("#FF9F43"))
        )
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        centerX = w / 2f
        centerY = h / 2f + 40f // Offset down slightly for title
        radius = minOf(w, h) * 0.3f

        rect.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw title
        canvas.drawText(
            chartTitle,
            centerX,
            80f,
            titlePaint
        )

        // Draw pie chart
        var startAngle = -90f // Start from top

        chartData.forEach { data ->
            val sweepAngle = (data.percentage / 100f) * 360f

            paint.color = data.color
            canvas.drawArc(rect, startAngle, sweepAngle, true, paint)

            // Calculate label position with increased margin
            val labelAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())


//            val labelRadius = if (data.label == "Carbs") {
//                radius + 60f // Extra margin for Carbs label
//            } else {
//                radius + 60f // Standard margin for other labels
//            }

            // Dynamic label radius based on slice size and position
            val labelRadius = when {
                data.percentage > 40f -> radius + 80f  // Larger slices need more space
                data.percentage < 15f -> radius + 40f  // Smaller slices can be closer
                else -> radius + 60f                   // Medium slices use standard spacing
            }

            val labelX = centerX + (labelRadius * cos(labelAngle)).toFloat()
            val labelY = centerY + (labelRadius * sin(labelAngle)).toFloat()

            // Draw percentage on the slice
            val textAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
            val textRadius = radius * 0.7f
            val textX = centerX + (textRadius * Math.cos(textAngle)).toFloat()
            val textY = centerY + (textRadius * Math.sin(textAngle)).toFloat()

            canvas.drawText("${data.percentage.toInt()}%", textX, textY + 15f, textPaint)

            // Draw label outside with margin
            canvas.drawText("${data.label} (${data.percentage.toInt()}%)", labelX, labelY, labelPaint)

            startAngle += sweepAngle
        }
    }
}