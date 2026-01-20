package com.mykaimeal.planner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mykaimeal.planner.databinding.ItemNewIngredientsViewWithHeaderBinding
import com.mykaimeal.planner.databinding.ItemNewRecipeViewBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Instruction

class NewRecipeDetailRecipeAdapter(
    private var recipeList: MutableList<Instruction>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_STEP = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (recipeList[position].header != null) TYPE_HEADER else TYPE_STEP
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemNewIngredientsViewWithHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemNewRecipeViewBinding.inflate(inflater, parent, false)
                StepViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = recipeList[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(item)
            is StepViewHolder -> holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = recipeList.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(rawItems: List<Instruction>) {
        val groupedList = mutableListOf<Instruction>()
        val ingredientItems = rawItems.filter { it.header.equals("Recipe", true) }
            .map { it.copy(header = null) }
        groupedList.addAll(ingredientItems)
        rawItems.groupBy { it.header ?: "" }.forEach { (header, ingredients) ->
            if (!header.equals("Recipe", true) && header.isNotEmpty()) {
                groupedList.add(Instruction(header, 0,null))
                groupedList.addAll(ingredients.map { it.copy(header = null) })
            }
        }
        recipeList = groupedList
        notifyDataSetChanged()
    }

    inner class StepViewHolder(val binding: ItemNewRecipeViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        /*fun bind(data: Instruction, position: Int) {
            val stepNumber = recipeList.take(position + 1).count { it.header == null }
            binding.tvStep.text = "Step-$stepNumber"
            binding.tvDescription.text = data.text ?: ""

        }*/

        fun bind(data: Instruction, position: Int) {
            // Peeche se nearest header dhoondo
            var lastHeaderPos = -1
            for (i in position downTo 0) {
                if (recipeList[i].header != null) {
                    lastHeaderPos = i
                    break
                }
            }

            // Agar header mila toh us header ke baad se steps count karo
            val stepNumber = if (lastHeaderPos != -1) {
                recipeList.subList(lastHeaderPos + 1, position + 1)
                    .count { it.header == null }
            } else {
                recipeList.take(position + 1).count { it.header == null }
            }

            binding.tvStep.text = "Step-$stepNumber"
            binding.tvDescription.text = data.text ?: ""
        }

    }

    inner class HeaderViewHolder(val binding: ItemNewIngredientsViewWithHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: Instruction) {
            if (data.header.equals("Recipe", true)) {
                binding.root.visibility = View.GONE   // pura header hide hoga
            } else {
                binding.root.visibility = View.VISIBLE
                binding.headerTitle.text = data.header
                binding.root.setOnClickListener(null) // headers shouldn’t be clickable
            }

        }
    }
}
