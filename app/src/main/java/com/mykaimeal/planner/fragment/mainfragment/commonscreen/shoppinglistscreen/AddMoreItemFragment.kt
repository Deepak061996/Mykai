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
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AddMoreIngredientsAdapter
import com.mykaimeal.planner.adapter.IngredientsAdapterItem
import com.mykaimeal.planner.adapter.NewIngredientsShoppingAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentAddMoreItemBinding
import com.mykaimeal.planner.databinding.FragmentShoppingListBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.ingredientDislikes.model.DislikedIngredientsModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketyourrecipe.viewmodel.BasketYourRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.shoppinglistscreen.viewmodel.ShoppingListViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.core.graphics.drawable.toDrawable

@AndroidEntryPoint
class AddMoreItemFragment : Fragment(), OnItemSelectListener, OnItemClickListener {

    private lateinit var binding: FragmentAddMoreItemBinding

    private lateinit var adapterShoppingAdapter: AddMoreIngredientsAdapter
    private var ingredientList: MutableList<Ingredient> = mutableListOf()
    private var tvCounter: TextView? = null
    private var tvLabel: EditText?=null
    private lateinit var textListener: TextWatcher
    private lateinit var rlWriteNameHere: RelativeLayout
    private lateinit var cardViewSearchRecipe: CardView
    private lateinit var rcySearchCooked: RecyclerView
    var searchFor = ""
    private var textChangedJob: Job? = null
    private lateinit var commonWorkUtils: CommonWorkUtils
    private lateinit var shoppingListViewModel: ShoppingListViewModel
    private var popupWindow: PopupWindow? = null
    private var ingredientsAdapterItem: IngredientsAdapterItem? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddMoreItemBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        shoppingListViewModel = ViewModelProvider(requireActivity())[ShoppingListViewModel::class.java]
        commonWorkUtils = CommonWorkUtils(requireActivity())


        adapterShoppingAdapter = AddMoreIngredientsAdapter(ingredientList, requireActivity(), this)
        binding.rcvIngredients.adapter = adapterShoppingAdapter
        buttonColor()
        setListener()

