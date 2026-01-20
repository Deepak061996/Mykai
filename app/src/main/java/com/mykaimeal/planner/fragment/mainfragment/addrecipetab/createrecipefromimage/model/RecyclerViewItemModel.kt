package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefromimage.model

import com.google.gson.annotations.SerializedName

data class RecyclerViewItemModel(
    var uri: String?,
    @SerializedName("name")
    var ingredientName: String?,
    var status: Boolean = false,
    var quantity: String?,
    @SerializedName("measure")
    var measurement: String?=""
)