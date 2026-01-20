package com.mykaimeal.planner.adapter

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
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.messageclass.ErrorMessage

class NewIngredientsShoppingAdapter(private var ingredientsData: MutableList<Ingredient>?,
                                    private var requireActivity: FragmentActivity,
                                    private var onItemSelectListener: OnItemSelectListener
):
    RecyclerView.Adapter<NewIngredientsShoppingAdapter.ViewHolder>() {


    // Track currently opened swipe layout
    private var openedSwipeLayout: SwipeRevealLayout? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterBasketNewIngItemBinding = AdapterBasketNewIngItemBinding.inflate(inflater, parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data= ingredientsData?.get(position)

        holder.binding.layServing.visibility = View.VISIBLE

        if (data != null) {
            if (data.status == 1){
                holder.binding.imgCheckbox.setImageResource(R.drawable.orange_checkbox_images)
            }else{
                holder.binding.imgCheckbox.setImageResource(R.drawable.orange_uncheck_box_images)
            }

            if (data.sch_id!=null){
                holder.binding.textCount.text=data.sch_id.toString()
            }else{
                holder.binding.textCount.text=""
            }

            if (data.pro_name!=null){
                val foodName = data.pro_name
                val result = foodName.mapIndexed { index, c ->
                    if (index == 0 || c.isUpperCase()) c.uppercaseChar() else c
                }.joinToString("")
                holder.binding.tvTitleName.text=result
            }else{
                holder.binding.tvTitleName.text=""
            }
            if (data.quantity!=null){
                val measurement=""+(data.quantity?:0)+ (data.unit_of_measurement?:"")
                holder.binding.tvTitleDesc.text=""+measurement
            }else{
                holder.binding.tvTitleDesc.text=""
            }
            if (data.pro_img!=null){
                Glide.with(requireActivity)
                    .load(data.pro_img)
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
                    .into(holder.binding.imageFood)
            }else{
                Glide.with(requireActivity)
                    .load(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .into(holder.binding.imageFood)
                holder.binding.layProgess.root.visibility= View.GONE
            }
        }

        holder.binding.clickItem.setOnClickListener {
            onItemSelectListener.itemSelect(position,"click","IngredientsClick")
        }

        holder.binding.imageMinusIcon.setOnClickListener{
            if (ingredientsData?.get(position)?.sch_id.toString().toInt() > 1) {
                onItemSelectListener.itemSelect(position,"Minus","ShoppingIngredients")
            }else{
                Toast.makeText(requireActivity,ErrorMessage.servingError, Toast.LENGTH_LONG).show()
            }
        }

        holder.binding.imageAddIcon.setOnClickListener{
            if (ingredientsData?.get(position)?.sch_id.toString().toInt() < 1000) {
                onItemSelectListener.itemSelect(position,"Plus","ShoppingIngredients")
            }
        }

        // Swipe layout behavior
        holder.binding.swipeLayout.setSwipeListener(object : SwipeRevealLayout.SwipeListener {
            override fun onClosed(view: SwipeRevealLayout) {
                if (openedSwipeLayout == view) {
                    openedSwipeLayout = null
                }
            }

            override fun onOpened(view: SwipeRevealLayout) {
                if (openedSwipeLayout != null && openedSwipeLayout != view) {
                    openedSwipeLayout?.close(true)
                }
                openedSwipeLayout = view
            }

            override fun onSlide(view: SwipeRevealLayout, slideOffset: Float) {
                // Optional: Animate background color or something else
            }
        })

        holder.binding.deleteLayout.setOnClickListener{
            // Close the swipe layout before deletion to prevent UI artifacts
            holder.binding.swipeLayout.close(true)
            // Reset the reference to the opened swipe layout
            if (openedSwipeLayout == holder.binding.swipeLayout) {
                openedSwipeLayout = null
            }
            onItemSelectListener.itemSelect(position,"Delete","Ingredients")
        }

    }

    override fun getItemCount(): Int {
        return ingredientsData!!.size
    }

    fun updateList(ingredientList: MutableList<Ingredient>) {
        ingredientsData=ingredientList
        notifyDataSetChanged()
    }

    class ViewHolder(var binding: AdapterBasketNewIngItemBinding) : RecyclerView.ViewHolder(binding.root){
    }

}