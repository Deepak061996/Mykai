package com.mykaimeal.planner.adapter

import android.graphics.drawable.Drawable
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
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.AdapterMealItemBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Recipe
import com.mykaimeal.planner.listener.RecipeDetailListener


class SearchMealRecipeAdapter(var datalist: MutableList<Recipe>?,
                              private var requireActivity: FragmentActivity,
                              private var onItemClickListener: RecipeDetailListener
): RecyclerView.Adapter<SearchMealRecipeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterMealItemBinding = AdapterMealItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         val data= datalist?.get(position)

        if (data?.recipe?.label!=null){
            holder.binding.tvRecipeName.text= data.recipe.label
        }

        if (data?.recipe?.images?.SMALL?.url!=null){
            Glide.with(requireActivity)
                .load(data.recipe.images.SMALL.url)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility= View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.binding.layProgess.root.visibility= View.GONE
                        return false
                    }
                })
                .into(holder.binding.imgRecipe)
        }else{
            holder.binding.layProgess.root.visibility= View.GONE
        }

        holder.binding.relativeBreakFast.setOnClickListener{
            val dataLocal= data?.recipe?.mealType?.get(0)?.split(",")
            val formattedFoodName = dataLocal?.get(0)!!.replaceFirstChar { it.uppercase() }
            onItemClickListener.itemRecipeSelect(data.recipe.uri,formattedFoodName,"details")
        }

    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }

    class ViewHolder(var binding: AdapterMealItemBinding) : RecyclerView.ViewHolder(binding.root){

    }

}