package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
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
import com.chauthai.swipereveallayout.SwipeRevealLayout
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterBasketIngItemBinding
import com.mykaimeal.planner.databinding.AdapterBasketNewIngItemBinding
import com.mykaimeal.planner.databinding.AddmoreitemshopingBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.messageclass.ErrorMessage

class AddMoreIngredientsAdapter(private var ingredientsData: MutableList<Ingredient>?,
                                private var requireActivity: FragmentActivity,
                                private var onItemSelectListener: OnItemSelectListener
):
    RecyclerView.Adapter<AddMoreIngredientsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AddmoreitemshopingBinding = AddmoreitemshopingBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data= ingredientsData?.get(position)

        data?.pro_name?.let {
            holder.binding.name.text=it?:""
        }

        data?.sch_id?.let {
            holder.binding.textCount.text= ("" + it) ?: "1"
        }


        holder.binding.imgNotification.setOnClickListener {
            onItemSelectListener.itemSelect(position,"","Delete")
        }


        holder.binding.imageAddIcon.setOnClickListener {
            onItemSelectListener.itemSelect(position,"","add")
        }

        holder.binding.imageMinusIcon.setOnClickListener {
            onItemSelectListener.itemSelect(position,"","minus")
        }


    }

    override fun getItemCount(): Int {
        return ingredientsData!!.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(ingredientList: MutableList<Ingredient>) {
        ingredientsData=ingredientList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AddmoreitemshopingBinding) : RecyclerView.ViewHolder(binding.root){
    }

}