/*
package com.mykaimeal.planner.di

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mykaimeal.planner.R
import sh.tyy.wheelpicker.core.BaseWheelPickerView
import sh.tyy.wheelpicker.core.TextWheelAdapter
import sh.tyy.wheelpicker.core.TextWheelPickerView
import sh.tyy.wheelpicker.databinding.TriplePickerViewBinding
import java.util.Calendar
class CustomDatePickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr), BaseWheelPickerView.WheelPickerViewListener {
    interface Listener {
        fun didSelectData(date: String, month: String, year: Int)
    }
    private val highlightView: View = View(context).apply {
        background = ContextCompat.getDrawable(context, R.drawable.height_bg)
    }
    private val binding = TriplePickerViewBinding.inflate(LayoutInflater.from(context), this)
    private val datePickerView = binding.leftPicker
    private val monthPickerView = binding.midPicker
    private val yearPickerView = binding.rightPicker
    private val dateAdapter = TextWheelAdapter()
    private val monthAdapter = TextWheelAdapter()
    private val yearAdapter = TextWheelAdapter()
    private val normalMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val today = Calendar.getInstance()
    private var listener: Listener? = null

    private val targetTextColor by lazy { ContextCompat.getColor(context, R.color.light_green) }
    private val selectedTextColor by lazy { ContextCompat.getColor(context, R.color.light_green) }
    private val normalTextColor by lazy { ContextCompat.getColor(context, android.R.color.darker_gray) }

    private val MONTH_REPEAT_COUNT = 500
    private val DATE_REPEAT_COUNT = 500

    private var lastValidDate = 1
    private var lastValidMonth = 0
    private var lastValidYear = 0

    private var _selectedDate = 1
    private var _selectedMonth = 0
    private var _selectedYear = 0

    // Flag to prevent recursive calls during setup
    private var isUpdatingPickers = false

    // Handler for continuous color updates
    private val colorUpdateHandler = Handler(Looper.getMainLooper())
    private var colorUpdateRunnable: Runnable? = null

    init {
        setupAdapters()
        setupHighlight()
        setupDefaults()

    }

    private fun setupAdapters() {
        datePickerView.setAdapter(dateAdapter)
        monthPickerView.setAdapter(monthAdapter)
        yearPickerView.setAdapter(yearAdapter)

        // Setup year adapter (simple non-circular)
        val minYear = today.get(Calendar.YEAR) - 100
        val maxYear = today.get(Calendar.YEAR)
        yearAdapter.values = (minYear..maxYear).map {
            TextWheelPickerView.Item("$it", "$it")
        }

        // Setup month adapter with circular scrolling
        setupMonthAdapter()
        // Disable haptic feedback (vibration)
        datePickerView.isHapticFeedbackEnabled = false
        monthPickerView.isHapticFeedbackEnabled = false
        yearPickerView.isHapticFeedbackEnabled = false

        datePickerView.isSoundEffectsEnabled = false
        monthPickerView.isSoundEffectsEnabled = false
        yearPickerView.isSoundEffectsEnabled = false
        datePickerView.setWheelListener(this)
        monthPickerView.setWheelListener(this)
        yearPickerView.setWheelListener(this)
    }

    private fun scheduleColorUpdate() {
        colorUpdateRunnable?.let { colorUpdateHandler.removeCallbacks(it) }
        colorUpdateRunnable = Runnable {
            applyTextColorsToAllPickers()
        }
        colorUpdateHandler.post(colorUpdateRunnable!!)
    }

    private fun setupMonthAdapter() {
        val circularMonths = mutableListOf<TextWheelPickerView.Item>()
        val centerStart = (MONTH_REPEAT_COUNT / 2) * 12

        // Add months in circular pattern
        for (cycle in 0 until MONTH_REPEAT_COUNT) {
            for (monthIndex in 0 until 12) {
                circularMonths.add(TextWheelPickerView.Item(normalMonths[monthIndex], normalMonths[monthIndex]))
            }
        }
        monthAdapter.values = circularMonths
    }

    private fun setupHighlight() {
        addView(highlightView)
        (highlightView.layoutParams as? LayoutParams)?.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = context.resources.getDimensionPixelSize(com.intuit.ssp.R.dimen._30ssp)
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun setupDefaults() {
        isUpdatingPickers = true

        val defaultDate = Calendar.getInstance().apply { add(Calendar.YEAR, -13) }
        _selectedYear = defaultDate.get(Calendar.YEAR)
        _selectedMonth = defaultDate.get(Calendar.MONTH)
        _selectedDate = defaultDate.get(Calendar.DAY_OF_MONTH)

        lastValidYear = _selectedYear
        lastValidMonth = _selectedMonth
        lastValidDate = _selectedDate

        // Set positions
        setYearPickerPosition()
        setMonthPickerPosition()
        setupDateAdapter()

        // Use postDelayed to ensure UI is ready
        postDelayed({
            isUpdatingPickers = false
            applyTextColorsToAllPickers()
            // Start continuous color updates
            startContinuousColorUpdates()
        }, 100)
    }

    private fun startContinuousColorUpdates() {
        val updateRunnable = object : Runnable {
            override fun run() {
                if (!isUpdatingPickers) {
                    applyTextColorsToAllPickers()
                }
                colorUpdateHandler.postDelayed(this, 16) // 60 FPS updates
            }
        }
        colorUpdateHandler.post(updateRunnable)
    }

    private fun setupDateAdapter() {
        val maxDay = getMaxDayOfMonth(_selectedYear, _selectedMonth)

        // Adjust selected date if needed
        if (_selectedDate > maxDay) {
            _selectedDate = maxDay
            lastValidDate = maxDay
        }

        // Create circular dates array
        val circularDates = mutableListOf<TextWheelPickerView.Item>()
        val centerStart = (DATE_REPEAT_COUNT / 2) * maxDay

        for (cycle in 0 until DATE_REPEAT_COUNT) {
            for (day in 1..maxDay) {
                circularDates.add(TextWheelPickerView.Item("$day", "$day"))
            }
        }

        dateAdapter.values = circularDates
        setDatePickerPosition()
    }

    private fun setYearPickerPosition() {
        val minYear = today.get(Calendar.YEAR) - 100
        val yearIndex = _selectedYear - minYear
        yearPickerView.selectedIndex = yearIndex
    }

    private fun setMonthPickerPosition() {
        val centerCycle = MONTH_REPEAT_COUNT / 2
        val monthPosition = centerCycle * 12 + _selectedMonth
        monthPickerView.selectedIndex = monthPosition
    }

    private fun setDatePickerPosition() {
        val maxDay = getMaxDayOfMonth(_selectedYear, _selectedMonth)
        val centerCycle = DATE_REPEAT_COUNT / 2
        val datePosition = centerCycle * maxDay + (_selectedDate - 1)
        datePickerView.selectedIndex = datePosition
    }

    private fun getMaxDayOfMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun calculateDateFromIndex(index: Int): Int {
        val maxDay = getMaxDayOfMonth(_selectedYear, _selectedMonth)
        val dayInCycle = index % maxDay
        return dayInCycle + 1
    }

    private fun calculateMonthFromIndex(index: Int): Int {
        return index % 12
    }

    override fun didSelectItem(picker: BaseWheelPickerView, index: Int) {
        if (isUpdatingPickers) return

        val minYear = today.get(Calendar.YEAR) - 100
        val newYear = if (picker == yearPickerView) index + minYear else _selectedYear
        val newMonth = if (picker == monthPickerView) calculateMonthFromIndex(index) else _selectedMonth
        val newDate = if (picker == datePickerView) calculateDateFromIndex(index) else _selectedDate

        println("DEBUG: Selected - Year: $newYear, Month: $newMonth, Date: $newDate")
        println("DEBUG: Picker type: ${when(picker) {
            yearPickerView -> "Year"
            monthPickerView -> "Month"
            datePickerView -> "Date"
            else -> "Unknown"
        }}")

        if (isValidDateSelection(newYear, newMonth, newDate)) {
            _selectedYear = newYear
            _selectedMonth = newMonth
            _selectedDate = newDate

            lastValidYear = _selectedYear
            lastValidMonth = _selectedMonth
            lastValidDate = _selectedDate

            // Update date picker if year or month changed
            if (picker == yearPickerView || picker == monthPickerView) {
                isUpdatingPickers = true
                setupDateAdapter()
                postDelayed({
                    isUpdatingPickers = false
                    applyTextColorsToAllPickers()
                }, 50)
            }

            // Notify listener
            listener?.didSelectData(_selectedDate.toString(), normalMonths[_selectedMonth], _selectedYear)

        } else {
            // Revert to last valid selection
            isUpdatingPickers = true
            when (picker) {
                yearPickerView -> {
                    yearPickerView.selectedIndex = lastValidYear - minYear
                }
                monthPickerView -> {
                    setMonthPickerPosition()
                }
                datePickerView -> {
                    setDatePickerPosition()
                }
            }
            postDelayed({
                isUpdatingPickers = false
            }, 50)
        }

        // Apply text colors immediately
        postDelayed({
            applyTextColorsToAllPickers()
        }, 10)



        scheduleColorUpdate()


    }

    private fun isValidDateSelection(year: Int, month: Int, day: Int): Boolean {
        // Check if date exists in that month/year
        val maxDay = getMaxDayOfMonth(year, month)
        if (day > maxDay) return false

        val selectedCal = Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val currentCal = Calendar.getInstance()
        if (selectedCal.after(currentCal)) return false
        return calculateAge(year, month, day) >= 13
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - year
        if (today.get(Calendar.MONTH) < month ||
            (today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) < day)) {
            age--
        }
        return age
    }

    private fun applyTextColorsToAllPickers() {
        try {
            // Apply normal colors to all items first
            applyTextColorRecursively(datePickerView, normalTextColor)
            applyTextColorRecursively(monthPickerView, normalTextColor)
            applyTextColorRecursively(yearPickerView, normalTextColor)

            // Apply selected colors to center items
            applyCenterTextColor(datePickerView)
            applyCenterTextColor(monthPickerView)
            applyCenterTextColor(yearPickerView)

            // Apply gradient colors for smooth transition
            applyGradientColors(datePickerView)
            applyGradientColors(monthPickerView)
            applyGradientColors(yearPickerView)

        } catch (e: Exception) {
            // Ignore exceptions during color updates
        }
    }

    private fun applyGradientColors(pickerView: TextWheelPickerView) {
        try {
            val centerY = pickerView.height / 2
            val itemHeight = if (pickerView.childCount > 0) pickerView.getChildAt(0).height else 0

            if (itemHeight > 0) {
                for (i in 0 until pickerView.childCount) {
                    val child = pickerView.getChildAt(i)
                    val childCenterY = child.top + child.height / 2
                    val distance = kotlin.math.abs(childCenterY - centerY)

                    // Calculate alpha based on distance from center
                    val alpha = when {
                        distance <= itemHeight / 2 -> 1.0f // Center item
                        distance <= itemHeight -> 0.7f // Adjacent items
                        distance <= itemHeight * 1.5 -> 0.5f // Second adjacent items
                        else -> 0.3f // Far items
                    }

                    val color = if (distance <= itemHeight / 2) {
                        selectedTextColor
                    } else {
                        normalTextColor
                    }

                    applyTextColorWithAlpha(child, color, alpha)
                }
            }
        } catch (e: Exception) {
            // Ignore exceptions during gradient application
        }
    }

    private fun applyTextColorWithAlpha(view: View, color: Int, alpha: Float) {
        when (view) {
            is TextView -> {
                val colorWithAlpha = Color.argb(
                    (alpha * 255).toInt(),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
                view.setTextColor(colorWithAlpha)
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    applyTextColorWithAlpha(view.getChildAt(i), color, alpha)
                }
            }
        }
    }

    private fun applyCenterTextColor(pickerView: TextWheelPickerView) {
        val centerView = pickerView.findViewAtCenter()
        centerView?.let {
            applyTextColorRecursively(it, selectedTextColor)
        }
    }

    private fun applyTextColorRecursively(view: View, color: Int) {
        when (view) {
            is TextView -> {
                view.setTextColor(color)
                view.invalidate()
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    applyTextColorRecursively(view.getChildAt(i), color)
                }
            }
        }
    }

    private fun TextWheelPickerView.findViewAtCenter(): View? {
        val centerY = this.height / 2
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.top <= centerY && child.bottom >= centerY) {
                return child
            }
        }
        return null
    }

    fun setSelectedDate(year: Int, month: Int, day: Int): Boolean {
        if (isValidDateSelection(year, month, day)) {
            isUpdatingPickers = true

            _selectedYear = year
            _selectedMonth = month
            _selectedDate = day

            lastValidYear = year
            lastValidMonth = month
            lastValidDate = day

            setYearPickerPosition()
            setMonthPickerPosition()
            setupDateAdapter()

            postDelayed({
                isUpdatingPickers = false
                applyTextColorsToAllPickers()
            }, 100)

            return true
        }
        return false
    }

    fun getSelectedDateParts(): Triple<Int, Int, Int> {
        return Triple(_selectedDate, _selectedMonth + 1, _selectedYear)  // Month 1-based
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up handlers
        colorUpdateHandler.removeCallbacksAndMessages(null)
    }
}
*/


