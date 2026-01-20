package com.mykaimeal.planner.fragment.mainfragment.viewmodel.instacartviewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
@HiltViewModel
class InstaCartViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {


    suspend fun scanResultRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject){
        repository.scanImageRequestApi({ successCallback(it) }, jsonObject)
    }



}