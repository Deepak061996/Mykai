package com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse

import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.CookWare
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Instruction

data class RecipeModel(
    val label: String?,
    val source: String?,
    val description: String?,
    val source_url: String?,
    val url: String?,
    val uri: String?,
    val createdType: String?,
    val type: String?,
    val mealType: MutableList<String>?,
    val images: ImagesModel?,
    val totalNutrients: TotalNutrientsModel?,
    val calories: Double?,
    val totalTime: Int?,
    val prep_time: Int?,
    val servings: Int?,
    val yield: Any?,
    val statusInGredients: Boolean=false,
    var ingredients: MutableList<IngredientsModel>?,
    val cookware: MutableList<CookwareModel>?,
    val instructionLines: MutableList<String>?,
    val instructions: MutableList<Instruction>?,

    )