package com.mykaimeal.planner.di
import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mykaimeal.planner.R
import sh.tyy.wheelpicker.core.BaseWheelPickerView
import sh.tyy.wheelpicker.core.TextWheelAdapter
import sh.tyy.wheelpicker.core.TextWheelPickerView
import sh.tyy.wheelpicker.databinding.TriplePickerViewBinding
import java.util.Calendar

class CustomDatePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), BaseWheelPickerView.WheelPickerViewListener {

    interface Listener {
        fun didSelectData(date: String, month: String, year: Int)
    }

    private val highlightView: View = View(context).apply {
        background = ContextCompat.getDrawable(context, R.drawable.height_bg)
    }
    private val binding = TriplePickerViewBinding.inflate(LayoutInflater.from(context), this)
    private val datePickerView = binding.leftPicker
    private val monthPickerView = binding.midPicker
    private val yearPickerView = binding.rightPicker
    private val dateAdapter = TextWheelAdapter()
    private val monthAdapter = TextWheelAdapter()
    private val yearAdapter = TextWheelAdapter()
    private val normalMonths = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    private val today = Calendar.getInstance()
    private var listener: Listener? = null

    private val targetTextColor by lazy { ContextCompat.getColor(context, R.color.light_green) }
    private val selectedTextColor by lazy { ContextCompat.getColor(context, R.color.light_green) }
    private val normalTextColor by lazy { ContextCompat.getColor(context, android.R.color.darker_gray) }

