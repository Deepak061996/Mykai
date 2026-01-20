package com.mykaimeal.planner.di

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mykaimeal.planner.R
import sh.tyy.wheelpicker.core.BaseWheelPickerView
import sh.tyy.wheelpicker.core.TextWheelAdapter
import sh.tyy.wheelpicker.core.TextWheelPickerView
import sh.tyy.wheelpicker.databinding.TriplePickerViewBinding

class WeekdayTimePickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), BaseWheelPickerView.WheelPickerViewListener {

    interface Listener {
        fun didSelectItem(index: Int, value: String)
    }

    private val highlightView: View = View(context).apply {
        background = ContextCompat.getDrawable(context, R.drawable.height_bg)
    }

    private val binding: TriplePickerViewBinding =
        TriplePickerViewBinding.inflate(LayoutInflater.from(context), this)

    private val pickerView: TextWheelPickerView = binding.leftPicker
    private val adapter = TextWheelAdapter()
    private var listener: Listener? = null
    private var colorRunnable: Runnable? = null
    // Lists passed from fragment
    private var listA: List<String> = emptyList()
    private var listB: List<String> = emptyList()
    private var currentList: List<String> = emptyList()
    private var currentListType: ListType = ListType.NONE

    var selectedIndex: Int
        get() = pickerView.selectedIndex
        set(value) {
            if (currentList.isNotEmpty()) {
                pickerView.selectedIndex = value.coerceIn(0, currentList.size - 1)
            }
        }

    var isCircular: Boolean = false
        set(value) {
            field = value
            pickerView.isCircular = value
        }

    // Get current selected value
    val selectedValue: String
        get() = if (selectedIndex >= 0 && selectedIndex < currentList.size) {
            currentList[selectedIndex]
        } else {
            ""
        }

    init {
        // Hide unused pickers (hour and minute)
        binding.midPicker.visibility = View.GONE
        binding.rightPicker.visibility = View.GONE

        addView(highlightView)
        (highlightView.layoutParams as? LayoutParams)?.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = context.resources.getDimensionPixelSize(com.intuit.ssp.R.dimen._30ssp)
            gravity = Gravity.CENTER_VERTICAL
        }

        // Setup adapter
        pickerView.setAdapter(adapter)
        pickerView.setWheelListener(this)

        // Disable haptic feedback (vibration)
        pickerView.isHapticFeedbackEnabled = false
        pickerView.isSoundEffectsEnabled  = false

        // Start continuous color monitoring
        startColorMonitoring()

    }

    override fun setHapticFeedbackEnabled(enabled: Boolean) {
        super.setHapticFeedbackEnabled(false) // Always keep it false
        pickerView.isHapticFeedbackEnabled = false
        pickerView.isSoundEffectsEnabled  = false
    }

    fun setWheelListener(listener: Listener) {
        this.listener = listener
    }

    // Method to set both lists from fragment
    fun setLists(listA: List<String>, listB: List<String>) {
        this.listA = listA
        this.listB = listB
    }

    // Method to show list A (when user clicks option A)
    fun showListA() {
        if (listA.isNotEmpty()) {
            currentList = listA
            currentListType = ListType.LIST_A
            updatePickerList(currentList)
            selectedIndex = 0 // Reset to first item
        }
    }


    // Method to show list B (when user clicks option B)
    fun showListB() {
        if (listB.isNotEmpty()) {
            currentList = listB
            currentListType = ListType.LIST_B
            updatePickerList(currentList)
            selectedIndex = 0 // Reset to first item
        }
    }

    // NEW METHOD: Show list A and select specific value
    fun showListAWithValue(value: String) {
        if (listA.isNotEmpty()) {
            currentList = listA
            currentListType = ListType.LIST_A
            updatePickerList(currentList)
            selectValueByString(value)
        }
    }

    // NEW METHOD: Show list B and select specific value
    fun showListBWithValue(value: String) {
        if (listB.isNotEmpty()) {
            currentList = listB
            currentListType = ListType.LIST_B
            updatePickerList(currentList)
            selectValueByString(value)
        }
    }

    // NEW METHOD: Select value by string (exact match)
    fun selectValueByString(value: String) {
        val index = currentList.indexOfFirst { it.equals(value, ignoreCase = true) }
        if (index >= 0) {
            selectedIndex = index
        }
    }

    // NEW METHOD: Select value by partial match (contains)
    fun selectValueContaining(partialValue: String) {
        val index = currentList.indexOfFirst { it.contains(partialValue, ignoreCase = true) }
        if (index >= 0) {
            selectedIndex = index
        }
    }

    // NEW METHOD: Find and select value based on unit (kg, lb, etc.)
    fun selectValueByUnit(unit: String) {
        val index = currentList.indexOfFirst { it.contains(unit, ignoreCase = true) }
        if (index >= 0) {
            selectedIndex = index
        }
    }

    // Method to set and show a single custom list
    fun setAndShowList(list: List<String>) {
        if (list.isNotEmpty()) {
            currentList = list
            currentListType = ListType.CUSTOM
            updatePickerList(currentList)
            selectedIndex = 0 // Reset to first item
        }
    }

    // NEW METHOD: Set custom list and select specific value
    fun setAndShowListWithValue(list: List<String>, value: String) {
        if (list.isNotEmpty()) {
            currentList = list
            currentListType = ListType.CUSTOM
            updatePickerList(currentList)
            selectValueByString(value)
        }
    }

    // Get current list type
    fun getCurrentListType(): ListType {
        return currentListType
    }

    // Check if lists are set
    fun areListsSet(): Boolean {
        return listA.isNotEmpty()
    }

    // Get current list
    fun getCurrentList(): List<String> {
        return currentList
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePickerList(list: List<String>) {
        adapter.values = list.map { TextWheelPickerView.Item(it, it) }
        adapter.notifyDataSetChanged()

        // Ensure haptic feedback is disabled after data update
        pickerView.isHapticFeedbackEnabled = false

        // Restart color monitoring after data update
        startColorMonitoring()
    }

    // Start continuous color monitoring
    private fun startColorMonitoring() {
        // Stop existing monitoring
        colorRunnable?.let { removeCallbacks(it) }

        colorRunnable = object : Runnable {
            override fun run() {
                try {
                    val targetColor = ContextCompat.getColor(context, R.color.light_green)
                    applyTextColorRecursively(pickerView, targetColor)

                    // Schedule next check
                    postDelayed(this, 50) // Check every 50ms for smooth color updates
                } catch (e: Exception) {
                    println("Error in color monitoring: ${e.message}")
                }
            }
        }

        // Start monitoring
        post(colorRunnable!!)
    }

    // Fallback method to apply text color to visible views
    private fun applyFallbackTextColor() {
        try {
            val targetColor = ContextCompat.getColor(context, R.color.light_green)
            applyTextColorRecursively(pickerView, targetColor)
        } catch (e: Exception) {
            println("Error applying fallback text color: ${e.message}")
        }
    }

    // Recursively find and apply color to all TextViews
    private fun applyTextColorRecursively(view: View, color: Int) {
        when (view) {
            is TextView -> {
                view.setTextColor(color)
            }
            is ViewGroup -> {
                for (i in 0 until view.childCount) {
                    applyTextColorRecursively(view.getChildAt(i), color)
                }
            }
        }
    }

    // Stop color monitoring when view is detached
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        colorRunnable?.let { removeCallbacks(it) }
    }

    override fun didSelectItem(picker: BaseWheelPickerView, index: Int) {
        listener?.didSelectItem(index, selectedValue)
    }

    enum class ListType {
        NONE, LIST_A, LIST_B, CUSTOM
    }
}