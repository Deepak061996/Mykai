package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mykaimeal.planner.databinding.ItemTextViewCreateBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.ingredientresponsemodel.Data
import com.mykaimeal.planner.listener.CreateRecipeListener

class SearchIngredientItemAdapter(
    var datalist: MutableList<Data>,
    var listener: CreateRecipeListener,
    var changeStatus: String
) : RecyclerView.Adapter<SearchIngredientItemAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemTextViewCreateBinding =
            ItemTextViewCreateBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val dataItem=datalist[position]

        dataItem.name?.let {
            holder.binding.texttitle.text=it
        }

        holder.itemView.setOnClickListener {
            listener.itemRecipeSelect(dataItem.id.toString(),dataItem.name.toString(),dataItem.image_url.toString()?:"",dataItem.unit_name.toString()?:"",changeStatus)
        }


    }

    override fun getItemCount(): Int {
        return datalist.size
    }


    class ViewHolder(var binding: ItemTextViewCreateBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}