    private val MONTH_REPEAT_COUNT = 500
    private val DATE_REPEAT_COUNT = 500
    private val YEAR_REPEAT_COUNT = 500

    private var lastValidDate = 1
    private var lastValidMonth = 0
    private var lastValidYear = 0

    private var _selectedDate = 1
    private var _selectedMonth = 0
    private var _selectedYear = 0

    // Flag to prevent recursive calls during setup
    private var isUpdatingPickers = false

    // Handler for continuous color updates
    private val colorUpdateHandler = Handler(Looper.getMainLooper())
    private var colorUpdateRunnable: Runnable? = null

    // New variables to control age validation
    private var ageValidationYears = 13 // Default 13 years
    private var isAgeValidationEnabled = true // Default validation enabled

    init {
        setupAdapters()
        setupHighlight()
        setupDefaults()
    }

    // New function to set age validation
    fun setAgeValidation(mode: Int) {
        when (mode) {
            0 -> {
                // No age validation
                isAgeValidationEnabled = false
                ageValidationYears = 0
            }
            1 -> {
                // 12 years validation
                isAgeValidationEnabled = true
                ageValidationYears = 12
            }
            else -> {
                // Custom age validation
                isAgeValidationEnabled = true
                ageValidationYears = mode
            }
        }
    }

