/*
package com.mykaimeal.planner.di

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewParent
import kotlin.math.abs

class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface TimePickerListener {
        fun onTimeChanged(hours: Int, minutes: Int)
    }

    private var listener: TimePickerListener? = null

    // Colors
    private val selectedTextColor = Color.parseColor("#4CAF50") // Green color
    private val normalTextColor = Color.parseColor("#999999")   // Gray color
    private val backgroundColor = Color.parseColor("#FFFFFF")   // Light gray background

    // Paint objects
    private val selectedTextPaint = Paint().apply {
        color = selectedTextColor
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val normalTextPaint = Paint().apply {
        color = normalTextColor
        textSize = 36f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val labelPaint = Paint().apply {
        color = normalTextColor
        textSize = 32f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }

    private val backgroundPaint = Paint().apply {
        color = backgroundColor
        isAntiAlias = true
    }

    // Selection highlight
    private val highlightPaint = Paint().apply {
        color = Color.parseColor("#33CDF3E1")
        isAntiAlias = true
    }

    // Current values
    private var selectedHours = 0
    private var selectedMinutes = 15

    // Layout properties
    private var itemHeight = 0f
    private var centerY = 0f
    private var hoursX = 0f
    private var minutesX = 0f
    private var hoursLabelX = 0f
    private var minutesLabelX = 0f

    // Touch handling
    private var isDragging = false
    private var lastTouchY = 0f
    private var initialTouchY = 0f
    private var activeColumn = 0 // 0 for hours, 1 for minutes
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // Scroll offset for smooth animation
    private var hoursScrollOffset = 0f
    private var minutesScrollOffset = 0f

    init {
        setBackgroundColor(backgroundColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        itemHeight = h / 7f // Show 7 items vertically
        centerY = h / 2f

        // Layout columns
        hoursX = w * 0.25f
        hoursLabelX = w * 0.45f
        minutesX = w * 0.65f
        minutesLabelX = w * 0.85f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Draw background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)
        // Draw selection highlight
        val highlightTop = centerY - itemHeight / 2
        val highlightBottom = centerY + itemHeight / 2
        canvas.drawRoundRect(
            50f, highlightTop, width - 50f, highlightBottom,
            20f, 20f, highlightPaint
        )
        // Draw hours column
        drawHoursColumn(canvas)
        // Draw minutes column
        drawMinutesColumn(canvas)
        // Draw labels
        canvas.drawText("hours", hoursLabelX, centerY + 10f, labelPaint)
        canvas.drawText("min", minutesLabelX, centerY + 10f, labelPaint)
    }

//    private fun drawHoursColumn(canvas: Canvas) {
//        val visibleRange = 3 // Show 3 items above and below selected
//        for (i in -visibleRange..visibleRange) {
//            val hour = (selectedHours + i + 24) % 24
//            val y = centerY + i * itemHeight + hoursScrollOffset
//            // Skip if outside visible area
//            if (y < -itemHeight || y > height + itemHeight) continue
//
//            val distance = abs(y - centerY)
//
//            val alpha = when {
//                distance < itemHeight / 2 -> 1.0f
//                distance < itemHeight -> 0.7f
//                distance < itemHeight * 1.5f -> 0.5f
//                else -> 0.3f
//            }
//            val paint = if (distance < itemHeight / 2) selectedTextPaint else normalTextPaint
//            paint.alpha = (alpha * 255).toInt()
//            val displayHour = when {
//                hour == 0 -> 12
//                hour > 12 -> hour - 12
//                else -> hour
//            }
//            canvas.drawText(displayHour.toString(), hoursX, y + 15f, paint)
//        }
//    }


    private fun drawHoursColumn(canvas: Canvas) {
        val visibleRange = 3 // ज्यादा items दिखाएंगे ताकि wheel feel आए
        val angleStep = 20f   // हर item के बीच 20 degree का gap
        val radius = height / 2.5f

        for (i in -visibleRange..visibleRange) {
            val hour24 = (selectedHours + i + 24) % 24
            val angle = i * angleStep + (hoursScrollOffset / itemHeight * angleStep)

            // Circle projection
            val y = centerY + radius * Math.sin(Math.toRadians(angle.toDouble())).toFloat()

            // Scale और transparency
            val scale = 1f - 0.5f * (abs(angle) / 90f)
            val alpha = (scale * 255).toInt().coerceIn(50, 255)

            val paint = if (abs(angle) < angleStep / 2) selectedTextPaint else normalTextPaint
            paint.alpha = alpha
            paint.textSize = (if (abs(angle) < angleStep / 2) 48f else 36f) * scale

            // ✅ 12-hour format display
            val displayHour = when {
                hour24 == 0 -> 12
                hour24 > 12 -> hour24 - 12
                else -> hour24
            }

            canvas.drawText(hour24.toString(), hoursX, y, paint)
        }
    }


//    private fun drawMinutesColumn(canvas: Canvas) {
//        val visibleRange = 3
//
//        for (i in -visibleRange..visibleRange) {
//            val minute = (selectedMinutes + i + 60) % 60
//            val y = centerY + i * itemHeight + minutesScrollOffset
//
//            // Skip if outside visible area
//            if (y < -itemHeight || y > height + itemHeight) continue
//
//            val distance = abs(y - centerY)
//            val alpha = when {
//                distance < itemHeight / 2 -> 1.0f
//                distance < itemHeight -> 0.7f
//                distance < itemHeight * 1.5f -> 0.5f
//                else -> 0.3f
//            }
//
//            val paint = if (distance < itemHeight / 2) selectedTextPaint else normalTextPaint
//            paint.alpha = (alpha * 255).toInt()
//
//            canvas.drawText(minute.toString().padStart(2, '0'), minutesX, y + 15f, paint)
//        }
//    }
    private fun drawMinutesColumn(canvas: Canvas) {
        val visibleRange = 3   // ज्यादा items दिखाएंगे ताकि wheel feel आए
        val angleStep = 20f    // हर item के बीच angle gap (20 degree)
        val radius = height / 2.5f

        for (i in -visibleRange..visibleRange) {
            val minute = (selectedMinutes + i + 60) % 60
            val angle = i * angleStep + (minutesScrollOffset / itemHeight * angleStep)
            // Circle projection
            val y = centerY + radius * Math.sin(Math.toRadians(angle.toDouble())).toFloat()
            // Scale और transparency
            val scale = 1f - 0.5f * (abs(angle) / 90f)
            val alpha = (scale * 255).toInt().coerceIn(50, 255)
            val paint = if (abs(angle) < angleStep / 2) selectedTextPaint else normalTextPaint
            paint.alpha = alpha
            paint.textSize = (if (abs(angle) < angleStep / 2) 48f else 36f) * scale
            // ✅ 2-digit format (00, 05, 10...)
            canvas.drawText(minute.toString().padStart(2, '0'), minutesX, y, paint)
        }
   }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                initialTouchY = event.y
                isDragging = false

                // Determine which column was touched
                activeColumn = if (event.x < width / 2) 0 else 1

                // Request parent to not intercept touch events
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - lastTouchY
                val totalDeltaY = event.y - initialTouchY

                if (!isDragging && abs(totalDeltaY) > touchSlop) {
                    isDragging = true
                    // Once we start dragging, definitely don't let parent intercept
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (isDragging) {
                    if (activeColumn == 0) {
                        hoursScrollOffset += deltaY
                        handleHoursScroll()
                    } else {
                        minutesScrollOffset += deltaY
                        handleMinutesScroll()
                    }
                    lastTouchY = event.y
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Allow parent to intercept touch events again
                parent?.requestDisallowInterceptTouchEvent(false)

                if (isDragging) {
                    // Snap to nearest item
                    snapToNearest()
                }
                isDragging = false
                return true
            }
        }

        return super.onTouchEvent(event)
    }

    // onInterceptTouchEvent is not available in View class, only in ViewGroup
    // So we'll handle everything in onTouchEvent only

    private fun handleHoursScroll() {
        while (hoursScrollOffset >= itemHeight) {
            hoursScrollOffset -= itemHeight
            selectedHours = (selectedHours - 1 + 24) % 24
            notifyTimeChanged()
        }

        while (hoursScrollOffset <= -itemHeight) {
            hoursScrollOffset += itemHeight
            selectedHours = (selectedHours + 1) % 24
            notifyTimeChanged()
        }
    }

    private fun handleMinutesScroll() {
        while (minutesScrollOffset >= itemHeight) {
            minutesScrollOffset -= itemHeight
            selectedMinutes = (selectedMinutes - 5 + 60) % 60  // 5-minute steps
            notifyTimeChanged()
        }

        while (minutesScrollOffset <= -itemHeight) {
            minutesScrollOffset += itemHeight
            selectedMinutes = (selectedMinutes + 5) % 60      // 5-minute steps
            notifyTimeChanged()
        }
    }

    private fun snapToNearest() {
        // Animate scroll offsets back to 0
        post(object : Runnable {
            override fun run() {
                var needsUpdate = false

                if (abs(hoursScrollOffset) > 0.5f) {
                    hoursScrollOffset *= 0.8f
                    needsUpdate = true
                } else {
                    hoursScrollOffset = 0f
                }

                if (abs(minutesScrollOffset) > 0.5f) {
                    minutesScrollOffset *= 0.8f
                    needsUpdate = true
                } else {
                    minutesScrollOffset = 0f
                }

                if (needsUpdate) {
                    invalidate()
                    postDelayed(this, 16)
                }
            }
        })
    }

    private fun notifyTimeChanged() {
        listener?.onTimeChanged(selectedHours, selectedMinutes)
    }

    fun setTime(hours: Int, minutes: Int) {
        selectedHours = hours.coerceIn(0, 23)
        selectedMinutes = minutes.coerceIn(0, 59)
        hoursScrollOffset = 0f
        minutesScrollOffset = 0f
        invalidate()
        notifyTimeChanged()
    }

    fun getHours(): Int = selectedHours
    fun getMinutes(): Int = selectedMinutes

    fun setOnTimeChangedListener(listener: TimePickerListener) {
        this.listener = listener
    }
}*/



