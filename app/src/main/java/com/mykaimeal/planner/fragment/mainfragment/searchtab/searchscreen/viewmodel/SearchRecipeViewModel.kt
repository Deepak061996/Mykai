package com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.searchtab.searchscreen.apiresponse.Data
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SearchRecipeViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {


    private var _data: Data? = null
    private var _search: String? = null
    val data: Data? get() = _data
    val search: String? get() = _search


    suspend fun recipeforSearchApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.recipeforSearchApi { successCallback(it) }
    }

    suspend fun recipeSearchApi(successCallback: (response: NetworkResult<String>) -> Unit, itemSearch: JsonObject?){
        repository.recipeSearchFromSearchApi ({ successCallback(it) },itemSearch)
    }


    fun setData(data: Data?, search: String){
        _data=data
        _search=search
    }

    suspend fun recipePreferencesApi(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.recipePreferencesApi { successCallback(it) }
    }

    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }

    suspend fun likeUnlikeRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                  uri: String,likeType: String,type:String){
        repository.likeUnlikeRequestApi({ successCallback(it) },uri,likeType,type)
    }


    suspend fun getMealByUrl(successCallback: (response: NetworkResult<String>) -> Unit,url:String?){
        repository.getMealByUrl({successCallback(it)},url)
    }

}