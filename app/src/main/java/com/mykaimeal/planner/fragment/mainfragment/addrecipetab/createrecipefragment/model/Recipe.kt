package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model

import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefromimage.model.RecyclerViewItemModel

data class Recipe(
    val calories: Double?,
    val cautions: List<String>?,
    val cuisineType: List<String>?,
    val dietLabels: List<String>?,
    val digest: List<Any>?,
    val dishType: List<String>?,
    val healthLabels: List<String>?,
    val image: String?,
    val images: Images?,
    val ingredientLines: List<String>?,
    val ingredients: List<Ingredient>?,
    val instructionLines: List<String>?,
    val label: String?,
    val mealType: List<String>?,
    val shareAs: String?,
    val source: String?,
    val description: String?,
    val totalDaily: Any?,
    val totalNutrients: Any?,
    val totalTime: Int?,
    val is_public: Int?,
    val servings: Int?,
    val totalWeight: Double?,
    val uri: String?,
    val cookBook: String?,
    val yield: Int?,
    val ingredientList: MutableList<RecyclerViewItemModel>?,
    val cookList: MutableList<RecyclerViewCookIngModel>?
)