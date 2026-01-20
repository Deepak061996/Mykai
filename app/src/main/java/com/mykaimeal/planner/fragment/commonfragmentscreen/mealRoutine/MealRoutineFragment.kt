package com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine
import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.MealRoutineAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentMealRoutineBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.model.MealRoutineModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.mealRoutine.viewmodel.MealRoutineViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MealRoutineFragment : Fragment(), View.OnClickListener, OnItemClickedListener {

    private lateinit var binding: FragmentMealRoutineBinding
    private lateinit var sessionManagement: SessionManagement
    private var mealRoutineAdapter: MealRoutineAdapter? = null
    private var totalProgressValue: Int = 0
    private var status: String? = ""
    private var mealRoutineSelectedId = mutableListOf<String>()
    private lateinit var mealRoutineViewModel: MealRoutineViewModel
    private var mealRoutineModelData: MutableList<MealRoutineModelData> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentMealRoutineBinding.inflate(inflater, container, false)
        mealRoutineViewModel = ViewModelProvider(requireActivity())[MealRoutineViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())
        val cookingFor = sessionManagement.getCookingFor()
        val progressValue: Int
        val maxProgress: Int
        val mealRoutineDesc: String
        binding.textAllergensIng.visibility = View.GONE
        binding.textAllergensIngPartner.visibility = View.GONE
        binding.textAllergensIngFamily.visibility = View.GONE
        when (cookingFor) {
            "Myself" -> {
                binding.textAllergensIng.visibility = View.VISIBLE
                mealRoutineDesc = getString(R.string.meal_routine_desc)
                maxProgress = 10
                progressValue = 6
            }
            "MyPartner" -> {
                binding.textAllergensIngPartner.visibility = View.VISIBLE
                mealRoutineDesc = "Which days do you guys normally meal prep or cook on?\n"
                maxProgress = 11
                progressValue = 7
            }
            else -> {
                binding.textAllergensIngFamily.visibility = View.VISIBLE
                mealRoutineDesc = "What meals do you typically cook for your family?"
                maxProgress = 11
                progressValue = 7
            }
        }
        binding.tvMealRoutineDesc.text = mealRoutineDesc
        binding.progressBar6.max = maxProgress
        totalProgressValue = maxProgress
        updateProgress(progressValue)
        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        val isOnline = isOnline(requireContext())
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateMealRoutine.visibility = if (isProfileScreen) View.VISIBLE else View.GONE
        if (isOnline) {
            if (isProfileScreen) {
                mealRoutineSelectApi()
            } else {
                mealRoutineViewModel.getMealRoutineData()?.let {
                    mealRoutineModelData =it
                    showDataInUi(mealRoutineModelData)
                }?: mealRoutineApi()
            }
        }else {
            alertError(requireContext(), ErrorMessage.networkError, false)
        }
        backButton()
        initialize()
        return binding.root
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

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar6.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }
    private fun initialize() {
        binding.tvSkipBtn.setOnClickListener(this)
        binding.imgBackMealRoutine.setOnClickListener(this)
        binding.tvNextBtn.setOnClickListener(this)
        binding.rlUpdateMealRoutine.setOnClickListener {
            if (status.equals("2",true)){
                if (isOnline(requireContext())) {
                    mealRoutineSelectedId.clear()
                    mealRoutineModelData.forEach {
                        if (it.selected){
                            mealRoutineSelectedId.add(it.id.toString())
                        }
                    }
                    if (mealRoutineSelectedId.isNotEmpty()){
                        updateMealRoutineApi()
                    }else{
                        alertError(requireContext(), ErrorMessage.mealTypetError, false)
                    }
                } else {
                    alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }
    private fun stillSkipDialog() {
        val dialogStillSkip: Dialog = context?.let { Dialog(it) }!!
        dialogStillSkip.setContentView(R.layout.alert_dialog_still_skip)
        dialogStillSkip.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogStillSkip.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogSkipBtn = dialogStillSkip.findViewById<TextView>(R.id.tvDialogSkipBtn)
        dialogStillSkip.show()
        dialogStillSkip.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        tvDialogCancelBtn.setOnClickListener {
            dialogStillSkip.dismiss()
        }
        tvDialogSkipBtn.setOnClickListener {
            sessionManagement.setMealRoutineList(null)
            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.cookingFrequencyFragment)
        }
    }
    private fun mealRoutineApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            mealRoutineViewModel.getMealRoutine {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val mealRoutineModel = Gson().fromJson(it.data, MealRoutineModel::class.java)
                            if (mealRoutineModel.code == 200 && mealRoutineModel.success) {
                                mealRoutineModel.data?.let { list->
                                    showDataInUi(list)
                                }

                            } else {
                                if (mealRoutineModel.code == ErrorMessage.code) {
                                    showAlertFunction(mealRoutineModel.message, true)
                                } else {
                                    showAlertFunction(mealRoutineModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("MealRoutine@@@","message:---"+e.message)
                        }
                    }
                    is NetworkResult.Error -> { showAlertFunction(it.message, false) }
                    else -> { showAlertFunction(it.message, false) }
                }
            }
        }
    }

    private fun mealRoutineSelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            mealRoutineViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val bodyModel = Gson().fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                bodyModel.data.mealroutine?.let { list->
                                    showDataInUi(list)
                                }
                            } else {
                                handleError(bodyModel.code,bodyModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("MealRoutine@@","message:---"+e.message)
                        }
                    }
                    is NetworkResult.Error -> { showAlertFunction(it.message, false) }
                    else -> { showAlertFunction(it.message, false) }
                }
            }
        }
    }

    private fun showDataInUi(mealRoutineModelsData: MutableList<MealRoutineModelData>) {
        try {
            if (mealRoutineModelsData.isNotEmpty()) {
                // Check if list is null
                if (mealRoutineViewModel.getMealRoutineData() == null) {
                    // By default, assume "Select All" should be false
                    var isAllSelected = false
                    // Check if all items are selected
                    if (mealRoutineModelsData.isNotEmpty() && mealRoutineModelsData.all { it.selected }) {
                        isAllSelected = true
                    }
                    // Add "Select All" at the first position with proper selection status
                    mealRoutineModelsData.add(0, MealRoutineModelData(id = -1, "Select All", selected = isAllSelected))
                }
                // Set the data and adapter
                mealRoutineModelData = mealRoutineModelsData
                mealRoutineAdapter = MealRoutineAdapter(mealRoutineModelsData, requireActivity(), this)
                binding.rcyMealRoutine.adapter = mealRoutineAdapter

            }
        }catch (e:Exception){
            Log.d("MealRoutine","message:---"+e.message)
        }
        hideShow()
    }
    private fun showAlertFunction(message: String?, status: Boolean) {
        alertError(requireContext(), message, status)
    }
    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.imgBackMealRoutine -> {
                findNavController().navigateUp()
            }
            R.id.tvSkipBtn -> {
                stillSkipDialog()
            }
            R.id.tvNextBtn -> {
                if (status.equals("2")) {
                    mealRoutineSelectedId.clear()
                    mealRoutineModelData.forEach {
                        if (it.selected){
                            mealRoutineSelectedId.add(it.id.toString())
                        }
                    }
                    sessionManagement.setMealRoutineList(mealRoutineSelectedId)
                    findNavController().navigate(R.id.cookingFrequencyFragment)
                }
            }
        }
    }
    private fun updateMealRoutineApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            mealRoutineViewModel.updateMealRoutineApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val updateModel = gson.fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                (activity as MainActivity?)?.upDatePlan()
                                findNavController().navigateUp()
                            } else {
                                handleError(updateModel.code,updateModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("MealRoutine@@@","message:---"+e.message)
                        }
                    }
                    is NetworkResult.Error -> { showAlertFunction(it.message, false) }
                    else -> { showAlertFunction(it.message, false) }
                }
            }, mealRoutineSelectedId)
        }
    }
    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            showAlertFunction(message, false)
        }
    }
    override fun itemClicked(position: Int?, list: MutableList<String>?, status: String?, type: String?) {
        hideShow()
    }
    private fun hideShow() {
        if (mealRoutineModelData.isNotEmpty()){
            mealRoutineViewModel.setMealRoutineData(mealRoutineModelData)
        }
        val count = mealRoutineModelData.count { it.selected }
        if (count == 0) {
            status = ""
            binding.tvNextBtn.isClickable = false
            binding.rlUpdateMealRoutine.isClickable = false
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateMealRoutine.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        } else {
            status = "2"
            binding.tvNextBtn.isClickable = true
            binding.rlUpdateMealRoutine.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateMealRoutine.setBackgroundResource(R.drawable.green_fill_corner_bg)
        }
    }
}