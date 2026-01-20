package com.mykaimeal.planner.listener

interface CreateRecipeListener {

    fun itemRecipeSelect(id: String?, title:String?,image:String?,unitName:String?,type:String?)
}