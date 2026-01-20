package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.unitList

data class UnitModel(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String,
    val success: Boolean
)