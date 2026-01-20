package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.ItemTextViewCreateBinding
import com.mykaimeal.planner.listener.CreateRecipeListener

class SearchUnitItemAdapter(var datalist: MutableList<com.mykaimeal.planner.fragment.mainfragment.addrecipetab.unitList.Data>, var listener: CreateRecipeListener) : RecyclerView.Adapter<SearchUnitItemAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemTextViewCreateBinding =
            ItemTextViewCreateBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val dataItem=datalist[position]

        dataItem.unit_name.let {
            holder.binding.texttitle.text=it
        }


        holder.itemView.setOnClickListener {
            listener.itemRecipeSelect(dataItem.id.toString(),dataItem.unit_name,dataItem.unit_name,dataItem.unit_name,"Select")
        }


    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: ItemTextViewCreateBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}