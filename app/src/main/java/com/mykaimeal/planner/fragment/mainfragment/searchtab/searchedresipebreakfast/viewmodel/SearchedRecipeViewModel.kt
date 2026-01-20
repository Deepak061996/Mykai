package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchedresipebreakfast.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.model.Recipe
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchedRecipeViewModel @Inject constructor(private val repository: MainRepository) :
    ViewModel() {





    suspend fun recipeSearchedApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch: JsonObject?) {
        repository.recipeSearchApi({ successCallback(it) }, itemSearch)
    }

    suspend fun recipeFilterSearchApi(
        successCallback: (response: NetworkResult<String>) -> Unit,
        mealType: MutableList<String>?,
        health: MutableList<String>?,
        time: String?,
        cuisine: MutableList<String>?,
        nutrition: String?
    ) {
        repository.recipeFilterSearchApi({ successCallback(it) }, mealType, health, time,cuisine,nutrition)
    }

    suspend fun recipeAddToPlanRequest(
        successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ) {
        repository.recipeAddToPlanRequestApi({ successCallback(it) }, jsonObject)
    }

    suspend fun addBasketRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        uri: String, quantity: String, type:String
    ) {
        repository.addBasketRequestApi({ successCallback(it) }, uri, quantity,type)
    }

    suspend fun likeUnlikeRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        uri: String, likeType: String, type: String
    ) {
        repository.likeUnlikeRequestApi({ successCallback(it) }, uri, likeType, type)
    }

    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.getCookBookRequestApi { successCallback(it) }
    }


    private var _data: MutableList<Recipe>? = null
    val data: MutableList<Recipe>? get() = _data

    fun setData(data: MutableList<Recipe>?){
        _data=data
    }


}