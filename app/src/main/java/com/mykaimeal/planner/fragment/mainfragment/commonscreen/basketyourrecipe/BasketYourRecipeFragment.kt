package com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
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
import com.mykaimeal.planner.adapter.YourRecipeAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentBasketYourRecipeBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model.BasketYourRecipeModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model.BasketYourRecipeModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.model.Dinner
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.viewmodel.BasketYourRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.CookedTabModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BasketYourRecipeFragment : Fragment(),OnItemClickListener,OnItemSelectListener {

    private lateinit var binding: FragmentBasketYourRecipeBinding
    private lateinit var basketYourRecipeViewModel: BasketYourRecipeViewModel
    private var breakfastRecipeAdapter: YourRecipeAdapter?=null
    private var brunchRecipeAdapter: YourRecipeAdapter?=null
    private var lunchRecipeAdapter: YourRecipeAdapter?=null
    private var snacksRecipeAdapter: YourRecipeAdapter?=null
    private var dinnerRecipeAdapter: YourRecipeAdapter?=null
    private var DessertRecipeAdapter: YourRecipeAdapter?=null
    private var basketYourModelData: BasketYourRecipeModelData?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
       binding= FragmentBasketYourRecipeBinding.inflate(layoutInflater, container, false)

        basketYourRecipeViewModel = ViewModelProvider(requireActivity())[BasketYourRecipeViewModel::class.java]

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

        initialize()

        return binding.root
    }

    private fun initialize() {

        basketYourRecipeViewModel.dataBasketRecipe?.let {
            showDataInUI(it)
        }?:run {
            if (BaseApplication.isOnline(requireActivity())) {
                getYourRecipeList()
            } else {
                hideData()
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }


        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

    }



    private fun getYourRecipeList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketYourRecipeViewModel.getYourRecipeUrl {
                BaseApplication.dismissMe()
                handleApiYourRecipeResponse(it)
            }
        }
    }

    private fun handleApiYourRecipeResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessYourRecipeResponse(result.data.toString())
            is NetworkResult.Error -> {
                hideData()
                showAlert(result.message, false)
            }
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessYourRecipeResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, BasketYourRecipeModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                if (apiModel.data!= null) {
                    showDataInUI(apiModel.data)
                }else{
                    hideData()
                }
            } else {
                hideData()
               handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            hideData()
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }
    private fun showDataInUI(data: BasketYourRecipeModelData?) {

        try {
            basketYourRecipeViewModel.setBasketData(data)
            basketYourModelData=data

            basketYourModelData?.let {

                fun setupMealAdapter(mealRecipes: MutableList<Dinner>?, recyclerView: RecyclerView, type: String): YourRecipeAdapter? {
                    return if (!mealRecipes.isNullOrEmpty()) {
                        val adapter = YourRecipeAdapter(mealRecipes, requireActivity(), this, type)
                        recyclerView.adapter = adapter
                        adapter
                    } else {
                        null
                    }
                }

                if (it.Breakfast !=null && it.Breakfast.size>0){
                    binding.linearBreakfast.visibility=View.VISIBLE
                    breakfastRecipeAdapter = setupMealAdapter(it.Breakfast,binding.rcyBreakfast, ErrorMessage.Breakfast)
                }else{
                    binding.linearBreakfast.visibility=View.GONE
                }

                if (it.Lunch !=null && it.Lunch.size>0){
                    binding.linearLunch.visibility=View.VISIBLE
                    lunchRecipeAdapter = setupMealAdapter(it.Lunch,binding.rcyLunch, ErrorMessage.Lunch)
                }else{
                    binding.linearLunch.visibility=View.GONE
                }


                if (it.Dinner!=null && it.Dinner.size>0){
                    binding.linearDinner.visibility=View.VISIBLE
                    dinnerRecipeAdapter = setupMealAdapter(it.Dinner,binding.rcyDinner, ErrorMessage.Dinner)
                }else{
                    binding.linearDinner.visibility=View.GONE
                }

                if (it.Snacks!=null && it.Snacks.size>0){
                    binding.linearSnacks.visibility=View.VISIBLE
                    snacksRecipeAdapter = setupMealAdapter(it.Snacks,binding.rcySnacks, ErrorMessage.Snacks)
                }else{
                    binding.linearSnacks.visibility=View.GONE
                }

                if (it.Brunch!=null && it.Brunch.size>0){
                    binding.linearBrunch.visibility=View.VISIBLE
                    brunchRecipeAdapter = setupMealAdapter(it.Brunch,binding.rcyTeaTimes, ErrorMessage.Brunch)
                }else{
                    binding.linearBrunch.visibility=View.GONE
                }

                if (it.Dessert!=null && it.Dessert.size>0){
                    binding.linearDessert.visibility=View.VISIBLE
                    DessertRecipeAdapter = setupMealAdapter(it.Dessert,binding.rcyDessert, ErrorMessage.Dessert)
                }else{
                    binding.linearDessert.visibility=View.GONE
                }

                val count = (it.Breakfast?.count() ?: 0) +
                        (it.Brunch?.count() ?: 0) +
                        (it.Lunch?.count() ?: 0) +
                        (it.Snacks?.count() ?: 0) +
                        (it.Dessert?.count() ?: 0) +
                        (it.Dinner?.count() ?: 0)

                if (count==0){
                    binding.layNoData.visibility=View.VISIBLE
                }else{
                    binding.layNoData.visibility=View.GONE
                }

            }?:run {
                hideData()
            }

        }catch (e:Exception){
            hideData()
            Log.d("Error","******"+e.message)
        }
    }

    private fun hideData(){
        binding.linearBrunch.visibility=View.GONE
        binding.linearSnacks.visibility=View.GONE
        binding.linearDinner.visibility=View.GONE
        binding.linearLunch.visibility=View.GONE
        binding.linearBreakfast.visibility=View.GONE
        binding.linearDessert.visibility=View.GONE
        binding.layNoData.visibility=View.VISIBLE
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {

    }

    private fun removeRecipeBasketDialog(item: Dinner?, adapter: YourRecipeAdapter?, type: String?, mealList: MutableList<Dinner>?, position: Int?) {
        val dialogAddItem: Dialog = context?.let { Dialog(it) }!!
        dialogAddItem.setContentView(R.layout.alert_dialog_remove_recipe_basket)
        dialogAddItem.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogAddItem.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        val tvDialogCancelBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogRemoveBtn = dialogAddItem.findViewById<TextView>(R.id.tvDialogRemoveBtn)
        dialogAddItem.show()
        dialogAddItem.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        tvDialogCancelBtn.setOnClickListener {
            dialogAddItem.dismiss()
        }
        tvDialogRemoveBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeBasketRecipeApi(item?.basket_id.toString(), dialogAddItem, adapter, type,mealList,position)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun removeBasketRecipeApi(
        id: String,
        dialogAddItem: Dialog,
        adapter: YourRecipeAdapter?,
        type: String?,
        mealList: MutableList<Dinner>?,
        position: Int?
    ) {
        BaseApplication.showMe(requireActivity())
        lifecycleScope.launch {
            basketYourRecipeViewModel.removeBasketUrlApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        val cookedModel = gson.fromJson(it.data, CookedTabModel::class.java)
                        if (cookedModel.code == 200 && cookedModel.success) {
                            // Remove item from the list
                            mealList?.removeAt(position!!)
                            // Define meal types and corresponding UI elements
                            val mealVisibilityMap = mapOf(ErrorMessage.Breakfast to binding.linearBreakfast,
                                ErrorMessage.Lunch to binding.linearLunch,
                                ErrorMessage.Dinner to binding.linearDinner,
                                ErrorMessage.Snacks to binding.linearSnacks,
                                ErrorMessage.Brunch to binding.linearBrunch,
                                ErrorMessage.Dessert to binding.linearDessert
                            )
                            // Update adapter and visibility
                            mealVisibilityMap[type]?.let { view ->
                                if (mealList?.isNotEmpty() == true) {
                                    adapter?.updateList(mealList, type.toString())
                                    view.visibility = View.VISIBLE
                                } else {
                                    view.visibility = View.GONE
                                }
                            }

                            val count = (basketYourModelData?.Breakfast?.count() ?: 0) +
                                    (basketYourModelData?.Brunch?.count() ?: 0) +
                                    (basketYourModelData?.Lunch?.count() ?: 0) +
                                    (basketYourModelData?.Snacks?.count() ?: 0) +
                                    (basketYourModelData?.Dessert?.count() ?: 0) +
                                    (basketYourModelData?.Dinner?.count() ?: 0)

                            if (count==0){
                                binding.layNoData.visibility=View.VISIBLE
                            }else{
                                binding.layNoData.visibility=View.GONE
                            }
                            // Dismiss the dialog
                            dialogAddItem.dismiss()
                            (activity as MainActivity?)?.upBasket()
                        } else {
                            handleError(cookedModel.code,cookedModel.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlert(it.message, false)
                    }
                    else -> {
                        showAlert(it.message, false)
                    }
                }
            }, id)
        }
    }


    override fun itemSelect(position: Int?, status: String?, type: String?) {
        // Map the type to the corresponding list and adapter
        val (mealList, adapter) = when (type) {
            ErrorMessage.Breakfast -> basketYourModelData?.Breakfast to breakfastRecipeAdapter
            ErrorMessage.Lunch -> basketYourModelData?.Lunch to lunchRecipeAdapter
            ErrorMessage.Dinner -> basketYourModelData?.Dinner to dinnerRecipeAdapter
            ErrorMessage.Snacks -> basketYourModelData?.Snacks to snacksRecipeAdapter
            ErrorMessage.Brunch -> basketYourModelData?.Brunch to brunchRecipeAdapter
            ErrorMessage.Dessert -> basketYourModelData?.Dessert to DessertRecipeAdapter
            else -> null to null
        }

        val item = mealList?.get(position!!)

        if (status.equals("Minus",true) || status.equals("Plus",true)){
            removeAddServing(item, position, status,adapter,type,mealList)
        }

        if (status.equals("remove",true)){
            removeRecipeBasketDialog(item, adapter, type, mealList, position)
        }

        if (status.equals("view",true)){
            val bundle = Bundle().apply {
                putString("uri", item?.uri)
                val data= item?.data?.recipe?.mealType?.get(0)?.split(",")
                val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
                putString("mealType", formattedFoodName)
                putString("recipeID", item.basket_id.toString())
                putString("serving", item.serving.toString())
                putString("statusType", "0")
            }
            findNavController().navigate(R.id.recipeDetailsFragment, bundle)
        }

    }

    private fun removeAddServing(item: Dinner?, position: Int?,
        status: String?, adapter: YourRecipeAdapter?, type: String?, mealList: MutableList<Dinner>?) {
        if (status.equals("plus",true) || status.equals("minus",true)) {
            var count = item?.serving?.toInt()
            count = when (status?.lowercase()) {
                "plus" -> count!! + 1
                "minus" -> count!! - 1
                else -> count // No change if `apiType` doesn't match
            }
            val data= item?.data?.recipe?.mealType?.get(0)?.split(",")
            val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
            increaseQuantityRecipe(item, adapter, type, mealList, position, count.toString(),formattedFoodName)
        }
    }

    private fun increaseQuantityRecipe(
        item: Dinner?,
        adapter: YourRecipeAdapter?,
        type: String?,
        mealList: MutableList<Dinner>?,
        position: Int?,
        count: String,
        formattedFoodName: String
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketYourRecipeViewModel.basketYourRecipeIncDescUrl({
                BaseApplication.dismissMe()
                handleApiQuantityResponse(it,item,adapter,type,mealList,position,count)
            },item?.uri,count,formattedFoodName)
        }
    }

    private fun handleApiQuantityResponse(
        result: NetworkResult<String>,
        item: Dinner?,
        adapter: YourRecipeAdapter?,
        type: String?,
        mealList: MutableList<Dinner>?,
        position: Int?,
        count: String
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessQuantityResponse(result.data.toString(),item,adapter,type,mealList,position,count)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessQuantityResponse(data: String, item: Dinner?, adapter: YourRecipeAdapter?, type: String?, mealList: MutableList<Dinner>?, position: Int?, count: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                // Toggle the is_like value
                item?.serving = count.toInt().toString()
                if (item != null) {
                    mealList?.set(position!!, item)
                }
                // Update the adapter
                if (mealList != null) {
                    adapter?.updateList(mealList,type.toString())
                }
                (activity as MainActivity?)?.upBasket()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }


}