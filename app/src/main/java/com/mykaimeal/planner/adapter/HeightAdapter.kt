package com.mykaimeal.planner.adapter

import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.R

class HeightAdapter(
    private val heights: List<String>,
    private val onHeightSelected: (String) -> Unit
) : RecyclerView.Adapter<HeightAdapter.HeightViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_height, parent, false)
        return HeightViewHolder(view)
    }

    override fun onBindViewHolder(holder: HeightViewHolder, position: Int) {
        val height = heights[position]
        holder.heightTextView.text = height

        // Apply styling based on whether the item is selected
        if (position == selectedPosition) {
            holder.heightTextView.setTextColor(Color.RED)
            holder.heightTextView.setTextColor(Color.parseColor("#06C169"))
            holder.heightTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25f)// Keep this for selected item text size
        } else {
            holder.heightTextView.setTextColor(Color.parseColor("#A0A0A0")) // Dark gray for unselected text
            holder.heightTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // Keep this for unselected item text size
        }

    }

    override fun getItemCount(): Int = heights.size

    fun setSelectedPosition(position: Int) {
        if (selectedPosition != position) {
            val oldPosition = selectedPosition
            selectedPosition = position
            // Notify both the old and new positions to refresh their views
            if (oldPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPosition)
            }
            if (selectedPosition != RecyclerView.NO_POSITION) {
                notifyItemChanged(selectedPosition)
            }
            // Trigger the callback for the programmatically selected item
            if (position != RecyclerView.NO_POSITION) {
                onHeightSelected(heights[position])
            }
        }
    }

    // Add a getter for the currently selected position
    fun getSelectedPosition(): Int = selectedPosition

    class HeightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val heightTextView: TextView = itemView.findViewById(R.id.heightTextView)
//        val itemContainer: LinearLayout = itemView.findViewById(R.id.item_container) // Get reference to the LinearLayout
    }
}