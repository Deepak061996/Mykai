package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.ItemViewTargetBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.DataPerWeek

class HelthActivityTargetAdapter(private var list: MutableList<DataPerWeek>, var requireActivity: FragmentActivity, private var onItemClickedListener: OnItemClickedListener, var type: String) : RecyclerView.Adapter<HelthActivityTargetAdapter.ViewHolder>() {

    private var selectedIds = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemViewTargetBinding = ItemViewTargetBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data  = list[position]


        if (data.is_selected==1){
            holder.binding.relTodayTimers.setBackgroundResource(R.drawable.calendar_events_health_target)
        }else{
            holder.binding.relTodayTimers.setBackgroundResource(0)
        }


        data.name?.let {
            holder.binding.texttitle.text = it
        }

        data.value?.let {
            val value= if (type.equals("lb",true)){
                if (it == 1.0){
                    "1"
                }else{
                    it.toString()
                }
            }else{
                BaseApplication.convertToKg(it)
            }
            holder.binding.textdes.text = "You'll ${data.tar?.lowercase()} $value ${type}/week"
        }

        data.days?.let {
            val (months, days) = BaseApplication.convertDaysToMonthsAndDays(it)
            if (months == 0 && days == 0) {
                holder.binding.textTime.visibility = View.VISIBLE
            } else {
                val monthText = if (months > 0) "$months ${if (months == 1) "month" else "months"}" else ""
                val dayText = if (days > 0) "$days ${if (days == 1) "day" else "days"}" else ""

                val estimatedText = buildString {
                    append("Estimated time : ")
                    if (monthText.isNotEmpty()) append(monthText)
                    if (monthText.isNotEmpty() && dayText.isNotEmpty()) append(" and ")
                    if (dayText.isNotEmpty()) append(dayText)
                }
                holder.binding.textTime.visibility = View.VISIBLE
                holder.binding.textTime.text = estimatedText
            }
        }

        holder.itemView.setOnClickListener {
            onItemClickedListener.itemClicked(position,null,"target","")
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: ItemViewTargetBinding) : RecyclerView.ViewHolder(binding.root) {

    }

}