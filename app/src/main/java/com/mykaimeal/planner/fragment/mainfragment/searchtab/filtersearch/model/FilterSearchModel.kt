package com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model

data class FilterSearchModel(
    val code: Int?,
    val `data`: FilterSearchModelData?,
    val message: String?,
    val success: Boolean?
)

data class FilterSearchModelData(
    val Diet: MutableList<Diet>?,
    val cook_time: MutableList<CookTime>?,
    val mealType: MutableList<MealType>?,
    val dishType: MutableList<DishType>?,
    val protein: MutableList<Protein>?
)

data class Diet(
    val name: String?,
    var selected:Boolean?=false,
    val value: String?
)

data class CookTime(
    val name: String?,
    val value: String?,
    var selected:Boolean?=false
)

data class MealType(
    val id: Int?,
    val image: String?,
    val name: String?,
    val value: String?,
    var selected:Boolean?=false
)

data class DishType(
    val created_at: String?,
    val deleted_at: Any?,
    val id: Int?,
    val name: String?,
    val updated_at: String?,
    var selected:Boolean?=false

)

data class Protein(
    val name: String?,
    val value: String?,
    var selected:Boolean?=false
)
