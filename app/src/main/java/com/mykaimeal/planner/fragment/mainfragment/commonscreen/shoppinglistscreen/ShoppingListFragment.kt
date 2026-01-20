package com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.BasketYourRecipeAdapter
import com.mykaimeal.planner.adapter.IngredientsAdapterItem
import com.mykaimeal.planner.adapter.IngredientsShoppingAdapter
import com.mykaimeal.planner.adapter.NewIngredientsShoppingAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentShoppingListBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Recipes
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.viewmodel.BasketYourRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.model.ShoppingListModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.model.ShoppingListModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.viewmodel.ShoppingListViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@AndroidEntryPoint
class ShoppingListFragment : Fragment(), OnItemSelectListener {
    private lateinit var binding: FragmentShoppingListBinding
    private lateinit var shoppingListViewModel: ShoppingListViewModel
//    private lateinit var adapterShoppingAdapter: IngredientsShoppingAdapter
    private lateinit var adapterShoppingAdapter: NewIngredientsShoppingAdapter
    private var adapterRecipe: BasketYourRecipeAdapter? = null
    private var tvCounter: TextView? = null
    private var recipe: MutableList<Recipes> = mutableListOf()
    private var ingredientList: MutableList<Ingredient> = mutableListOf()
    private lateinit var commonWorkUtils: CommonWorkUtils
    private var textChangedJob: Job? = null
    private var popupWindow: PopupWindow? = null
    private var tvLabel:EditText?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentShoppingListBinding.inflate(layoutInflater, container, false)

