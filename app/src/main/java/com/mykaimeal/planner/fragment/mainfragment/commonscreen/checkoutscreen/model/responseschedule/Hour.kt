package com.mykaimeal.planner.fragment.mainfragment.commonscreen.checkoutscreen.model.responseschedule

import com.google.gson.annotations.SerializedName

data class Hour(
    val offer: String?="0",
    val time: String?,
    @SerializedName("selected")
    var status: Boolean
)