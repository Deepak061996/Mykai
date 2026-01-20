package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel

import com.google.gson.annotations.SerializedName

data class Instruction(
    @SerializedName("steps_headers")
    val header: String?,
    val step_order: Int?,
    val text: String?,
    var status:String?="1",
    var itemId:Int=1
)