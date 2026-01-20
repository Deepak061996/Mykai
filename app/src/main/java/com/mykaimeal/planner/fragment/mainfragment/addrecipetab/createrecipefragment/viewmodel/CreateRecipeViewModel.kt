package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CreateRecipeViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {
    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }


    suspend fun getUnitRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getUnitRequest { successCallback(it) }
    }


    suspend fun recipeSearchApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch: String?){
        repository.createRecipeUrlApi ({ successCallback(it) },itemSearch)
    }

    suspend fun recipeSearchFromURLApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        itemSearch: String?,
        apiCall: String?
    ){
        repository.recipeSearchFromURLApi ({ successCallback(it) },itemSearch,apiCall)
    }

    suspend fun recipeSearchIngredientApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch: String?, type: String?){
        repository.recipeSearchIngredientApi ({ successCallback(it) },itemSearch,type)
    }

    suspend fun createRecipeRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject,screenName:String
    ){
        repository.createRecipeRequestApi({ successCallback(it) },jsonObject,screenName)
    }


    suspend fun convertUnitRequestApi(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.convertUnitRequestApi({ successCallback(it) },jsonObject)
    }

}