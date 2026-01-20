package com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.apiresponse.CookedTabRandomModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CookedTabViewModel@Inject constructor(private val repository: MainRepository) : ViewModel()  {



    private var _data: CookedTabRandomModelData? = null
    private var _date: String? = null
    val data: CookedTabRandomModelData? get() = _data
    val date: String? get() = _date
    private var _status:String?="1"
    val type: String? get() = _status


    suspend fun cookedDateRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                date: String,planType:String){
        repository.cookPlanDateRequestApi({ successCallback(it) },date,planType)
    }

    suspend fun removeMealApi(successCallback: (response: NetworkResult<String>) -> Unit,
                                cookedId: String){
        repository.removeMealCookApi({ successCallback(it) },cookedId)
    }

    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }

    suspend fun likeUnlikeRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                  uri: String,likeType: String,type:String){
        repository.likeUnlikeRequestApi({ successCallback(it) },uri,likeType,type)
    }

    suspend fun recipeServingCountRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddToPlanRequestApi({ successCallback(it) },jsonObject)
    }

    suspend fun recipeSearchApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch: JsonObject?){
        repository.recipeSearchApi ({ successCallback(it) },itemSearch)
    }

    suspend fun recipeAddToPlanRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddToPlanRequestApi({ successCallback(it) },jsonObject)
    }


    // set Home Data
    fun setData(dataItem: CookedTabRandomModelData?, type:String?, date:String?) {
        _data = dataItem
        _status = type
        _date = date
    }

}