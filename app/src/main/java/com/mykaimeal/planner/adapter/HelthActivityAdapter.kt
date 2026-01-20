package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBodyGoalsBinding
import com.mykaimeal.planner.databinding.ItemTextViewBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.activitymodel.ActivityDataModel

class HelthActivityAdapter(
    private var list: MutableList<ActivityDataModel>,
    var requireActivity: FragmentActivity,
    private var onItemClickedListener: OnItemClickedListener
) : RecyclerView.Adapter<HelthActivityAdapter.ViewHolder>() {

    private var selectedIds = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemTextViewBinding = ItemTextViewBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=list[position]
        holder.binding.texttitle.text = data.title

        if (data.short.equals("",true)){
            holder.binding.textdes.text = data.description
        }else{
            holder.binding.textdes.text = data.description+"\n"+data.short
        }

        if (data.is_selected==1){
            holder.binding.relTodayTimers.setBackgroundResource(R.drawable.calendar_events_health_target)
        }else{
            holder.binding.relTodayTimers.setBackgroundResource(0)
        }

        holder.itemView.setOnClickListener {
            onItemClickedListener.itemClicked(position,null,"","")
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(var binding: ItemTextViewBinding) : RecyclerView.ViewHolder(binding.root) {

    }

}