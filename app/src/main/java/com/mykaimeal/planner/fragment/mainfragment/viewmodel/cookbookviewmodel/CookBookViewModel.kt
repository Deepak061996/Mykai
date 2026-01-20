package com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse.CookBookDataModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.cookbookviewmodel.apiresponse.CookBookListApiResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class CookBookViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getCookBookRequestApi { successCallback(it) }
    }


    suspend fun updateCookBookApi(successCallback: (response: NetworkResult<String>) -> Unit,cookBookID: String?){
        repository.updateCookBookApi ({ successCallback(it) },cookBookID)
    }

    suspend fun getCookBookTypeRequest(
        successCallback: (response: NetworkResult<String>) -> Unit,
        id: String?
    ){
        repository.getCookBookTypeRequestApi ({ successCallback(it) },id)
    }

    suspend fun addBasketRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                 uri: String,quantity: String,type:String){
        repository.addBasketRequestApi({ successCallback(it) },uri,quantity,type)
    }

    suspend fun recipeAddToPlanRequest(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.recipeAddToPlanRequestApi({ successCallback(it) },jsonObject)
    }

    suspend fun likeUnlikeRequest(successCallback: (response: NetworkResult<String>) -> Unit,
                                  uri: String,likeType: String,type:String){
        repository.likeUnlikeRequestApi({ successCallback(it) },uri,likeType,type)
    }

    suspend fun moveRecipeRequest(successCallback: (response: NetworkResult<String>) -> Unit, id: String,cookBook:String){
        repository.moveRecipeRequestApi({ successCallback(it) },id,cookBook)
    }

    suspend fun deleteCookBookRequest(successCallback: (response: NetworkResult<String>) -> Unit, id: String){
        repository.deleteCookBookRequestApi({ successCallback(it) },id)
    }

    suspend fun generateLinkUrl(successCallback: (response: NetworkResult<String>) -> Unit, link: RequestBody?, image: MultipartBody.Part?){
        repository.generateLinkUrl({ successCallback(it) },link,image)
    }

    // hold data in cookbook
    private var _dataCookBook: MutableList<Data>? = null
    val dataCookBook: MutableList<Data>? get() = _dataCookBook

    private var _dataCookBookList: MutableList<CookBookDataModel>? = null
    private var _dataItemRoot: CookBookListApiResponse? = null
    val dataCookBookList: MutableList<CookBookDataModel>? get() = _dataCookBookList
    val dataItemRoot: CookBookListApiResponse? get() = _dataItemRoot


    fun setDataCookBook(dataItem: MutableList<Data>?) {
        _dataCookBook = dataItem
    }

    fun setDataCookBookList(dataItem: MutableList<CookBookDataModel>?) {
        _dataCookBookList = dataItem
    }


    fun setDataCookBookChristmasList(dataItemRoot: CookBookListApiResponse?) {
        _dataItemRoot = dataItemRoot
    }

}