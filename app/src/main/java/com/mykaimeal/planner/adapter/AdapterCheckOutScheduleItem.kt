package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemLongClickListener
import com.mykaimeal.planner.databinding.AdapterScheduleDeliveryItemBinding
import com.mykaimeal.planner.databinding.TimeViewBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.DateCheckOut
import com.mykaimeal.planner.model.DataModel

class AdapterCheckOutScheduleItem(private var dataListModel: MutableList<DateCheckOut>,
                                  private var requireActivity: FragmentActivity,
                                  private var onItemClickedListener: OnItemLongClickListener
):
    RecyclerView.Adapter<AdapterCheckOutScheduleItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: TimeViewBinding = TimeViewBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val  data=dataListModel[position]


        if (data.status==true){
            holder.binding.cardViewMainLayout.strokeColor = Color.parseColor("#06C169")
        }else{
            holder.binding.cardViewMainLayout.strokeColor = Color.parseColor("#FFFFFF")
        }

        holder.binding.textToday.text = data.label
        holder.binding.tvTodayDate.text = data.day


        holder.itemView.setOnClickListener {
            onItemClickedListener.itemLongClick(position,"","","slot")
        }

    }



    override fun getItemCount(): Int {
        return dataListModel.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updated(dataList: MutableList<DateCheckOut>) {
        dataListModel=dataList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: TimeViewBinding) : RecyclerView.ViewHolder(binding.root){
    }

}