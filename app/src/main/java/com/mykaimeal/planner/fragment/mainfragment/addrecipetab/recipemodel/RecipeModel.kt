package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel

data class RecipeModel(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String,
    val success: Boolean
)