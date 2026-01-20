package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterCompareItemBinding
import com.mykaimeal.planner.databinding.AdapterSearchFilterItemBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.Diet

class AdapterCompareItem() : RecyclerView.Adapter<AdapterCompareItem.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCompareItemBinding =
            AdapterCompareItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (position % 2 == 0) {
            holder.binding.tvName.setTextColor(Color.parseColor("#06C169"))
            holder.binding.tvPrice.setTextColor(Color.parseColor("#06C169"))
        } else {
            holder.binding.tvName.setTextColor(Color.parseColor("#FE9F45"))
            holder.binding.tvPrice.setTextColor(Color.parseColor("#FE9F45"))
        }

    }

    override fun getItemCount(): Int {
        return 3
    }


    class ViewHolder(var binding: AdapterCompareItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}