        return binding.root
    }

    private fun setListener(){
        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.rlAddMore.setOnClickListener {
            addItemDialog()
        }

        binding.textCheckoutTesco.setOnClickListener {
            addToCartUrlApi()
        }

    }

    private fun addToCartUrlApi() {
        BaseApplication.showMe(requireContext())
        val foodIds = mutableListOf<String>()
        val schIds = mutableListOf<String>()
        val foodName = mutableListOf<String>()
        val statusType = mutableListOf<String>()

        foodIds.clear()
        schIds.clear()
        foodName.clear()
        statusType.clear()
        ingredientList.forEach {
            if (it.newStatus==true){
                foodIds.add(it.id.toString())
                schIds.add(it.quantity.toString())
                foodName.add(it.name.toString())
                statusType.add("3")
            }
        }
        lifecycleScope.launch {
            shoppingListViewModel.addShoppingCartUrlApi({
                BaseApplication.dismissMe()
                handleCartApiResponse(it)
            }, foodIds, schIds, foodName, statusType,"","")
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
                shoppingListViewModel.setShopingListData(null)
                findNavController().navigateUp()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun addItemDialog() {
        val context = requireContext()
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.alert_dialog_add_new_item)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        val tvDialogCancelBtn = dialog.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val imageCross = dialog.findViewById<ImageView>(R.id.imageCross)
        val imageMinus = dialog.findViewById<ImageView>(R.id.imageMinus)
        val imagePlus = dialog.findViewById<ImageView>(R.id.imagePlus)
        tvCounter = dialog.findViewById(R.id.tvCounter)
        tvLabel = dialog.findViewById(R.id.tvLabel)
        val tvDialogAddBtn = dialog.findViewById<TextView>(R.id.tvDialogAddBtn)
        rlWriteNameHere = dialog.findViewById(R.id.rlWriteNameHere)
        cardViewSearchRecipe = dialog.findViewById(R.id.cardViewSearchRecipe)
        rcySearchCooked = dialog.findViewById(R.id.rcySearchCooked)
        textListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (!searchText.equals(searchFor,true)) {
                    searchFor = searchText
                    textChangedJob?.cancel()
                    textChangedJob = lifecycleScope.launch {
                        delay(1000)
                        if (searchText.equals(searchFor,true)) {
                            searchable(searchText)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }

        tvLabel?.addTextChangedListener(textListener)

        dialog.setOnDismissListener {
            tvLabel?.removeTextChangedListener(textListener)
        }

        imageMinus.setOnClickListener {
            if (tvCounter!!.text.toString().toInt() > 1) {
                var data = tvCounter!!.text.toString().toInt()
                data-- // Decrement the value
                updateValue(data)
            } else {
                Toast.makeText(requireActivity(), ErrorMessage.servingError, Toast.LENGTH_LONG).show()
            }
        }

        imagePlus.setOnClickListener {
            if (tvCounter!!.text.toString().toInt() < 99) {
                var data = tvCounter!!.text.toString().toInt()
                data++ // Decrement the value
                updateValue(data)
            }
        }

        imageCross.setOnClickListener {
            dialog.dismiss()
        }

        tvDialogCancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        tvDialogAddBtn.setOnClickListener {
            val inputName = tvLabel?.text.toString().trim()
            val quantityText = tvCounter?.text.toString()
            val schId = quantityText.toIntOrNull()

            if (inputName.isEmpty()) {
                commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.enterIngName, false)
                return@setOnClickListener
            }


            val newIngredient = Ingredient(
                created_at = null,
                deleted_at = "",
                food_id = null,
                id = Random.nextInt(10000000, 99999999),
                market_id = "",
                name = inputName,
                price = "",
                pro_id = null,
                pro_img = null,
                pro_name = inputName,
                pro_price = "Not available",
                product_id = null,
                quantity = quantityText,
                sch_id = schId,
                status = null,
                newStatus=true,
                updated_at = null,
                user_id = null,
                null
            )

            ingredientList.let { it.add(newIngredient)
                adapterShoppingAdapter.notifyItemInserted(it.size - 1)
            }
            searchFor=""
            buttonColor()
            dialog.dismiss()

            // Optional: Add API logic here
        }

        dialog.show()


    }

    private fun buttonColor(){
        if (ingredientList.size > 0){
            binding.textCheckoutTesco.isEnabled=true
            binding.textCheckoutTesco.setBackgroundResource(R.drawable.gray_btn_select_background)
        }else{
            binding.textCheckoutTesco.isEnabled=false
            binding.textCheckoutTesco.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    private fun updateValue(value:Int) {
        tvCounter?.text = ""+value
    }

    private fun searchable(editText: String) {
//        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            shoppingListViewModel.getDislikeSearchIngredients({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        try {
                            val dietaryModel = gson.fromJson(it.data, DislikedIngredientsModel::class.java)
                            if (dietaryModel.code == 200 && dietaryModel.success) {
                                if (dietaryModel.data != null) {
                                    cardViewSearchRecipe.visibility = View.VISIBLE
                                    showDataInUi(dietaryModel.data)
                                }else{
                                    cardViewSearchRecipe.visibility = View.GONE
                                }
                            } else {
                                popupWindow?.dismiss()
                                cardViewSearchRecipe.visibility = View.GONE
                                handleError(dietaryModel.code,dietaryModel.message)
                            }
                        } catch (e: Exception) {
                            popupWindow?.dismiss()
                            cardViewSearchRecipe.visibility = View.GONE
                            Log.d("IngredientDislike@@@@", "message:--" + e.message)
                        }
                    }

                    is NetworkResult.Error -> {
                        popupWindow?.dismiss()
                        cardViewSearchRecipe.visibility = View.GONE
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        popupWindow?.dismiss()
                        cardViewSearchRecipe.visibility = View.GONE
                        showAlertFunction(it.message, false)
                    }
                }
            }, editText, "Shopping")
        }
    }

    private fun showDataInUi(searchModelData: MutableList<DislikedIngredientsModelData>?) {
        try {
            /*val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?
            val popupView: View? = inflater?.inflate(R.layout.item_select_layoutdrop, null)
            // Allows dismissing the popup when touching outside
            popupWindow?.isOutsideTouchable = true
            popupWindow = PopupWindow(popupView, rlWriteNameHere.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
            popupWindow?.showAsDropDown(rlWriteNameHere, 0, 0, Gravity.CENTER)
            val rcyData = popupView?.findViewById<RecyclerView>(R.id.rcy_data)*/
            searchModelData?.let {
                ingredientsAdapterItem = IngredientsAdapterItem(it, requireActivity(), this)
//                rcyData!!.adapter = ingredientsAdapterItem
                rcySearchCooked.adapter = ingredientsAdapterItem
            }

        } catch (e: Exception) {
            popupWindow?.dismiss()
            cardViewSearchRecipe.visibility = View.GONE
            Log.d("AddMeal", "message:--" + e.message)
        }
    }

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }
    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }
    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    @SuppressLint("NotifyDataSetChanged")
    override fun itemSelect(position: Int?, status: String?, type: String?) {

         if (type.equals("Delete")){
             ingredientList.removeAt(position!!)
             adapterShoppingAdapter.notifyDataSetChanged()
             buttonColor()
         }

        if (type.equals("add")){
            val localData=ingredientList[position!!]
            localData.sch_id=(localData.sch_id?.plus(1))
            ingredientList[position] = localData
            adapterShoppingAdapter.notifyDataSetChanged()
        }

        if (type.equals("minus")){
            val localData=ingredientList[position!!]
            if ((localData.sch_id ?: 1) != 1){
                localData.sch_id=(localData.sch_id?.minus(1))
                ingredientList[position] = localData
                adapterShoppingAdapter.notifyDataSetChanged()
            }
        }

    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        searchFor=status.toString().trim()
        textChangedJob?.cancel()
        popupWindow?.dismiss()
        cardViewSearchRecipe.visibility = View.GONE
        // Set text
        tvLabel?.setText(status.toString().trim())
        // Move cursor to the end
        tvLabel?.text?.let { tvLabel?.setSelection(it.length) }
        // Hide the keyboard
        val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(tvLabel?.windowToken, 0)
    }




}