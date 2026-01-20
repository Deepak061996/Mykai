package com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.activitymodel

data class ActivityDataModel(
    val title: String?,
    val description: String?,
    val short: String?="",
    var is_selected: Int=0,
)