        shoppingListViewModel = ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]
        commonWorkUtils = CommonWorkUtils(requireActivity())

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        adapterRecipe = BasketYourRecipeAdapter(recipe, requireActivity(), this)
        binding.rcvYourRecipes.adapter = adapterRecipe

        adapterShoppingAdapter = NewIngredientsShoppingAdapter(ingredientList, requireActivity(), this)
        binding.rcvIngredients.adapter = adapterShoppingAdapter

        backButton()

        initialize()

        shoppingListViewModel.dataShopingList?.let {
            showDataShoppingUI(it)
        }?:run {
            lunchApi()
        }


        return binding.root
    }

    private fun lunchApi(){
        if (BaseApplication.isOnline(requireContext())) {
            getShoppingList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateValue(value:Int) {
        tvCounter!!.text = ""+value
    }

    private fun initialize() {

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.textSeeAll2.setOnClickListener {
            ViewModelProvider(requireActivity())[BasketYourRecipeViewModel::class.java].setBasketData(null)
            findNavController().navigate(R.id.basketYourRecipeFragment)
        }


        binding.rlAddMore.setOnClickListener {
            findNavController().navigate(R.id.addMoreItemFragment)
        }

        binding.textCheckoutTesco.setOnClickListener{
            if (BaseApplication.isOnline(requireContext())) {
                addToCartUrlApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    private fun buttonColor(){
        if (ingredientList.size >0){
            binding.textCheckoutTesco.isEnabled=true
            binding.textCheckoutTesco.setBackgroundResource(R.drawable.gray_btn_select_background)
        }else{
            binding.textCheckoutTesco.isEnabled=false
            binding.textCheckoutTesco.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }

    private fun addToCartUrlApi() {
        BaseApplication.showMe(requireContext())
         val ids = mutableListOf<String>()
        ids.clear()
         ingredientList.forEach {
                if (it.status==1){
                    ids.add(it.id.toString())
                }
            }
        lifecycleScope.launch {
            shoppingListViewModel.addShoppingCheckApi({
                BaseApplication.dismissMe()
                handleCartApiResponse(it)
            }, ids)
        }
    }

    private fun handleCartApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCartResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCartResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                if (ingredientList.size > 0) {
                    ingredientList.sortBy { it.status==1 }
                    adapterShoppingAdapter.updateList(ingredientList)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun getShoppingList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.getShoppingListUrl {
                BaseApplication.dismissMe()
                handleApiShoppingListResponse(it)
            }
        }
    }

    private fun handleApiShoppingListResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessShoppingResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessShoppingResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ShoppingListModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                apiModel.data?.let {
                    showDataShoppingUI(apiModel.data)
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showDataShoppingUI(data: ShoppingListModelData) {

        shoppingListViewModel.setShopingListData(data)
        recipe.clear()
        ingredientList.clear()
        data.recipe?.let {
            recipe.addAll(it)
        }
        data.ingredient?.let {
            ingredientList.addAll(it)
        }

        if (recipe.size > 0) {
            binding.rlYourRecipes.visibility = View.VISIBLE
            adapterRecipe?.updateList(recipe)
        } else {
            binding.rlYourRecipes.visibility = View.GONE
        }

        if (ingredientList.size > 0) {
            ingredientList.sortBy { it.status==1 }
            adapterShoppingAdapter.updateList(ingredientList)
        }

        buttonColor()
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {

        if (BaseApplication.isOnline(requireActivity())) {
            if (type.equals("YourRecipe",true)) {
                if (status.equals("view",true)){
                    val bundle = Bundle().apply {
                        putString("uri", recipe[position!!].uri)
                        val data= recipe[position].data?.recipe?.mealType?.get(0)?.split(",")
                        val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
                        putString("mealType", formattedFoodName)
                        putString("recipeID", recipe[position].id.toString())
                        putString("serving", recipe[position].serving.toString())
                        putString("statusType", "0")
                    }
                    findNavController().navigate(R.id.recipeDetailsFragment, bundle)
                }else{
                    if (status.equals("remove",true)){
                        val data=recipe[position!!]
                        removeRecipeBasketDialog(data.id.toString(), position)
                    }else{
                        removeAddRecipeServing(position, status.toString())
                    }
                }
            } else  {
                if (type.equals("IngredientsClick",true)){
                    val dataChange=ingredientList[position!!]
                    if (dataChange.status==0){
                        dataChange.status=1
                    }else{
                        dataChange.status=0
                    }
                    ingredientList[position] = dataChange
                    adapterShoppingAdapter.updateList(ingredientList)
                }else{
                    val data=ingredientList[position!!]
                    if (data.newStatus==true){
                        ingredientList.removeAt(position)
                        if (ingredientList.size>0){
                            adapterShoppingAdapter.updateList(ingredientList)
                        }
                        buttonColor()
                    }else{
                        removeAddIngServing(position, status.toString())
                    }
                }
            }
        }else{
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun removeAddIngServing(position: Int?, type: String) {
        val item = position?.let { ingredientList.get(it) }
        val foodId = item?.food_id
        val qty: String
        if (type.equals("plus", true) || type.equals("minus", true)) {
            var count = item?.sch_id
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            qty= count.toString()
        } else {
            qty="0"
        }
        increaseIngRecipe(foodId, qty, item, position)
    }

    private fun increaseIngRecipe(
        foodId: String?,
        quantity: String,
        item: Ingredient?,
        position: Int?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.basketIngIncDescUrl({
                BaseApplication.dismissMe()
                handleApiIngResponse(it, item, quantity, position)
            }, foodId, quantity)
        }
    }

    private fun handleApiIngResponse(
        result: NetworkResult<String>,
        item: Ingredient?,
        quantity: String,
        position: Int?
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessIngResponse(result.data.toString(), item, quantity, position)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessIngResponse(data: String, item: Ingredient?, quantity: String, position: Int?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (!quantity.equals("0",true)) {
                    // Toggle the is_like value
                    item?.sch_id = quantity.toInt()
                    if (item != null) {
                        ingredientList[position!!] = item
                    }
                }else{
                    ingredientList.removeAt(position!!)
                }
                if (ingredientList.size>0){
                    adapterShoppingAdapter.updateList(ingredientList)
                    binding.rcvIngredients.visibility=View.VISIBLE
                }else{
                    binding.rcvIngredients.visibility=View.GONE
                }
                (activity as MainActivity?)?.upBasket()
                Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun removeAddRecipeServing(position: Int?, type: String) {
        val item = position?.let { recipe[it] }
        if (type.equals("plus", true) || type.equals("minus", true)) {
            var count = item?.serving?.toInt()
            val uri = item?.uri
            count = when (type.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            val data= item?.data?.recipe?.mealType?.get(0)?.split(",")
            val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
            increaseQuantityRecipe(uri, count.toString(), item, position,formattedFoodName)
        }
    }

    private fun increaseQuantityRecipe(
        uri: String?,
        quantity: String,
        item: Recipes?,
        position: Int?,
        formattedFoodName: String
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.basketYourRecipeIncDescUrl({
                BaseApplication.dismissMe()
                handleApiQuantityResponse(it, item, quantity, position)
            }, uri, quantity,formattedFoodName)
        }
    }

    private fun handleApiQuantityResponse(
        result: NetworkResult<String>,
        item: Recipes?,
        quantity: String,
        position: Int?
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessQuantityResponse(result.data.toString(), item, quantity, position)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessQuantityResponse(data: String, item: Recipes?, quantity: String, position: Int?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item?.serving = quantity.toInt().toString()
                recipe[position!!] = item!!
                // Update the adapter
                adapterRecipe?.updateList(recipe)
                (activity as MainActivity?)?.upBasket()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun removeRecipeBasketDialog(recipeId: String?, position: Int?) {
        val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
        dialogAddItem.setContentView(R.layout.alert_dialog_remove_recipe_basket)
        dialogAddItem.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddItem.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        val tvDialogCancelBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogAddItem.show()
        dialogAddItem.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn.setOnClickListener {
            dialogAddItem.dismiss()
        }

        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeBasketRecipeApi(recipeId.toString(), dialogAddItem, position)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun removeBasketRecipeApi(recipeId: String, dialogRemoveDay: Dialog, position: Int?) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.removeBasketUrlApi({
                BaseApplication.dismissMe()
                handleApiRemoveBasketResponse(it, position, dialogRemoveDay)
            }, recipeId)
        }
    }

    private fun handleApiRemoveBasketResponse(
        result: NetworkResult<String>,
        position: Int?,
        dialogRemoveDay: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessRemoveBasketResponse(result.data.toString(), position, dialogRemoveDay)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessRemoveBasketResponse(data: String, position: Int?, dialogRemoveDay: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogRemoveDay.dismiss()
                recipe.removeAt(position!!)
                // Update the adapter
                if (recipe.size > 0) {
                    binding.rlYourRecipes.visibility = View.VISIBLE
                    adapterRecipe?.updateList(recipe)
                } else {
                    binding.rlYourRecipes.visibility = View.GONE
                }
                (activity as MainActivity?)?.upBasket()
                Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_LONG).show()
                buttonColor()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        shoppingListViewModel.setShopingListData(null)
    }

}