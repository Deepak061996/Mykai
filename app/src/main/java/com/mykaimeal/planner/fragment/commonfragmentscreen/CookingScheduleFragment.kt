package com.mykaimeal.planner.fragment.commonfragmentscreen

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentCookingScheduleBinding

class CookingScheduleFragment : Fragment(),OnItemClickListener {

    private lateinit var binding: FragmentCookingScheduleBinding
    private lateinit var sessionManagement: SessionManagement
    private var totalProgressValue:Int=0
    private var statusType:String?=null


    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCookingScheduleBinding.inflate(inflater, container, false)

        sessionManagement = SessionManagement(requireContext())
        if (sessionManagement.getCookingFor().equals("Myself",true)){
            binding.textCookingSch.visibility=View.VISIBLE
            binding.textCookingFamilySch.visibility=View.GONE
            binding.tvCookingSchDesc.text="Select the days you usually cook or prep meals"
            binding.progressBar8.max=10
            totalProgressValue=10
            updateProgress(8)
        } else if (sessionManagement.getCookingFor().equals("MyPartner",true)){
            binding.textCookingSch.visibility=View.VISIBLE
            binding.textCookingFamilySch.visibility=View.GONE
            binding.tvCookingSchDesc.text="Select the days you usually cook or prep meals"
            binding.progressBar8.max=11
            totalProgressValue=11
            updateProgress(7)
        } else {
            binding.textCookingSch.visibility=View.GONE
            binding.textCookingFamilySch.visibility=View.VISIBLE
            binding.tvCookingSchDesc.text="Which days do you normally meal prep or cook for your family?"
            binding.progressBar8.max=11
            totalProgressValue=11
            updateProgress(7)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })

//        cookingScheduleModel()
        initialize()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar8.progress = progress
        binding.tvProgressText.text = "$progress/$totalProgressValue"
    }

    private fun initialize() {

        binding.imgBackCookingSch.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.tvSkipBtn.setOnClickListener{
            stillSkipDialog()
        }

        binding.tvNextBtn.setOnClickListener{
            if (statusType.equals("2",true)){
                if (sessionManagement.getCookingFor().equals("Myself",true)){
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                } else if (sessionManagement.getCookingFor().equals("MyPartner",true)) {
                    findNavController().navigate(R.id.mealRoutineFragment)
                } else {
                    findNavController().navigate(R.id.spendingOnGroceriesFragment)
                }
            }
        }
    }

    private fun stillSkipDialog() {
        val dialogStillSkip: Dialog? = context?.let { Dialog(it) }
        dialogStillSkip?.setContentView(R.layout.alert_dialog_still_skip)
        dialogStillSkip?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogCancelBtn = dialogStillSkip?.findViewById<TextView>(R.id.tvDialogCancelBtn)
        val tvDialogSkipBtn = dialogStillSkip?.findViewById<TextView>(R.id.tvDialogSkipBtn)
        dialogStillSkip?.show()
        dialogStillSkip?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogCancelBtn?.setOnClickListener {
            dialogStillSkip.dismiss()
        }

        tvDialogSkipBtn?.setOnClickListener {
            dialogStillSkip.dismiss()
            if (sessionManagement.getCookingFor().equals("Myself",true)){
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            } else if (sessionManagement.getCookingFor().equals("MyPartner",true)) {
                findNavController().navigate(R.id.mealRoutineFragment)
            } else {
                findNavController().navigate(R.id.spendingOnGroceriesFragment)
            }
        }
    }

    override fun itemClick(position: Int?, status: String?, type: String?) {
        if (status == "1") {
            statusType=""
            binding.tvNextBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        } else {
            statusType="2"
            binding.tvNextBtn.isClickable = true
            binding.tvNextBtn.setBackgroundResource(R.drawable.green_fill_corner_bg)

        }
    }

}