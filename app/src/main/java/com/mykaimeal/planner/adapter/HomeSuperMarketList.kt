package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.AdapterLayoutSupermarketBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.OperationalHours
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.SuperMarketModelsData
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

class HomeSuperMarketList(
    private var storesData: MutableList<SuperMarketModelsData>?,
    private var requireActivity: FragmentActivity,
    private var onItemSelectListener: OnItemSelectListener,
    pos: Int
) : RecyclerView.Adapter<HomeSuperMarketList.ViewHolder>() {

    private var selectedPosition = pos // Default no selection

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterLayoutSupermarketBinding =
            AdapterLayoutSupermarketBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data = storesData?.get(position)

        // ✅ Correctly update the background based on selection
        if (selectedPosition == position) {
            // ✅ Notify selection change
            onItemSelectListener.itemSelect(position, data!!.store_uuid.toString(), "SuperMarket")
            holder.binding.cardViewMainLayout.strokeColor = Color.parseColor("#06C169")
        } else {
            holder.binding.cardViewMainLayout.strokeColor = Color.parseColor("#FFFFFF")
        }

        if (data?.is_open == true) {
            holder.binding.layclose.visibility = View.GONE
            holder.binding.tvTime.text = "" // CLEAR any leftover text
        } else {
            holder.binding.layclose.visibility = View.VISIBLE
            val time = getTodayHours(data?.operational_hours)?.split("-")
            val openTime = BaseApplication.formatTime(time?.getOrNull(0) ?: "")
            holder.binding.tvTime.text = "Closed now Open at $openTime"
        }

        data?.let {
            if (it.store_name != null) {
                holder.binding.tvSuperMarketItems.text = it.store_name
            }

            /*if (it.total != null) {
                val totalValue = it.total
                val formattedTotal = if (totalValue % 1 == 0.0) {
                    totalValue.toInt().toString() // Show without decimal
                } else {
                    String.format("%.2f", totalValue) // Show two decimals
                }
                holder.binding.tvSuperMarketRupees.text = "$$formattedTotal"
            }*/


            it.distance?.let { value->
                holder.binding.tvMiles.text= "$value miles"
            }

            // ✅ Load image with Glide
            Glide.with(requireActivity)
                .load(it.image)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                })
                .into(holder.binding.imageSuperMarket)
        } ?: run {
            holder.binding.layProgess.root.visibility = View.GONE
        }



        holder.binding.relativeLayoutMain.setOnClickListener {
            if (data?.is_open==true){
                val previousPosition = selectedPosition
                selectedPosition = position
                // Refresh the UI for both previously selected and newly selected item
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
            }else{
                Toast.makeText(requireActivity, "Store ${data?.store_name} is closed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("NewApi")
    private fun getTodayHours(operationalHours: OperationalHours?): String? {
        if (operationalHours == null) return null
        val today = LocalDate.now().dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        return when (today) {
            "Monday" -> operationalHours.Monday
            "Tuesday" -> operationalHours.Tuesday
            "Wednesday" -> operationalHours.Wednesday
            "Thursday" -> operationalHours.Thursday
            "Friday" -> operationalHours.Friday
            "Saturday" -> operationalHours.Saturday
            "Sunday" -> operationalHours.Sunday
            else -> null
        }
    }


    fun updateList(list: MutableList<SuperMarketModelsData>){
        storesData=list
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return storesData?.size ?: 0
    }

    class ViewHolder(var binding: AdapterLayoutSupermarketBinding) :
        RecyclerView.ViewHolder(binding.root)
}
