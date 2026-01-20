package com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel

data class Data(
    val bmr: Double?,
    val carbs: Int?,
    val fat: Int?,
    val calories: Int?,
    val protein: Int?,
    val tdee: Double?,
    val height: String?,
    val height_type: String?,
    val target: String?,
    val target_weight_type: String?,
    val weight: String?,
    val macro_per: Macroper?,
    val macro_options: String?,
    val macros: String?,
    val disclaimer: String?,
    val weight_type: String?,
    var goal_in_weeks: Double?,
    var target_weight: String?,
    var time: Int?,
    var data_per_week: MutableList<DataPerWeek>,
    var value_per_week: Double?
)