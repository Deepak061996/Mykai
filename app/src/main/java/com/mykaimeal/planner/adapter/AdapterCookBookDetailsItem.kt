package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.util.Log
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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.databinding.AdapterCookbookDetailsItemBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse.CookBookDataModel
import com.mykaimeal.planner.messageclass.ErrorMessage

class AdapterCookBookDetailsItem(var datalist: MutableList<CookBookDataModel>?, var requireActivity: FragmentActivity, private var onItemClickListener: OnItemClickListener)
    : RecyclerView.Adapter<AdapterCookBookDetailsItem.ViewHolder>() {

    private var lastIndex:Int=-1

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(datalistLocal: MutableList<CookBookDataModel>?){
        datalist=datalistLocal
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterCookbookDetailsItemBinding =
            AdapterCookbookDetailsItemBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    @SuppressLint("MissingInflatedId", "SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= datalist?.get(position)

        if (lastIndex == position){
            holder.binding.cardViewItems.visibility=View.VISIBLE
        }else{
            holder.binding.cardViewItems.visibility=View.GONE
        }

        if (data?.shared==0){
            holder.binding.imgThreeDot.visibility=View.VISIBLE
        }else{
            holder.binding.imgThreeDot.visibility=View.GONE
        }

        if (data?.is_created == true){
            holder.binding.tvEditRecipe.visibility = View.VISIBLE
        }else{
            holder.binding.tvEditRecipe.visibility = View.GONE
        }

        if (data?.data?.recipe?.label!=null){
            holder.binding.tvBreakfast.text= data.data.recipe.label
        }

        if (data?.data?.recipe?.totalTime!=null){
            holder.binding.tvTime.text=""+ data.data.recipe.totalTime+" min"
        }

        if (data?.data?.recipe?.images?.SMALL?.url!=null){
            Glide.with(requireActivity)
                .load(data.data.recipe.images.SMALL.url)
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
                .into(holder.binding.imageData)
        }else{
            holder.binding.layProgess.root.visibility= View.GONE
        }

        holder.binding.tvAddToPlan.setOnClickListener {
            data?.data?.let {
                onItemClickListener.itemClick(position,"1","")
            }?: run {
                BaseApplication.alertError(requireActivity, ErrorMessage.recipeTimeError, false)
            }
        }

        holder.binding.imgBasket.setOnClickListener {
            data?.data?.let {
                var mealType = ""
                if (datalist!![position].data?.recipe?.mealType != null && datalist!![position].data?.recipe?.mealType?.isNotEmpty() == true) {
                    mealType = datalist!![position].data?.recipe?.mealType!![0]
                    mealType = if (mealType.contains("/")) {
                        mealType.split("/")[0] // Get the first part before the slash
                    } else {
                        mealType // Return as is if no slash is present
                    }
                }
                onItemClickListener.itemClick(position,"2",mealType)
            }?: run {
                BaseApplication.alertError(requireActivity, ErrorMessage.recipeTimeError, false)
            }

        }

        holder.binding.tvMoveRecipe.setOnClickListener{
            holder.binding.cardViewItems.visibility=View.GONE
            lastIndex=-1
            onItemClickListener.itemClick(position,"5","")

        }

        holder.binding.tvEditRecipe.setOnClickListener {
            holder.binding.cardViewItems.visibility=View.GONE
            lastIndex=-1
            onItemClickListener.itemClick(position,"7","")
        }

        holder.binding.tvRemoveRecipe.setOnClickListener{
            holder.binding.cardViewItems.visibility=View.GONE
            lastIndex=-1
            onItemClickListener.itemClick(position,"6","")
        }

        holder.itemView.setOnClickListener {
            data?.data?.let {
                onItemClickListener.itemClick(position,"4","")
            }?:run {
                BaseApplication.alertError(requireActivity, ErrorMessage.recipeTimeError, false)
            }

        }

        holder.binding.imgThreeDot.setOnClickListener{
            data?.data?.let {
                lastIndex = if (lastIndex == position) {
                    -1
                } else {
                    position
                }
                notifyDataSetChanged()
            }?: run {
                BaseApplication.alertError(requireActivity, ErrorMessage.recipeTimeError, false)
            }
        }


    }

    override fun getItemCount(): Int {
        return datalist!!.size
    }

    class ViewHolder(var binding: AdapterCookbookDetailsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}