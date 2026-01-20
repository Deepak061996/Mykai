package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.ItemNewCookwareViewBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.ingredientmodel.IngredientAddModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.CookWare
import com.mykaimeal.planner.listener.CreateRecipeSelectListener

class NewCookWareCreateRecipeAdapter(
    private var requireActivity: FragmentActivity,
    private var cookwareList: MutableList<CookWare>,
    var listener: CreateRecipeSelectListener
):
    RecyclerView.Adapter<NewCookWareCreateRecipeAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemNewCookwareViewBinding = ItemNewCookwareViewBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data=cookwareList[position]

        holder.binding.tvtext.text= data.name?:""


        data.image?.let {
            Glide.with(requireActivity)
                .load(it)
                .placeholder(R.drawable.cookwareicon)
                .error(R.drawable.cookwareicon)
                .into(holder.binding.imageProfile)
        }?:run {
            Glide.with(requireActivity)
                .load(R.drawable.cookwareicon)
                .into(holder.binding.imageProfile)
        }

        holder.itemView.setOnClickListener {
            listener.itemSelectType(position,"cookware")
        }

    }

    override fun getItemCount(): Int {
        return cookwareList.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(data: MutableList<CookWare>){
        cookwareList=data
        notifyDataSetChanged()
    }


    class ViewHolder(var binding: ItemNewCookwareViewBinding) : RecyclerView.ViewHolder(binding.root){
    }

}