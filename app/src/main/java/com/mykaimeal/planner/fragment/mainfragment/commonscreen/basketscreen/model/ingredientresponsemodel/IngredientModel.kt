package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.ingredientresponsemodel

data class IngredientModel(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String,
    val success: Boolean
)