    private fun setupAdapters() {
        datePickerView.setAdapter(dateAdapter)
        monthPickerView.setAdapter(monthAdapter)
        yearPickerView.setAdapter(yearAdapter)

        // Setup year adapter with circular scrolling
        setupYearAdapter()

        // Setup month adapter with circular scrolling
        setupMonthAdapter()
        // Disable haptic feedback (vibration)
        datePickerView.isHapticFeedbackEnabled = false
        monthPickerView.isHapticFeedbackEnabled = false
        yearPickerView.isHapticFeedbackEnabled = false

        datePickerView.isSoundEffectsEnabled = false
        monthPickerView.isSoundEffectsEnabled = false
        yearPickerView.isSoundEffectsEnabled = false
        datePickerView.setWheelListener(this)
        monthPickerView.setWheelListener(this)
        yearPickerView.setWheelListener(this)
    }

    private fun scheduleColorUpdate() {
        colorUpdateRunnable?.let { colorUpdateHandler.removeCallbacks(it) }
        colorUpdateRunnable = Runnable {
            applyTextColorsToAllPickers()
        }
        colorUpdateHandler.post(colorUpdateRunnable!!)
    }

    private fun setupYearAdapter() {
        val minYear = today.get(Calendar.YEAR) - 100
        val maxYear = today.get(Calendar.YEAR)
        val yearRange = maxYear - minYear + 1

        val circularYears = mutableListOf<TextWheelPickerView.Item>()

        // Add years in circular pattern
        for (cycle in 0 until YEAR_REPEAT_COUNT) {
            for (year in minYear..maxYear) {
                circularYears.add(TextWheelPickerView.Item("$year", "$year"))
            }
        }
        yearAdapter.values = circularYears
    }

