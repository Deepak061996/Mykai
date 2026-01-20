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
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Store
import java.time.LocalDate


class SuperMarketListAdapter(
    private var storesData: MutableList<Store>?,
    private var requireActivity: FragmentActivity,
    private var onItemSelectListener: OnItemSelectListener
) : RecyclerView.Adapter<SuperMarketListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterLayoutSupermarketBinding =
            AdapterLayoutSupermarketBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "DefaultLocale", "NewApi")
    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {

        val data = storesData?.get(position)


        if (data?.is_open == true) {
            holder.binding.layclose.visibility = View.GONE
            holder.binding.tvTime.text = "" // CLEAR any leftover text
        } else {
            holder.binding.layclose.visibility = View.VISIBLE
            val time = getTodayHours(data?.operational_hours)?.split("-")
            val openTime = BaseApplication.formatTime(time?.getOrNull(0) ?: "")
            holder.binding.tvTime.text = "Closed now Open at $openTime"
        }

        if (data?.is_slected != null) {
            if (data.is_slected == 1) {
                holder.binding.cardViewMainLayout.strokeColor = Color.parseColor("#06C169")
            } else {
                holder.binding.cardViewMainLayout.strokeColor = Color.parseColor("#FFFFFF")
            }
        }

        data?.let {
            if (it.missing !=null) {
                if (it.missing != "0") {
                    holder.binding.tvSuperMarketItems.setTextColor(Color.parseColor("#FF3232"))
                    holder.binding.tvSuperMarketItems.text = it.missing.toString() + " ITEMS MISSING"
                } else {
                    holder.binding.tvSuperMarketItems.setTextColor(Color.parseColor("#06C169"))
                    holder.binding.tvSuperMarketItems.text = "ALL ITEMS"
                }
            }

            if (it.total != null) {
                val totalValue = it.total?:0.0
                val formattedTotal = if (totalValue % 1 == 0.0) {
                    totalValue.toInt().toString() // Show without decimal
                } else {
                    String.format("%.2f", totalValue) // Show two decimals
                }
                holder.binding.tvSuperMarketRupees.text = "$$formattedTotal"
            }


            it.distance?.let{ distance->
                holder.binding.tvMiles.text = "$distance miles"
            }


            /*  if (it.total != null) {
                  val roundedNetTotal = it.total.let {
                      BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toDouble()
                  }
                  holder.binding.tvSuperMarketRupees.text = "$$roundedNetTotal"
              }*/
            /*
                        holder.binding.tvSuperMarketItems.text = it.store_name ?: ""*/
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
                updateSelection(position)
                onItemSelectListener.itemSelect(position, storesData!![position].store_uuid.toString(), "SuperMarket")
            }else{
                Toast.makeText(requireActivity, "Store ${data?.store_name} is closed", Toast.LENGTH_SHORT).show()
            }
        }

        // Ensure proper layout refresh
        holder.binding.root.requestLayout()

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

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSelection(selectedPosition: Int) {
        storesData?.forEachIndexed { index, stores ->
            stores.is_slected = if (index == selectedPosition) 1 else 0
        }
        notifyDataSetChanged()
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(list: MutableList<Store>){
        storesData=list
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return storesData?.size ?: 0
    }

    class ViewHolder(var binding: AdapterLayoutSupermarketBinding) :
        RecyclerView.ViewHolder(binding.root)
}

