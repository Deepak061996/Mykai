package com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse

import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.DataPerWeek
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.Macroper

data class Data(
    var activity_level: String?,
    var bio: String?="",
    var calories: Int?=0,
    var carbs: Int?=0,
    val email: String,
    var fat: Int?=0,
    var gender: String?,
    var height: String?,
    var weight: String?,
    var dob: String?,
    var height_protein: String?,
    var height_type: String?,
    var target: String?,
    var weight_type: String?,
    var name: String?,
    val profile_img: String?,
    var protein: Int?=0,
    var goal_in_weeks: Double?,
    var target_weight: String?,
    var target_weight_type: String?,
    var time: Int?,
    var value_per_week: Double?,
    var macros: String?,
    var old_macro: String?,
    var macro_per: Macroper?,
    var macro_options: String?,
    var disclaimer: String?,
    var data_per_week: MutableList<DataPerWeek>?,
    var typeStatus: String? = "0",
)