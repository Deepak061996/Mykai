package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.viewmodel

import androidx.lifecycle.ViewModel
import com.google.gson.JsonObject
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketproductsdetailsscreen.model.BasketProductsDetailsModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.BasketScreenModelData
import com.mykaimeal.planner.repository.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BasketScreenViewModel @Inject constructor(private val repository: MainRepository) : ViewModel()  {

    suspend fun getBasketUrl(successCallback: (response: NetworkResult<String>) -> Unit,storeId:String?,latitude:String?,longitude:String?){
        repository.getBasketUrl({ successCallback(it) },storeId,latitude, longitude)
    }

    suspend fun getAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getAddressUrl{ successCallback(it) }
    }

    suspend fun getInstaCart(successCallback: (response: NetworkResult<String>) -> Unit, jsonObject: JsonObject
    ){
        repository.getInstaCart({ successCallback(it) },jsonObject)
    }

    suspend fun removeBasketUrlApi(successCallback: (response: NetworkResult<String>) -> Unit,
                              cookedId: String?){
        repository.removeBasketUrlApi({ successCallback(it) },cookedId)
    }

    suspend fun basketYourRecipeIncDescUrl(successCallback: (response: NetworkResult<String>) -> Unit,
                              uri: String?,quantity:String?,type:String?){
        repository.basketYourRecipeIncDescUrl({ successCallback(it) },uri, quantity,type)
    }


    suspend fun basketIngIncDescUrl(successCallback: (response: NetworkResult<String>) -> Unit,
                              foodId: String?,quantity:String?){
        repository.basketIngIncDescUrl({ successCallback(it) },foodId, quantity)
    }

    suspend fun addAddressUrl(successCallback: (response: NetworkResult<String>) -> Unit, latitude: String?, longitude: String?,
                              streetName:String?,streetNum:String?,apartNum:String?,city:String?,state:String?,country:String?,
                              zipcode:String?,primary:String?,id:String?,type:String?) {
        repository.addAddressUrl({ successCallback(it) }, latitude, longitude, streetName, streetNum, apartNum, city,state, country, zipcode, primary, id, type)
    }


    suspend fun makeAddressPrimaryUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?) {
        repository.makeAddressPrimaryUrl({ successCallback(it) },id)
    }

    suspend fun selectStoreProductUrl(successCallback: (response: NetworkResult<String>) -> Unit,storeName:String?,storeId:String?) {
        repository.selectStoreProductUrl({ successCallback(it) },storeName,storeId)
    }

    suspend fun getProductsUrl(successCallback: (response: NetworkResult<String>) -> Unit,query:String?,foodId:String?,schId:String?){
        repository.getProductsUrl({ successCallback(it) },query,foodId,schId)
    }

    suspend fun getProductsDetailsUrl(successCallback: (response: NetworkResult<String>) -> Unit,proId:String?,query:String?,foodId:String?,schId:String?){
        repository.getProductsDetailsUrl({ successCallback(it) },proId,query,foodId,schId)
    }

    suspend fun getSelectProductsUrl(successCallback: (response: NetworkResult<String>) -> Unit,id:String?,productId:String?,schId:String?){
        repository.getSelectProductsUrl({ successCallback(it) },id, productId,schId)
    }

    suspend fun getStoreProductUrl(successCallback: (response: NetworkResult<String>) -> Unit) {
        repository.getStoreProductUrl{ successCallback(it) }
    }

    suspend fun addPurchasedElementUrlApi(successCallback: (response: NetworkResult<String>) -> Unit,ids: MutableList<String>?){
        repository.addPurchasedElemenentsUrlApi ({ successCallback(it) },ids)
    }

    suspend fun addGraphDataApi(successCallback: (response: NetworkResult<String>) -> Unit,storeName: String?,cardTotal: String?,date: String?){
        repository.addGraphDataApi ({ successCallback(it) },storeName,cardTotal,date)
    }


    suspend fun getSuperMarket(successCallback: (response: NetworkResult<String>) -> Unit,
                               latitude: String?,longitude: String?){
        repository.getSuperMarket({ successCallback(it) },latitude,longitude)
    }

    suspend fun getSuperMarketWithPage(successCallback: (response: NetworkResult<String>) -> Unit,
                                       latitude: String?,longitude: String?,pageCount: String?){
        repository.getSuperMarketWithPage({ successCallback(it) },latitude,longitude,pageCount)
    }

    suspend fun getcheckAvailablity(successCallback: (response: NetworkResult<String>) -> Unit){
        repository.getcheckAvailablity { successCallback(it) }
    }


    // set Data in view model

    private var _dataBasket: BasketScreenModelData? = null

    private var _dataBasketItemDetails: MutableList<BasketProductsDetailsModelData>? = null
    private var _dataStore: String? = "No"
    val dataBasket: BasketScreenModelData? get() = _dataBasket
    val dataBasketItemDetails: MutableList<BasketProductsDetailsModelData>? get() = _dataBasketItemDetails
    val dataStore: String? get() = _dataStore
    fun setBasketData(data: BasketScreenModelData?){
        _dataBasket=data
    }
    fun setBasketDetailsStore(data: String?){
        _dataStore=data
    }

    fun setBasketProductDetail(list:MutableList<BasketProductsDetailsModelData>?){
        _dataBasketItemDetails=list
    }








}