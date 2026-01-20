package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.ItemNewCookwareViewBinding
import com.mykaimeal.planner.databinding.ItemNewIngredientsViewBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.ingredientmodel.IngredientAddModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.CookWare
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.CookwareModel
import com.mykaimeal.planner.listener.CreateRecipeSelectListener

class MissingIngredientAdapter(
    private var ingredientsData: MutableList<Ingredient>,
    private var requireActivity: FragmentActivity,
    private var onItemSelectListener: OnItemSelectListener
):
    RecyclerView.Adapter<MissingIngredientAdapter.ViewHolder>() {

    private var originalList: MutableList<Ingredient> = ingredientsData.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: ItemNewIngredientsViewBinding = ItemNewIngredientsViewBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data= ingredientsData.get(position)

        holder.binding.imgCheckbox.visibility = View.VISIBLE

        data.let {

            if (it.name!=null){
                val foodName = it.name
                val result = foodName.mapIndexed { index, c ->
                    if (index == 0 || c.isUpperCase()) c.uppercaseChar() else c
                }.joinToString("")
                holder.binding.name.text=result
            }

            if (it.quantity!=null){
                val measurement=""+(it.quantity?:0)+ (it.unit_of_measurement?:"")
                holder.binding.measure.text=""+measurement
            }

            if (it.newStatus){
                holder.binding.imgCheckbox.setImageResource(R.drawable.orange_checkbox_images)
            }else{
                holder.binding.imgCheckbox.setImageResource(R.drawable.orange_uncheck_box_images)
            }

            if (it.pro_img!=null){
                Glide.with(requireActivity)
                    .load(it.pro_img)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .into(holder.binding.imageProfile)
            }else{
                Glide.with(requireActivity)
                    .load(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .into(holder.binding.imageProfile)
            }

        }

        holder.binding.root.setOnClickListener {
            onItemSelectListener.itemSelect(position,data.id.toString(),"IngredientsClick")
        }
    }

    override fun getItemCount(): Int {
        return ingredientsData.size
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateList(data: MutableList<Ingredient>) {
        originalList = data.toMutableList()
        ingredientsData = data
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        ingredientsData = if (query.isEmpty()) {
            originalList.toMutableList()
        } else {
            originalList.filter {
                it.name?.contains(query, ignoreCase = true) == true
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: ItemNewIngredientsViewBinding) : RecyclerView.ViewHolder(binding.root){
    }

}