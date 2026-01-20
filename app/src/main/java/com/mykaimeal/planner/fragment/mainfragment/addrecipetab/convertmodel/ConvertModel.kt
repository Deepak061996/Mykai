package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.convertmodel

data class ConvertModel(
    val code: Int,
    val `data`: MutableList<Data>?,
    val message: String,
    val success: Boolean
)