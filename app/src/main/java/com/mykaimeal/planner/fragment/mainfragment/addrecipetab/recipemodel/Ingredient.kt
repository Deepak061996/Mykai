package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel

data class Ingredient(
    val header: String?,
    val image: String?,
    val image_url: String?,
    val name: String?,
    val quantity: String?,
    val text: String?,
    val unit: String?,
    val itemId:Int =0
)