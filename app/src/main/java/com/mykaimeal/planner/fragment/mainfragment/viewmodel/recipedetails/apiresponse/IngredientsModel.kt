package com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse

import com.google.gson.annotations.SerializedName

data class IngredientsModel(
    val text: String?,
    val food: String?,
    val image: String?,
    @SerializedName("category")
    val foodCategory: String?,
    val ingredient_cost: String?,
    val measure: String?,
    val quantity: Double?,
    val weight:Double?,
    val foodId:String?,
    val id:String?,
    var status:Boolean = false,
    var header: String?,
)