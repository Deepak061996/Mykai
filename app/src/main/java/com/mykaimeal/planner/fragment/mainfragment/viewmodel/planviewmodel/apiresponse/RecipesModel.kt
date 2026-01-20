package com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponse

import com.google.gson.annotations.SerializedName

data class RecipesModel(
    var Breakfast: MutableList<BreakfastModel>?,
    var Dinner: MutableList<BreakfastModel>?,
    var Lunch: MutableList<BreakfastModel>?,
    @SerializedName("Snacks")
    var Snack: MutableList<BreakfastModel>?,
    @SerializedName("Brunch")
    var Teatime: MutableList<BreakfastModel>?,
    var Dessert: MutableList<BreakfastModel>?

)