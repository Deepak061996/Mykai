package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse

data class Data(
    val category: MutableList<Category>?,
    val ingredient: MutableList<Ingredient>?,
    val mealType: MutableList<MealType>?,
    val recipes: MutableList<Recipe>?,
    val preference_status: Int?=0
)