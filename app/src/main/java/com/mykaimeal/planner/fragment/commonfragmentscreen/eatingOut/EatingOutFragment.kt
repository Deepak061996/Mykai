package com.mykaimeal.planner.fragment.commonfragmentscreen.eatingOut

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
import com.mykaimeal.planner.basedata.BaseApplication.alertError
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentEatingOutBinding
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModel
import com.mykaimeal.planner.fragment.commonfragmentscreen.bodyGoals.model.BodyGoalModelData
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.GetUserPreference
import com.mykaimeal.planner.fragment.commonfragmentscreen.commonModel.UpdatePreferenceSuccessfully
import com.mykaimeal.planner.fragment.commonfragmentscreen.eatingOut.viewmodel.EatingOutViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class EatingOutFragment : Fragment(),View.OnClickListener,OnItemClickListener {

    private lateinit var binding: FragmentEatingOutBinding
    private var status:String=""
    private var eatingOutSelect: String? = ""
    private var bodyGoalAdapter: BodyGoalAdapter? = null
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue:Int=0
    private lateinit var eatingOutViewModel: EatingOutViewModel
    private var eatingOutModelsData: List<BodyGoalModelData> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment
        binding = FragmentEatingOutBinding.inflate(inflater, container, false)
        eatingOutViewModel = ViewModelProvider(requireActivity())[EatingOutViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())
        val progressMax = if (sessionManagement.getCookingFor().equals("Myself",true)) 10 else 11
        binding.progressBar10.max = progressMax
        totalProgressValue = progressMax
        updateProgress(progressMax - 1)

        val isProfileScreen = sessionManagement.getCookingScreen().equals("Profile",true)
        val isOnline = isOnline(requireContext())
        binding.llBottomBtn.visibility = if (isProfileScreen) View.GONE else View.VISIBLE
        binding.rlUpdateEatingOut.visibility = if (isProfileScreen) View.VISIBLE else View.GONE
        if (isOnline) {
            if (isProfileScreen) {
                eatingOutSelectApi()
            } else {
                eatingOutViewModel.getEatingOutData()?.let {
                    eatingOutModelsData=it
                    showDataInUi(eatingOutModelsData)
                }?:eatingOutApi()
            }
        } else {
            alertError(requireContext(), ErrorMessage.networkError, false)
        }
        backButton()
        binding.tvNextBtn.isEnabled = false
        binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        binding.rlUpdateEatingOut.isEnabled = false
        binding.rlUpdateEatingOut.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        initialize()

        return binding.root
    }
    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }
    private fun eatingOutSelectApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            eatingOutViewModel.userPreferencesApi {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val bodyModel = Gson().fromJson(it.data, GetUserPreference::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data.eatingout)
                            } else {
                               handleError(bodyModel.code,bodyModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("EatingOut@@@","message"+e.message)
                        }
                    }
                    is NetworkResult.Error -> { showAlertFunction(it.message, false) }
                    else -> { showAlertFunction(it.message, false) }
                }
            }
        }
    }
    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlertFunction(message, true)
        }else{
            showAlertFunction(message, false)
        }
    }
    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar10.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }
    private fun initialize() {
        binding.imbBackEatingOut.setOnClickListener(this)
        binding.tvSkipBtn.setOnClickListener(this)
        binding.tvNextBtn.setOnClickListener(this)
        binding.rlUpdateEatingOut.setOnClickListener(this)
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
            sessionManagement.setEatingOut(eatingOutSelect.toString())
            dialogStillSkip.dismiss()
            findNavController().navigate(R.id.reasonsForTakeAwayFragment)
        }
    }
    private fun eatingOutApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            eatingOutViewModel.getEatingOut {
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val bodyModel = Gson().fromJson(it.data, BodyGoalModel::class.java)
                            if (bodyModel.code == 200 && bodyModel.success) {
                                showDataInUi(bodyModel.data)
                            } else {
                                handleError(bodyModel.code,bodyModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("EatingOut@@@","message"+e.message)
                        }
                    }
                    is NetworkResult.Error -> { showAlertFunction(it.message, false) }
                    else -> { showAlertFunction(it.message, false) }
                }
            }
        }
    }
    private fun showDataInUi(bodyModelData: List<BodyGoalModelData>) {
        try {
            eatingOutModelsData=bodyModelData
            if (eatingOutModelsData.isNotEmpty()){
                bodyGoalAdapter = BodyGoalAdapter(bodyModelData, requireActivity(), this)
                binding.rcyEatingOut.adapter = bodyGoalAdapter
            }
        }catch (e:Exception){
            Log.d("EatingOut","message"+e.message)
        }
        hideShow()
    }
    private fun showAlertFunction(message: String?, status: Boolean) {
        alertError(requireContext(), message, status)
    }
    override fun onClick(item: View?) {
        when (item!!.id) {
            R.id.imbBackEatingOut -> {
                findNavController().navigateUp()
            }
            R.id.tvSkipBtn->{
                stillSkipDialog()
            }
            R.id.tvNextBtn->{
                if (status.equals("2",true)){
                    sessionManagement.setEatingOut(eatingOutSelect.toString())
                    findNavController().navigate(R.id.reasonsForTakeAwayFragment)
                }
            }
            R.id.rlUpdateEatingOut->{
                if (status.equals("2",true)){
                    if (isOnline(requireActivity())) {
                        updateEatingOutApi()
                    } else {
                       alertError(requireContext(), ErrorMessage.networkError, false)
                    }
                }
            }
        }
    }
    private fun updateEatingOutApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            eatingOutViewModel.updateEatingOutApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val updateModel = Gson().fromJson(it.data, UpdatePreferenceSuccessfully::class.java)
                            if (updateModel.code == 200 && updateModel.success) {
                                findNavController().navigateUp()
                            } else {
                                handleError(updateModel.code,updateModel.message)
                            }
                        }catch (e:Exception){
                            Log.d("EatingOut@@@","message"+e.message)
                        }
                    }
                    is NetworkResult.Error -> { showAlertFunction(it.message, false) }
                    else -> { showAlertFunction(it.message, false) }
                }
            }, eatingOutSelect)
        }
    }
    override fun itemClick(position: Int?, status: String?, type: String?) {
        hideShow()
    }
     private fun hideShow(){
         if (eatingOutModelsData.isNotEmpty()){
             eatingOutViewModel.setEatingOutData(eatingOutModelsData.toMutableList())
         }
         val count = eatingOutModelsData.count { it.selected }?:0
         if (count == 0){
             status=""
             eatingOutSelect=""
             binding.tvNextBtn.isEnabled = false
             binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
             binding.rlUpdateEatingOut.isEnabled = false
             binding.rlUpdateEatingOut.setBackgroundResource(R.drawable.gray_btn_unselect_background)
         }else{
             val data=eatingOutModelsData.find { it.selected }
             eatingOutSelect = data?.id.toString()
             status="2"
             binding.tvNextBtn.isEnabled = true
             binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)
             binding.rlUpdateEatingOut.isEnabled = true
             binding.rlUpdateEatingOut.setBackgroundResource(R.drawable.green_fill_corner_bg)
         }
     }
}