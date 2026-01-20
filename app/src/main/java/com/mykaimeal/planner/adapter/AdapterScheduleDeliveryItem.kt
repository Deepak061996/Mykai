package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemLongClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterScheduleDeliveryItemBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.responseschedule.Hour
import com.mykaimeal.planner.model.DataModel

class AdapterScheduleDeliveryItem(private var dataListModel: MutableList<Hour>,
                                  private var requireActivity: FragmentActivity,
                                  private var onItemClickedListener: OnItemLongClickListener,
                                  private var type: String
):
    RecyclerView.Adapter<AdapterScheduleDeliveryItem.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterScheduleDeliveryItemBinding = AdapterScheduleDeliveryItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val data = dataListModel[position]


        data.time?.let {
            holder.binding.textThreeWindows.text = it
        }

        data.offer?.let {
            holder.binding.tvOffTomorrow.text = it
        }

        val iconRes = if (data.status) {
            R.drawable.radio_green_icon
        } else {
            R.drawable.radio_uncheck_gray_icon
        }
        holder.binding.imgRadioSelect.setImageResource(iconRes)


        holder.itemView.setOnClickListener {
            onItemClickedListener.itemLongClick(position,"",type,"timeSelected")
        }


    }



    override fun getItemCount(): Int {
        return dataListModel.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updated(dataList: MutableList<Hour>,fullAddress:String) {
        dataListModel=dataList
        type=fullAddress
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterScheduleDeliveryItemBinding) : RecyclerView.ViewHolder(binding.root){
    }

}