/// ye vala kam kar rha hai
/*
package com.mykaimeal.planner.di

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs

class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface TimePickerListener {
        fun onTimeChanged(hours: Int, minutes: Int)
    }

    private var listener: TimePickerListener? = null

    // Colors
    private val selectedTextColor = Color.parseColor("#4CAF50") // Green color
    private val normalTextColor = Color.parseColor("#999999")   // Gray color
    private val backgroundColor = Color.parseColor("#FFFFFF")

    // Paint objects
    private val selectedTextPaint = Paint().apply {
        color = selectedTextColor
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val normalTextPaint = Paint().apply {
        color = normalTextColor
        textSize = 40f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    private val labelPaint = Paint().apply {
        color = normalTextColor
        textSize = 32f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT
    }

    // Background highlight for selected item
    private val highlightPaint = Paint().apply {
        color = Color.parseColor("#F0F8F0") // Very light green background
        isAntiAlias = true
    }

    // Current values
    private var selectedHours = 0
    private var selectedMinutes = 15

    // Layout properties
    private var itemHeight = 0f
    private var centerY = 0f
    private var hoursX = 0f
    private var minutesX = 0f

    // Touch handling
    private var isDragging = false
    private var lastTouchY = 0f
    private var initialTouchY = 0f
    private var activeColumn = 0 // 0 for hours, 1 for minutes
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // Scroll offset for smooth animation
    private var hoursScrollOffset = 0f
    private var minutesScrollOffset = 0f

    init {
        setBackgroundColor(backgroundColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        itemHeight = h / 7f // Show 7 items vertically
        centerY = h / 2f

        // Layout columns
        hoursX = w * 0.3f
        minutesX = w * 0.7f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background highlight for selected row
        val highlightRect = RectF(
            50f,
            centerY - itemHeight / 2,
            width - 50f,
            centerY + itemHeight / 2
        )
        canvas.drawRoundRect(highlightRect, 15f, 15f, highlightPaint)

        // Draw hours column
        drawColumn(canvas, true)

        // Draw minutes column
        drawColumn(canvas, false)

        // Draw labels next to selected values
        canvas.drawText("hours", hoursX +60f+ 40f, centerY + 12f, labelPaint)
        canvas.drawText("min", minutesX +60f+ 30f, centerY + 12f, labelPaint)
    }

    private fun drawColumn1(canvas: Canvas, isHours: Boolean) {
        val x = if (isHours) hoursX else minutesX
        val selectedValue = if (isHours) selectedHours else selectedMinutes
        val scrollOffset = if (isHours) hoursScrollOffset else minutesScrollOffset
        val maxValue = if (isHours) 24 else 60
        val step = if (isHours) 1 else 5

        val visibleRange = 3 // Show 3 items above and below selected

        for (i in -visibleRange..visibleRange) {
            val value = if (isHours) {
                (selectedValue + i + maxValue) % maxValue
            } else {
                ((selectedValue + i * step + maxValue) % maxValue).let {
                    // Round to nearest 5 for minutes
                    (it / 5) * 5
                }
            }

            // Calculate position
            val y = centerY + i * itemHeight + scrollOffset

            // Skip items outside visible area
            if (y < -itemHeight || y > height + itemHeight) continue

            // Calculate distance from center for styling
            val distanceFromCenter = abs(y - centerY)

            // Choose paint and alpha based on distance
            val paint: Paint
            val alpha: Float

            when {
                distanceFromCenter < itemHeight * 0.5f -> {
                    // Selected item - bold and green
                    paint = selectedTextPaint
                    alpha = 1.0f
                }
                distanceFromCenter < itemHeight * 1.5f -> {
                    // Near items - normal
                    paint = normalTextPaint
                    alpha = 0.7f
                }
                else -> {
                    // Far items - faded
                    paint = normalTextPaint
                    alpha = 0.4f
                }
            }

            // Apply alpha
            paint.alpha = (alpha * 255).toInt()

            // Format display value
            val displayValue = if (isHours) {
                value.toString()
            } else {
                value.toString().padStart(2, '0')
            }

            // Draw the text
            canvas.drawText(displayValue, x, y + 15f, paint)
        }
    }


    private fun drawColumn(canvas: Canvas, isHours: Boolean) {
        val x = if (isHours) hoursX else minutesX
        val selectedValue = if (isHours) selectedHours else selectedMinutes
        val scrollOffset = if (isHours) hoursScrollOffset else minutesScrollOffset
        val maxValue = if (isHours) 24 else 60
        val step = if (isHours) 1 else 5
        val visibleRange = 4 // कितने ऊपर-नीचे values दिखें
        val radius = itemHeight * 4 // arc radius

        for (i in -visibleRange..visibleRange) {
            val value = if (isHours) {
                (selectedValue + i + maxValue) % maxValue
            } else {
                ((selectedValue + i * step + maxValue) % maxValue).let { (it / 5) * 5 }
            }

            // Linear position
            val y = centerY + i * itemHeight + scrollOffset
            if (y < -itemHeight || y > height + itemHeight) continue

            // Arc projection (wheel look)
            val distance = (y - centerY) / itemHeight
            val angle = distance * (Math.PI / 10) // tilt factor
            val curvedY = centerY + Math.sin(angle) * radius
            val scale = Math.cos(angle).toFloat().coerceAtLeast(0.5f) // दूर जाने पर छोटा

            // Alpha fade
            val alpha = (scale).coerceIn(0.3f, 1f)

            // Paint
            val paint = if (i == 0) selectedTextPaint else normalTextPaint
            paint.alpha = (alpha * 255).toInt()

            // Text
            val displayValue = if (isHours) value.toString() else value.toString().padStart(2, '0')

            // Draw with scaling
            canvas.save()
            canvas.scale(scale, scale, x, curvedY.toFloat())
            canvas.drawText(displayValue, x, (curvedY + 15f).toFloat(), paint)
            canvas.restore()
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                initialTouchY = event.y
                isDragging = false

                // Determine which column was touched
                activeColumn = if (event.x < width / 2) 0 else 1
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - lastTouchY
                val totalDeltaY = event.y - initialTouchY

                if (!isDragging && abs(totalDeltaY) > touchSlop) {
                    isDragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }

                if (isDragging) {
                    if (activeColumn == 0) {
                        hoursScrollOffset += deltaY
                        handleHoursScroll()
                    } else {
                        minutesScrollOffset += deltaY
                        handleMinutesScroll()
                    }
                    lastTouchY = event.y
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (isDragging) {
                    snapToNearest()
                }
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleHoursScroll() {
        while (hoursScrollOffset >= itemHeight) {
            hoursScrollOffset -= itemHeight
            selectedHours = (selectedHours - 1 + 24) % 24
            notifyTimeChanged()
        }

        while (hoursScrollOffset <= -itemHeight) {
            hoursScrollOffset += itemHeight
            selectedHours = (selectedHours + 1) % 24
            notifyTimeChanged()
        }
    }

    private fun handleMinutesScroll() {
        while (minutesScrollOffset >= itemHeight) {
            minutesScrollOffset -= itemHeight
            selectedMinutes = (selectedMinutes - 5 + 60) % 60
            notifyTimeChanged()
        }

        while (minutesScrollOffset <= -itemHeight) {
            minutesScrollOffset += itemHeight
            selectedMinutes = (selectedMinutes + 5) % 60
            notifyTimeChanged()
        }
    }

    private fun snapToNearest() {
        post(object : Runnable {
            override fun run() {
                var needsUpdate = false

                if (abs(hoursScrollOffset) > 0.5f) {
                    hoursScrollOffset *= 0.8f
                    needsUpdate = true
                } else {
                    hoursScrollOffset = 0f
                }

                if (abs(minutesScrollOffset) > 0.5f) {
                    minutesScrollOffset *= 0.8f
                    needsUpdate = true
                } else {
                    minutesScrollOffset = 0f
                }

                if (needsUpdate) {
                    invalidate()
                    postDelayed(this, 16)
                }
            }
        })
    }

    private fun notifyTimeChanged() {
        listener?.onTimeChanged(selectedHours, selectedMinutes)
    }

    // Public methods
    fun setTime(hours: Int, minutes: Int) {
        selectedHours = hours.coerceIn(0, 23)
        selectedMinutes = (minutes / 5) * 5 // Round to nearest 5
        hoursScrollOffset = 0f
        minutesScrollOffset = 0f
        invalidate()
        notifyTimeChanged()
    }

    fun getHours(): Int = selectedHours
    fun getMinutes(): Int = selectedMinutes

    fun setOnTimeChangedListener(listener: TimePickerListener) {
        this.listener = listener
    }
}*/
package com.mykaimeal.planner.di

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// --- Simple Adapter class ---
class TextWheelAdapter(private val values: List<String>) {
    fun getItem(index: Int): String = values[index % values.size]
    fun getCount(): Int = values.size
}

class TimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface TimePickerListener {
        fun onTimeChanged(hours: Int, minutes: Int)
    }

    private var listener: TimePickerListener? = null

    // Colors
    private val selectedTextColor = Color.parseColor("#4CAF50") // Green
    private val normalTextColor = Color.parseColor("#999999")   // Gray
    private val backgroundColor = Color.parseColor("#FFFFFF")

    // Paints
    private val selectedTextPaint = Paint().apply {
        color = selectedTextColor
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val normalTextPaint = Paint().apply {
        color = normalTextColor
        textSize = 40f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT
    }

    private val labelPaint = Paint().apply {
        color = normalTextColor
        textSize = 48f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
        typeface = Typeface.DEFAULT
    }

    private val highlightPaint = Paint().apply {
        color = Color.parseColor("#F0F8F0")
        isAntiAlias = true
    }

    // --- Adapters ---
    private val hoursAdapter = TextWheelAdapter((0..23).map { it.toString() })
    private val minutesAdapter = TextWheelAdapter((0..59 step 5).map { it.toString()/*.padStart(2, '0')*/ })

    // Values
    private var selectedHours = 0
    private var selectedMinutes = 15

    // Layout
    private var itemHeight = 0f
    private var centerY = 0f
    private var hoursX = 0f
    private var minutesX = 0f

    // Touch
    private var isDragging = false
    private var lastTouchY = 0f
    private var initialTouchY = 0f
    private var activeColumn = 0
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    // Scroll offset
    private var hoursScrollOffset = 0f
    private var minutesScrollOffset = 0f

    init {
        setBackgroundColor(backgroundColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        itemHeight = h / 9f   // पहले 7f था, अब 9f कर दिया
        centerY = h / 2f
        hoursX = w * 0.3f
        minutesX = w * 0.7f
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Highlight bar
        val highlightRect = RectF(
            50f,
            centerY - itemHeight / 2,
            width - 50f,
            centerY + itemHeight / 2
        )
        canvas.drawRoundRect(highlightRect, 15f, 15f, highlightPaint)

        // Hours + Minutes
        drawColumn(canvas, true)
        drawColumn(canvas, false)

        // Labels
        canvas.drawText("hours", hoursX + 100f, centerY + 12f, labelPaint)
        canvas.drawText("min", minutesX + 90f, centerY + 12f, labelPaint)
    }

    private fun drawColumn(canvas: Canvas, isHours: Boolean) {
        val x = if (isHours) hoursX else minutesX
        val selectedValue = if (isHours) selectedHours else selectedMinutes
        val scrollOffset = if (isHours) hoursScrollOffset else minutesScrollOffset
        val adapter = if (isHours) hoursAdapter else minutesAdapter
        val step = if (isHours) 1 else 5
        val visibleRange = 4
        val radius = itemHeight * 4 // wheel radius

        for (i in -visibleRange..visibleRange) {
            val index = if (isHours) {
                (selectedValue + i + adapter.getCount()) % adapter.getCount()
            } else {
                ((selectedValue / step + i + adapter.getCount()) % adapter.getCount())
            }

            val displayValue = adapter.getItem(index)

            val y = centerY + i * itemHeight + scrollOffset
            if (y < -itemHeight || y > height + itemHeight) continue

            // --- Wheel projection (arc look) ---
            val distance = (y - centerY) / itemHeight
            val angle = distance * (Math.PI / 10) // arc tilt
            val curvedY = centerY + sin(angle) * radius
            val scale = cos(angle).toFloat().coerceAtLeast(0.5f)

            // Fade alpha
            val alpha = (scale).coerceIn(0.3f, 1f)

            val paint = if (i == 0) selectedTextPaint else normalTextPaint
            paint.alpha = (alpha * 255).toInt()

            canvas.save()
            canvas.scale(scale, scale, x, curvedY.toFloat())
            canvas.drawText(displayValue, x, (curvedY + 15f).toFloat(), paint)
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchY = event.y
                initialTouchY = event.y
                isDragging = false
                activeColumn = if (event.x < width / 2) 0 else 1
                parent?.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaY = event.y - lastTouchY
                val totalDeltaY = event.y - initialTouchY
                if (!isDragging && abs(totalDeltaY) > touchSlop) {
                    isDragging = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                }
                if (isDragging) {
                    if (activeColumn == 0) {
                        hoursScrollOffset += deltaY
                        handleHoursScroll()
                    } else {
                        minutesScrollOffset += deltaY
                        handleMinutesScroll()
                    }
                    lastTouchY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                if (isDragging) snapToNearest()
                isDragging = false
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun handleHoursScroll() {
        while (hoursScrollOffset >= itemHeight) {
            hoursScrollOffset -= itemHeight
            selectedHours = (selectedHours - 1 + 24) % 24
            notifyTimeChanged()
        }
        while (hoursScrollOffset <= -itemHeight) {
            hoursScrollOffset += itemHeight
            selectedHours = (selectedHours + 1) % 24
            notifyTimeChanged()
        }
    }

    private fun handleMinutesScroll() {
        while (minutesScrollOffset >= itemHeight) {
            minutesScrollOffset -= itemHeight
            selectedMinutes = (selectedMinutes - 5 + 60) % 60
            notifyTimeChanged()
        }
        while (minutesScrollOffset <= -itemHeight) {
            minutesScrollOffset += itemHeight
            selectedMinutes = (selectedMinutes + 5) % 60
            notifyTimeChanged()
        }
    }

    private fun snapToNearest() {
        post(object : Runnable {
            override fun run() {
                var needsUpdate = false
                if (abs(hoursScrollOffset) > 0.5f) {
                    hoursScrollOffset *= 0.8f
                    needsUpdate = true
                } else {
                    hoursScrollOffset = 0f
                }
                if (abs(minutesScrollOffset) > 0.5f) {
                    minutesScrollOffset *= 0.8f
                    needsUpdate = true
                } else {
                    minutesScrollOffset = 0f
                }
                if (needsUpdate) {
                    invalidate()
                    postDelayed(this, 16)
                }
            }
        })
    }

    private fun notifyTimeChanged() {
        listener?.onTimeChanged(selectedHours, selectedMinutes)
    }

    fun setTime(hours: Int, minutes: Int) {
        selectedHours = hours.coerceIn(0, 23)
        selectedMinutes = (minutes / 5) * 5
        hoursScrollOffset = 0f
        minutesScrollOffset = 0f
        invalidate()
        notifyTimeChanged()
    }

    fun getHours(): Int = selectedHours
    fun getMinutes(): Int = selectedMinutes
    fun setOnTimeChangedListener(listener: TimePickerListener) {
        this.listener = listener
    }
}

