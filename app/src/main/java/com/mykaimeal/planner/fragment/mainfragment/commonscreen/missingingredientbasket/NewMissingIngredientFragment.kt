package com.mykaimeal.planner.fragment.mainfragment.commonscreen.missingingredientbasket

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.MissingIngredientAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentNewMissingIngredientBinding
import com.mykaimeal.planner.di.CustomDatePickerIngredientView
import com.mykaimeal.planner.di.CustomDatePickerView
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.viewmodel.BasketScreenViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate


@RequiresApi(Build.VERSION_CODES.O)
class NewMissingIngredientFragment : Fragment(), OnItemSelectListener {


    lateinit var binding: FragmentNewMissingIngredientBinding
    private lateinit var basketScreenViewModel: BasketScreenViewModel
    private var ingredientList: MutableList<Ingredient> = mutableListOf()
    private  var adapterIngredients: MissingIngredientAdapter? = null
    private var clickStatus :Boolean= false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNewMissingIngredientBinding.inflate(inflater, container, false)
        basketScreenViewModel = ViewModelProvider(requireActivity())[BasketScreenViewModel::class.java]

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        adapterIngredients = MissingIngredientAdapter(ingredientList, requireActivity(),this)
        binding.rcvIngredients.adapter = adapterIngredients

        if (basketScreenViewModel.dataBasket!=null){
            showDataInUI()
        }

        setListener()

