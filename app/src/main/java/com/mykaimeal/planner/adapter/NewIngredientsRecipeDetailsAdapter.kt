
package com.mykaimeal.planner.adapter
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.databinding.ItemNewIngredientsViewBinding
import com.mykaimeal.planner.databinding.ItemNewIngredientsViewWithHeaderBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.IngredientsModel
import com.mykaimeal.planner.listener.CreateRecipeSelectListener

class NewIngredientsRecipeDetailsAdapter(
    private var items: MutableList<IngredientsModel>,
    private val requireActivity: FragmentActivity,
    var lsitener: OnItemSelectListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_INGREDIENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].header != null) TYPE_HEADER else TYPE_INGREDIENT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemNewIngredientsViewWithHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemNewIngredientsViewBinding.inflate(inflater, parent, false)
                IngredientViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(item)
            is IngredientViewHolder -> holder.bind(item,items)
        }
    }

    override fun getItemCount(): Int = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(rawItems: List<IngredientsModel>) {
        val groupedList = mutableListOf<IngredientsModel>()
        // 1. Ingredients ko filter karo
        val ingredientItems = rawItems.filter { it.header.equals("Ingredients", true) }
            .map { it.copy(header = null) }
        // 2. Pehle Ingredients ke items daalo (header ke bina)
        groupedList.addAll(ingredientItems)
        // 3. Ab baaki headers ko add karo
        rawItems.groupBy { it.header ?: "" }.forEach { (header, ingredients) ->
            if (!header.equals("Ingredients", true) && header.isNotEmpty()) {
                groupedList.add(IngredientsModel(null, null, null, null, null, null,null,null,null,null,false,header)) // header
                groupedList.addAll(ingredients.map { it.copy(header = null) })
            }
        }
        items = groupedList
        notifyDataSetChanged()
    }


    inner class IngredientViewHolder(private val binding: ItemNewIngredientsViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: IngredientsModel, items: MutableList<IngredientsModel>) {

            binding.imgCheckbox.visibility = View.VISIBLE

            binding.measure.text =
                if (!data.measure.isNullOrEmpty()) "${data.quantity ?: ""} ${data.measure}" else ""
            Glide.with(requireActivity)
                .load(data.image ?: R.drawable.ingredienticon)
                .placeholder(R.drawable.ingredienticon)
                .error(R.drawable.ingredienticon)
                .into(binding.imageProfile)

            binding.name.text = data.text ?: ""

            if (data.status!=null){
                if (data.status){
                    binding.imgCheckbox.setImageResource(R.drawable.orange_checkbox_images)
                }else{
                    binding.imgCheckbox.setImageResource(R.drawable.orange_uncheck_box_images)
                }
            }else{
                binding.imgCheckbox.setImageResource(R.drawable.orange_uncheck_box_images)
            }



            binding.relTodayTimers.setOnClickListener {
                val safePos = bindingAdapterPosition
                if (safePos != RecyclerView.NO_POSITION) {
                    val ingredientIndex = items.filter { it.header == null }
                        .indexOf(data)
                    lsitener.itemSelect(adapterPosition,data.id,"")
                }

            }



        }
    }

    inner class HeaderViewHolder(private val binding: ItemNewIngredientsViewWithHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(data: IngredientsModel) {
            if (data.header.equals("Ingredients", true)) {
                binding.root.visibility = View.GONE   // pura header hide hoga
            } else {
                binding.root.visibility = View.VISIBLE
                binding.headerTitle.text = data.header
            }
        }
    }
}



