package com.mykaimeal.planner.fragment.commonfragmentscreen.cookingFrequency

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
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.BodyGoalAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentCookingFrequencyBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.cookingFrequency.viewmodel.CookingFrequencyViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CookingFrequencyFragment : Fragment(), OnItemClickListener {

    private lateinit var binding: FragmentCookingFrequencyBinding
    private var bodyGoalAdapter: BodyGoalAdapter? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue: Int = 0
    private var status: String? = null
    private var cookingSelect: String? = null
    private lateinit var cookingFrequencyViewModel: CookingFrequencyViewModel
    private var cookingFreqModelData: List<BodyGoalModelData> = mutableListOf()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentCookingFrequencyBinding.inflate(inflater, container, false)
        cookingFrequencyViewModel = ViewModelProvider(requireActivity())[CookingFrequencyViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())
        val cookingFor = sessionManagement.getCookingFor()
        var progressValue = 8
        var maxProgress = 11
        if (cookingFor.equals("Myself",true)) {
            maxProgress = 10
            progressValue = 7
        }
        binding.tvCookFreqDesc.text = if (cookingFor.equals("Myself",true) || cookingFor.equals("MyPartner",true)) "How often do you cook meals at home?" else "How often do you cook meals for your family?"
        binding.progressBar7.max = maxProgress
        totalProgressValue = maxProgress
        updateProgress(progressValue)
        if (sessionManagement.getCookingScreen().equals("Profile",true)) {
            binding.llBottomBtn.visibility = View.GONE
            binding.rlUpdateCookingFrequency.visibility = View.VISIBLE
            if (BaseApplication.isOnline(requireActivity())) {
                cookingFrequencySelectApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        } else {
            binding.llBottomBtn.visibility = View.VISIBLE
            binding.rlUpdateCookingFrequency.visibility = View.GONE
            cookingFrequencyViewModel.getCookingFreqData()?.let {
                cookingFreqModelData = it
                showDataInUi(cookingFreqModelData)
            }?: kotlin.run {
                // checking the device of mobile data in online and offline(show network error message)
                if (BaseApplication.isOnline(requireActivity())) {
                    cookingFrequencyApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
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
        binding.progressBar7.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }
    private fun initialize() {
        binding.imgBackCookingFreq.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvSkipBtn.setOnClickListener {
            stillSkipDialog()
        }
        binding.tvNextBtn.setOnClickListener {
            if (status == "2") {
                sessionManagement.setCookingFrequency(cookingSelect.toString())
                if (sessionManagement.getCookingFor().equals("Myself",true)) {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                } else if (sessionManagement.getCookingFor().equals("MyPartner",true)) {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                } else {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                }
            }
        }
        binding.rlUpdateCookingFrequency.setOnClickListener {
            if (status.equals("2",true)){
                //checking the device of mobile data in online and offline(show network error message)
                if (BaseApplication.isOnline(requireActivity())) {
                    updateCookFrequencyApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }
    private fun updateCookFrequencyApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookingFrequencyViewModel.updateCookingFrequencyApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val updateModel = Gson().fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                findNavController().navigateUp()
                            } else {
                                if (updateModel.code == ErrorMessage.code) {
                                    showAlertFunction(updateModel.message, true)
                                } else {
                                    showAlertFunction(updateModel.message, false)
                                }
                            }
                        }catch (e:Exception){
                            Log.d("cookingFrequency@@@@", "message" + e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, cookingSelect.toString())
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
            dialogStillSkip.dismiss()
            sessionManagement.setCookingFrequency("")
            if (sessionManagement.getCookingFor().equals("Myself",true)) {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            } else if (sessionManagement.getCookingFor().equals("MyPartner",true)) {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            } else {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            }
        }
    }

    private fun cookingFrequencySelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookingFrequencyViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val bodyModel = Gson().fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data.cookingfrequency)
                            } else {
                                if (bodyModel.code == ErrorMessage.code) {
                                    showAlertFunction(bodyModel.message, true)
                                } else {
                                    showAlertFunction(bodyModel.message, false)
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("cookingFrequency@@@", "message" + e.message)
                        }
                    }
                    is NetworkResult.Error -> { showAlertFunction(it.message, false) }
                    else -> { showAlertFunction(it.message, false) }
                }
            }
        }
    }
    private fun cookingFrequencyApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            cookingFrequencyViewModel.getCookingFrequency {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val bodyGoalModel = Gson().fromJson(it.data, BodyGoalModel::class.java)
                            if (bodyGoalModel.code == 200 && bodyGoalModel.success) {
                                showDataInUi(bodyGoalModel.data)
                            } else {
                                handleError(bodyGoalModel.code,bodyGoalModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("cookingFrequency@@@@", "message" + e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }
        }
    }
    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        } else {
            showAlertFunction(message, false)
        }
    }
    private fun showDataInUi(bodyGoalModelData: List<BodyGoalModelData>) {
        try {
            cookingFreqModelData = bodyGoalModelData
            if (cookingFreqModelData.isNotEmpty()) {
                bodyGoalAdapter = BodyGoalAdapter(bodyGoalModelData, requireActivity(), this)
                binding.rcyCookingFreq.adapter = bodyGoalAdapter
            }
        } catch (e: Exception) {
            Log.d("cookingFrequency@@@@", "message" + e.message)
        }
        hideShow()
    }
    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }
    override fun itemClick(selectItem: Int?, status: String?, type: String?) {
        hideShow()
    }
    private fun hideShow(){
        if (cookingFreqModelData.isNotEmpty()){
            cookingFrequencyViewModel.setCookingFreqData(cookingFreqModelData.toMutableList())
        }
        val count = cookingFreqModelData.count { it.selected } ?:0
        if (count == 0){
            status=""
            cookingSelect=""
            binding.tvNextBtn.isEnabled = false
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.rlUpdateCookingFrequency.isEnabled = false
            binding.rlUpdateCookingFrequency.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }else{
            val data= cookingFreqModelData.find { it.selected }
            status="2"
            cookingSelect=data?.id.toString()
            binding.tvNextBtn.isEnabled = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
            binding.rlUpdateCookingFrequency.isEnabled = true
            binding.rlUpdateCookingFrequency.setBackgroundResource(R.drawable.green_fill_corner_bg)
        }
    }
}