        return binding.root
    }

    private fun showDataInUI() {

        ingredientList.clear()

        basketScreenViewModel.dataBasket?.ingredient?.let {
            ingredientList.addAll(it)
        }

        if (ingredientList.isNotEmpty()) {
            ingredientList.removeIf { !it.newStatus }
            ingredientList.forEachIndexed { index, ingredient ->
                ingredient.newStatus=false
                ingredientList[index] = ingredient
            }
            binding.rcvIngredients.visibility = View.VISIBLE
            adapterIngredients?.updateList(ingredientList)
        } else {
            binding.rcvIngredients.visibility = View.GONE
        }


    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListener(){

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSelectAllBtn.setOnClickListener {
            if (ingredientList.isNotEmpty()){
                clickStatus = !clickStatus
                ingredientList.forEachIndexed { index, ingredient ->
                    ingredient.newStatus = clickStatus
                    ingredientList[index] = ingredient
                }
                clickStatus = ingredientList.all { it.newStatus } == true

                val drawableRes = if (clickStatus) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
                val textSmsm = if (clickStatus) "Add to Basket" else "I have purchased everything"
                binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0,drawableRes, 0)
                binding.tvAddToBasket.text = textSmsm
                adapterIngredients?.updateList(ingredientList)
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })


        binding.etIngDislikesSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapterIngredients?.filter(s.toString())
                if (adapterIngredients?.itemCount == 0) {
                    binding.rcvIngredients.visibility = View.GONE
                    binding.tvNoData.visibility = View.VISIBLE
                } else {
                    binding.rcvIngredients.visibility = View.VISIBLE
                    binding.tvNoData.visibility = View.GONE
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.llBasketPurchasedBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                storeApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun storeApi(){
         val countSelect=ingredientList.count { !it.newStatus }
         val list :MutableList<String> = mutableListOf()
         list.clear()
          if (countSelect==0){
             alertSaveBox()
          }else{
              ingredientList.forEach {
                  if (!it.newStatus){
                      list.add(it.id.toString())
                  }
              }
              BaseApplication.showMe(requireContext())
              lifecycleScope.launch {
                  basketScreenViewModel.addPurchasedElementUrlApi(
                      {
                          BaseApplication.dismissMe()
                          handleApiSaveResponse(it)
                      },list)
              }
         }
    }

    private fun handleApiSaveResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleApiSaveResponse(result.data.toString(), null)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }
    private fun handleApiStoreSaveResponse(result: NetworkResult<String>, dialog: Dialog) {
        when (result) {
            is NetworkResult.Success -> handleApiSaveResponse(result.data.toString(),dialog)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun handleApiSaveResponse(data: String, dialog: Dialog?) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (dialog!=null){
                    dialog.dismiss()
                    (activity as MainActivity?)?.upDateHomeData()
                    findNavController().navigate(R.id.homeFragment)
                }else{
                    alertSaveBox()
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
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


    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun alertSaveBox(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.alert_save_track_popup)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)
        // Set width and height
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dialog.findViewById<TextView>(R.id.tvDialogCancelBtn).setOnClickListener {
            dialog.dismiss()
            (activity as MainActivity?)?.upDateHomeData()
            findNavController().navigate(R.id.homeFragment)
        }

        dialog.findViewById<TextView>(R.id.tvDialogLogoutBtn)?.setOnClickListener {
            dialog.dismiss()
            shoppingIngredientBox()
        }

        dialog.show()
    }

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun shoppingIngredientBox(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.alert_sopping_popup)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // Set width and height
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val llLogoutBtn =  dialog.findViewById<LinearLayout>(R.id.llLogoutBtn)
        val relDateOfBirthType =  dialog.findViewById<RelativeLayout>(R.id.relDateOfBirthType)
        val edCardTotal =  dialog.findViewById<EditText>(R.id.edCardTotal)
        val edStoreName =  dialog.findViewById<EditText>(R.id.edStoreName)
        val etDateOfBirth =  dialog.findViewById<TextView>(R.id.etDateOfBirth)

        fun validation() : Boolean{
            if (edStoreName.text.toString().trim().isEmpty()){
                Toast.makeText(requireContext(),ErrorMessage.storeError,Toast.LENGTH_SHORT).show()
                return false
            }else if (edCardTotal.text.toString().trim().isEmpty()){
                Toast.makeText(requireContext(),ErrorMessage.cardTotalError,Toast.LENGTH_SHORT).show()
                return false
            }else if (etDateOfBirth.text.toString().trim().equals("mm/dd/yyyy",true)){
                Toast.makeText(requireContext(),ErrorMessage.purchasedError,Toast.LENGTH_SHORT).show()
                return false
            }
            return true
        }


        llLogoutBtn?.setOnClickListener {
            if (validation()){
                BaseApplication.showMe(requireContext())
                lifecycleScope.launch {
                    basketScreenViewModel.addGraphDataApi(
                        {
                            BaseApplication.dismissMe()
                            handleApiStoreSaveResponse(it,dialog)
                        },edStoreName.text.toString(),edCardTotal.text.toString(),BaseApplication.convertPurchaseDateFormat(etDateOfBirth.text.toString()) )
                }

            }

        }
        relDateOfBirthType?.setOnClickListener {
            val dialogWeight = Dialog(requireContext(), R.style.BottomSheetDialog)
            dialogWeight.setContentView(R.layout.alert_dialog_date)
            dialogWeight.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialogWeight.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
            dialogWeight.window?.setGravity(Gravity.BOTTOM)
            val btnSave: TextView? = dialogWeight.findViewById(R.id.btnDone)
            val weekdayTimePickerView: CustomDatePickerIngredientView? = dialogWeight.findViewById(R.id.weekday_time_picker_view)

            lifecycleScope.launch {
                delay(1000L)
                try {
                    if (!dialog.findViewById<TextView>(R.id.etDateOfBirth).text.toString().equals("mm/dd/yyyy",true)){
                        val dataDate= dialog.findViewById<TextView>(R.id.etDateOfBirth).text.toString().split("/")
                        val year = dataDate[2].toInt()
                        val month = dataDate[0].toInt() -1
                        val day = dataDate[1].toInt()
                        weekdayTimePickerView?.setSelectedDate(year,month,day)
                    }
                }catch (e:Exception){
                    val currentDate = LocalDate.now()
                    val year = currentDate.year
                    val month = currentDate.monthValue // 1 to 12
                    val day = currentDate.dayOfMonth
                    weekdayTimePickerView?.setSelectedDate(year,month,day)
                }
            }

            btnSave?.setOnClickListener {
                if (weekdayTimePickerView!=null) {
                    dialogWeight.dismiss()
                    val (day, month, year) = weekdayTimePickerView.getSelectedDateParts()
                    Log.d("DATE_PICKER", "Selected: $month/$day/$year")
                    Log.d("Selected Date ", "*****$month/$day/$year")
                    val dateLocal="$month/$day/$year"
                    etDateOfBirth?.text =BaseApplication.convertDateFormat(dateLocal)
                }
            }


            dialogWeight.show()
        }
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun itemSelect(position: Int?, status: String?, type: String?) {
        val pos = ingredientList.indexOfFirst { it.id == status?.toInt() }
        val dataList= ingredientList[pos]
        dataList.newStatus = !dataList.newStatus
        ingredientList[pos] = dataList
        adapterIngredients?.notifyDataSetChanged()

        val count = ingredientList.count { it.newStatus }

        val textSms  =   if (count > 0){
             "Add to Basket"
        } else{
            "I have purchased everything"
        }
        clickStatus = ingredientList.all { it.newStatus } == true
        // Update the drawable based on the selectAll state
        val drawableRes = if (clickStatus) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
        binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0,drawableRes, 0)
        binding.tvAddToBasket.text = textSms

    }



}