    private fun setupMonthAdapter() {
        val circularMonths = mutableListOf<TextWheelPickerView.Item>()
        val centerStart = (MONTH_REPEAT_COUNT / 2) * 12

        // Add months in circular pattern
        for (cycle in 0 until MONTH_REPEAT_COUNT) {
            for (monthIndex in 0 until 12) {
                circularMonths.add(TextWheelPickerView.Item(normalMonths[monthIndex], normalMonths[monthIndex]))
            }
        }
        monthAdapter.values = circularMonths
    }

    private fun setupHighlight() {
        addView(highlightView)
        (highlightView.layoutParams as? LayoutParams)?.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = context.resources.getDimensionPixelSize(com.intuit.ssp.R.dimen._30ssp)
            gravity = Gravity.CENTER_VERTICAL
        }
    }

    private fun setupDefaults() {
        isUpdatingPickers = true
        val defaultDate = if (isAgeValidationEnabled) {
            Calendar.getInstance().apply { add(Calendar.YEAR, -ageValidationYears) }
        } else {
            Calendar.getInstance() // Current date if no validation
        }
        _selectedYear = defaultDate.get(Calendar.YEAR)
        _selectedMonth = defaultDate.get(Calendar.MONTH)
        _selectedDate = defaultDate.get(Calendar.DAY_OF_MONTH)

        lastValidYear = _selectedYear
        lastValidMonth = _selectedMonth
        lastValidDate = _selectedDate

        // Set positions
        setYearPickerPosition()
        setMonthPickerPosition()
        setupDateAdapter()

        // Use postDelayed to ensure UI is ready
        postDelayed({
            isUpdatingPickers = false
            applyTextColorsToAllPickers()
            // Start continuous color updates
            startContinuousColorUpdates()
        }, 100)
    }

    private fun startContinuousColorUpdates() {
        val updateRunnable = object : Runnable {
            override fun run() {
                if (!isUpdatingPickers) {
                    applyTextColorsToAllPickers()
                }
                colorUpdateHandler.postDelayed(this, 16) // 60 FPS updates
            }
        }
        colorUpdateHandler.post(updateRunnable)
    }

    private fun setupDateAdapter() {
        val maxDay = getMaxDayOfMonth(_selectedYear, _selectedMonth)

        // Adjust selected date if needed
        if (_selectedDate > maxDay) {
            _selectedDate = maxDay
            lastValidDate = maxDay
        }

        // Create circular dates array
        val circularDates = mutableListOf<TextWheelPickerView.Item>()
        val centerStart = (DATE_REPEAT_COUNT / 2) * maxDay

        for (cycle in 0 until DATE_REPEAT_COUNT) {
            for (day in 1..maxDay) {
                circularDates.add(TextWheelPickerView.Item("$day", "$day"))
            }
        }

        dateAdapter.values = circularDates
        setDatePickerPosition()
    }

    private fun setYearPickerPosition() {
        val minYear = today.get(Calendar.YEAR) - 100
        val maxYear = today.get(Calendar.YEAR)
        val yearRange = maxYear - minYear + 1
        val centerCycle = YEAR_REPEAT_COUNT / 2
        val yearPosition = centerCycle * yearRange + (_selectedYear - minYear)
        yearPickerView.selectedIndex = yearPosition
    }

    private fun setMonthPickerPosition() {
        val centerCycle = MONTH_REPEAT_COUNT / 2
        val monthPosition = centerCycle * 12 + _selectedMonth
        monthPickerView.selectedIndex = monthPosition
    }

    private fun setDatePickerPosition() {
        val maxDay = getMaxDayOfMonth(_selectedYear, _selectedMonth)
        val centerCycle = DATE_REPEAT_COUNT / 2
        val datePosition = centerCycle * maxDay + (_selectedDate - 1)
        datePickerView.selectedIndex = datePosition
    }

    private fun getMaxDayOfMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun calculateYearFromIndex(index: Int): Int {
        val minYear = today.get(Calendar.YEAR) - 100
        val maxYear = today.get(Calendar.YEAR)
        val yearRange = maxYear - minYear + 1
        val yearInCycle = index % yearRange
        return minYear + yearInCycle
    }

    private fun calculateDateFromIndex(index: Int): Int {
        val maxDay = getMaxDayOfMonth(_selectedYear, _selectedMonth)
        val dayInCycle = index % maxDay
        return dayInCycle + 1
    }

    private fun calculateMonthFromIndex(index: Int): Int {
        return index % 12
    }

    override fun didSelectItem(picker: BaseWheelPickerView, index: Int) {
        if (isUpdatingPickers) return

        val minYear = today.get(Calendar.YEAR) - 100
        val newYear = if (picker == yearPickerView) calculateYearFromIndex(index) else _selectedYear
        val newMonth = if (picker == monthPickerView) calculateMonthFromIndex(index) else _selectedMonth
        val newDate = if (picker == datePickerView) calculateDateFromIndex(index) else _selectedDate

        println("DEBUG: Selected - Year: $newYear, Month: $newMonth, Date: $newDate")
        println("DEBUG: Picker type: ${when(picker) {
            yearPickerView -> "Year"
            monthPickerView -> "Month"
            datePickerView -> "Date"
            else -> "Unknown"
        }}")

        if (isValidDateSelection(newYear, newMonth, newDate)) {
            _selectedYear = newYear
            _selectedMonth = newMonth
            _selectedDate = newDate

            lastValidYear = _selectedYear
            lastValidMonth = _selectedMonth
            lastValidDate = _selectedDate

            // Update date picker if year or month changed
            if (picker == yearPickerView || picker == monthPickerView) {
                isUpdatingPickers = true
                setupDateAdapter()
                postDelayed({
                    isUpdatingPickers = false
                    applyTextColorsToAllPickers()
                }, 50)
            }

            // Notify listener
            listener?.didSelectData(_selectedDate.toString(), normalMonths[_selectedMonth], _selectedYear)

        } else {
            // Revert to last valid selection
            isUpdatingPickers = true
            when (picker) {
                yearPickerView -> {
                    setYearPickerPosition()
                }
                monthPickerView -> {
                    setMonthPickerPosition()
                }
                datePickerView -> {
                    setDatePickerPosition()
                }
            }
            postDelayed({
                isUpdatingPickers = false
            }, 50)
        }

        // Apply text colors immediately
        postDelayed({
            applyTextColorsToAllPickers()
        }, 10)

        scheduleColorUpdate()
    }

    private fun isValidDateSelection(year: Int, month: Int, day: Int): Boolean {
        // Check if date exists in that month/year
        val maxDay = getMaxDayOfMonth(year, month)
        if (day > maxDay) return false

        val selectedCal = Calendar.getInstance().apply {
            set(year, month, day, 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val currentCal = Calendar.getInstance()
        if (selectedCal.after(currentCal)) return false

        // Age validation only if enabled
        return if (isAgeValidationEnabled) {
            calculateAge(year, month, day) >= ageValidationYears
        } else {
            true // No age validation, allow any past/current date
        }
    }

    private fun calculateAge(year: Int, month: Int, day: Int): Int {
        val today = Calendar.getInstance()
        var age = today.get(Calendar.YEAR) - year
        if (today.get(Calendar.MONTH) < month ||
            (today.get(Calendar.MONTH) == month && today.get(Calendar.DAY_OF_MONTH) < day)) {
            age--
        }
        return age
    }

    private fun applyTextColorsToAllPickers() {
        try {
            // Apply normal colors to all items first
            applyTextColorRecursively(datePickerView, normalTextColor)
            applyTextColorRecursively(monthPickerView, normalTextColor)
            applyTextColorRecursively(yearPickerView, normalTextColor)

            // Apply selected colors to center items
            applyCenterTextColor(datePickerView)
            applyCenterTextColor(monthPickerView)
            applyCenterTextColor(yearPickerView)

            // Apply gradient colors for smooth transition
            applyGradientColors(datePickerView)
            applyGradientColors(monthPickerView)
            applyGradientColors(yearPickerView)

        } catch (e: Exception) {
            // Ignore exceptions during color updates
        }
    }

    private fun applyGradientColors(pickerView: TextWheelPickerView) {
        try {
            val centerY = pickerView.height / 2
            val itemHeight = if (pickerView.childCount > 0) pickerView.getChildAt(0).height else 0

            if (itemHeight > 0) {
                for (i in 0 until pickerView.childCount) {
                    val child = pickerView.getChildAt(i)
                    val childCenterY = child.top + child.height / 2
                    val distance = kotlin.math.abs(childCenterY - centerY)

                    // Calculate alpha based on distance from center
                    val alpha = when {
                        distance <= itemHeight / 2 -> 1.0f // Center item
                        distance <= itemHeight -> 0.7f // Adjacent items
                        distance <= itemHeight * 1.5 -> 0.5f // Second adjacent items
                        else -> 0.3f // Far items
                    }

                    val color = if (distance <= itemHeight / 2) {
                        selectedTextColor
                    } else {
                        normalTextColor
                    }

                    applyTextColorWithAlpha(child, color, alpha)
                }
            }
        } catch (e: Exception) {
            // Ignore exceptions during gradient application
        }
    }

    private fun applyTextColorWithAlpha(view: View, color: Int, alpha: Float) {
        when (view) {
            is TextView -> {
                val colorWithAlpha = Color.argb(
                    (alpha * 255).toInt(),
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
                view.setTextColor(colorWithAlpha)
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    applyTextColorWithAlpha(view.getChildAt(i), color, alpha)
                }
            }
        }
    }

    private fun applyCenterTextColor(pickerView: TextWheelPickerView) {
        val centerView = pickerView.findViewAtCenter()
        centerView?.let {
            applyTextColorRecursively(it, selectedTextColor)
        }
    }

    private fun applyTextColorRecursively(view: View, color: Int) {
        when (view) {
            is TextView -> {
                view.setTextColor(color)
                view.invalidate()
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    applyTextColorRecursively(view.getChildAt(i), color)
                }
            }
        }
    }

    private fun TextWheelPickerView.findViewAtCenter(): View? {
        val centerY = this.height / 2
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.top <= centerY && child.bottom >= centerY) {
                return child
            }
        }
        return null
    }

    fun setSelectedDate(year: Int, month: Int, day: Int): Boolean {
        if (isValidDateSelection(year, month, day)) {
            isUpdatingPickers = true

            _selectedYear = year
            _selectedMonth = month
            _selectedDate = day

            lastValidYear = year
            lastValidMonth = month
            lastValidDate = day

            setYearPickerPosition()
            setMonthPickerPosition()
            setupDateAdapter()

            postDelayed({
                isUpdatingPickers = false
                applyTextColorsToAllPickers()
            }, 100)

            return true
        }
        return false
    }

    fun getSelectedDateParts(): Triple<Int, Int, Int> {
        return Triple(_selectedDate, _selectedMonth + 1, _selectedYear)  // Month 1-based
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Clean up handlers
        colorUpdateHandler.removeCallbacksAndMessages(null)
    }
}
