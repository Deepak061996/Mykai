package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.HelthActivityAdapter
import com.mykaimeal.planner.adapter.HelthActivityTargetAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentHealthDataBinding
import com.mykaimeal.planner.di.CustomDatePickerView
import com.mykaimeal.planner.di.WeekdayTimePickerView
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.ApiModelBMR
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.DataPerWeek
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.SettingViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.activitymodel.ActivityDataModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.ProfileRootResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import androidx.core.graphics.drawable.toDrawable

@AndroidEntryPoint
class HealthDataFragment : Fragment(), OnItemClickedListener {

    private lateinit var binding: FragmentHealthDataBinding
    private lateinit var viewModel: SettingViewModel
    private var genderType: String = ""
    private lateinit var popupWindowActivityLevel: PopupWindow
    private var popupWindow: PopupWindow? = null
    private lateinit var adapterHealthActivity: HelthActivityAdapter
    private lateinit var adapterHealthTarget: HelthActivityTargetAdapter
    private var listActivity:MutableList<ActivityDataModel> = mutableListOf()
    private var heights = mutableListOf<String>()
    private val cmList = mutableListOf<String>()
    private var targetList = mutableListOf<DataPerWeek>()
    private lateinit var dialogSave:Dialog
    private lateinit var sessionManagement: SessionManagement


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHealthDataBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }


        holdList()

        setupBackNavigation()

        if (viewModel.getProfileData() != null) {
            showDataInUi(viewModel.getProfileData()!!)
        } else {
            // This condition is true when network condition is enable and call the api if condition is true
            if (BaseApplication.isOnline(requireActivity())) {
                getUserProfileData()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        checkButtonDisable()

        setupUi()

        return binding.root
    }

    private fun holdList(){
        listActivity.clear()
        listActivity.add(ActivityDataModel("Sedentary","Little to no exercise.","(desk job, minimal walking)"))
        listActivity.add(ActivityDataModel("Lightly active","Light exercise or sports 1–3 days/week.","(casual walks, yoga)"))
        listActivity.add(ActivityDataModel("Moderately active","Moderate exercise 3–5 days/week.","(gym workouts, cycling)"))
        listActivity.add(ActivityDataModel("Very active","Hard exercise 6–7 days/week.","(a physically demanding job)"))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupUi() {


        binding.imgBackHealthData.setOnClickListener {
            backButtonLogic()
        }

        binding.tvNutrition.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidation("1")) {
                    saveAlertBox()
                }

            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.imageEditTargets.setOnClickListener {
            if (isValidation("1")) {
                saveAlertBox()
            }
        }

        binding.tvTargetdate.setOnClickListener {
            if (isValidation("2")){
                openTargetDate()
            }
        }

        binding.textMale.setOnClickListener { selectGender(true,"1") }

        binding.textFemale.setOnClickListener { selectGender(false,"1") }

        binding.etHeight.setOnClickListener {
            openDialogWeight("Height")
        }

        binding.tvweight.setOnClickListener {
            openDialogWeight("Weight")
        }

        binding.tvTargetweight.setOnClickListener {
            if (binding.tvweight.text.toString().equals("Weight",true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.weightError, false)
            }else{
                openDialogWeight("Target Weight")
            }
        }

        binding.tvActivityLevel.setOnClickListener {
            openDialogActivityLevel()
        }

        binding.etDateOfBirth.setOnClickListener {
            openCalendarBox()
        }


        binding.relTargetdate.setOnClickListener {
            val status=viewModel.getProfileData()?.typeStatus?:"0"
            if (status.equals("1",true)){
                if (BaseApplication.isOnline(requireActivity())) {
                    logicBMR("TargetDate","1")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        binding.layBottom.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidation("1")) {
//                    saveAlertBox()
                    (activity as MainActivity?)?.upDatePlan()
                    upDateProfile()
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun saveAlertBox(){

        dialogSave = Dialog(requireContext())
        dialogSave.setContentView(R.layout.alert_dialog_health_popup)
        dialogSave.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Set width and height
        dialogSave.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // or specific width like 600
            ViewGroup.LayoutParams.WRAP_CONTENT   // or specific height like 400
        )

        val tvTitle = dialogSave.findViewById<TextView>(R.id.tvTitle)
        val tvCheckBox = dialogSave.findViewById<TextView>(R.id.tvcheckbox)
        val tvDes = dialogSave.findViewById<TextView>(R.id.tvDes)
        val tvCancel = dialogSave.findViewById<TextView>(R.id.tvCancel)
        val tvContinue = dialogSave.findViewById<TextView>(R.id.tvContinue)

        var status=true
        tvCheckBox.visibility = View.VISIBLE

        tvCancel.setTextColor(Color.parseColor("#06C169"))
        tvCancel.setBackgroundResource(R.drawable.outline_green_border_bg)

        tvContinue.setTextColor(Color.parseColor("#FFFFFF"))
        tvContinue.setBackgroundResource(R.drawable.gray_btn_unselect_background)

        tvContinue.isEnabled = false

        tvTitle.text="Disclaimer"

        tvDes.text=ErrorMessage.healthUpdateError

        tvCancel.setOnClickListener { dialogSave.dismiss() }

        tvCheckBox.setOnClickListener {
            if (status){
                status=false
                tvCheckBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tick_ckeckbox_images,0,0,0)
                tvContinue.setBackgroundResource(R.drawable.gray_btn_select_background)
                tvContinue.isEnabled = true
            }else{
                tvCheckBox.setCompoundDrawablesWithIntrinsicBounds(R.drawable.uncheck_box_images,0,0,0)
                status=true
                tvContinue.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                tvContinue.isEnabled = false
            }
        }

        tvContinue.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (isValidation("1")) {
                    dialogSave.dismiss()
                    findNavController().navigate(R.id.nutritionGoalFragment)
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        dialogSave.show()

    }



    @SuppressLint("MissingInflatedId")
    private fun openDialogActivityLevel() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View = inflater?.inflate(R.layout.item_show_data_health, null) ?: return
        // Setup RecyclerView first
        val rcyActivity = popupView.findViewById<RecyclerView>(R.id.rcyActivity)

        val selectedDate = binding.tvActivityLevel.text.toString()
        listActivity.forEach { dataPerWeek ->
            dataPerWeek.is_selected = if (dataPerWeek.title == selectedDate) 1 else 0
        }

        adapterHealthActivity = HelthActivityAdapter(listActivity, requireActivity(), this)
        rcyActivity?.adapter = adapterHealthActivity

        // Measure popup height
        val popupHeight = getPopupHeight(popupView)
        popupWindowActivityLevel = PopupWindow(popupView, binding.tvActivityLevel.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        val tvActivityLevel = binding.tvActivityLevel
        // Get screen height
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        // Get tvActivityLevel position relative to screen
        val location = IntArray(2)
        tvActivityLevel.getLocationOnScreen(location)
        val tvYPosition = location[1]
        val tvHeight = tvActivityLevel.height

        val extraMargin = 5
        val extraMarginPx = (extraMargin * resources.displayMetrics.density).toInt()

        // Calculate available space
        val spaceBelow = screenHeight - (tvYPosition + tvHeight)
        val spaceAbove = tvYPosition

        // Determine position
        if (spaceBelow >= popupHeight) {
            popupWindowActivityLevel.showAsDropDown(tvActivityLevel, 0, 8, Gravity.NO_GRAVITY)
        } else if (spaceAbove >= popupHeight) {
            popupWindowActivityLevel.showAsDropDown(tvActivityLevel, 0, -popupHeight - tvHeight - extraMarginPx, Gravity.NO_GRAVITY)
        } else {
            // Show where there's more space
            if (spaceBelow > spaceAbove) {
                popupWindowActivityLevel.showAsDropDown(tvActivityLevel, 0, 8, Gravity.NO_GRAVITY)
            } else {
                popupWindowActivityLevel.showAsDropDown(tvActivityLevel, 0, -popupHeight - tvHeight - extraMarginPx, Gravity.NO_GRAVITY)
            }
        }
    }

    // Method to measure popup height dynamically
    private fun getPopupHeight(popupView: View): Int {
        popupView.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        return popupView.measuredHeight
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun openTargetDate(){
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View = inflater?.inflate(R.layout.item_show_data_health, null) ?: return

        val rcyActivity = popupView.findViewById<RecyclerView>(R.id.rcyActivity)
        if (targetList.size>0){
            val selectedDate = binding.tvTargetdate.text.toString()
            targetList.forEach { dataPerWeek ->
                dataPerWeek.is_selected = if (dataPerWeek.name == selectedDate) 1 else 0
            }
            val type=if (binding.tvweight.text.toString().contains("lb")) {
                "lb"
            }else{
                "kg"
            }
            adapterHealthTarget= HelthActivityTargetAdapter(targetList,requireActivity(),this,type)
            rcyActivity?.adapter=adapterHealthTarget
        }

        // Measure popup height
        val popupHeight = getPopupHeight(popupView)
        popupWindow = PopupWindow(popupView, binding.tvTargetdate.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        val tvActivityLevel = binding.tvTargetdate
        // Get screen height
        val displayMetrics = resources.displayMetrics
        val screenHeight = displayMetrics.heightPixels
        // Get tvActivityLevel position relative to screen
        val location = IntArray(2)
        tvActivityLevel.getLocationOnScreen(location)
        val tvYPosition = location[1]
        val tvHeight = tvActivityLevel.height
        // Calculate available space
        val spaceBelow = screenHeight - (tvYPosition + tvHeight)
        val spaceAbove = tvYPosition
        // Determine position
        if (spaceBelow >= popupHeight) {
            Log.d("*****Show","A")
            popupWindow?.showAsDropDown(tvActivityLevel, 0, 8, Gravity.NO_GRAVITY)
        } else if (spaceAbove >= popupHeight) {
            Log.d("*****Show","B")
            popupWindow?.showAsDropDown(binding.relTargetWeight, 0,  -35, Gravity.NO_GRAVITY)
        } else {
            // Show where there's more space
            if (spaceBelow > spaceAbove) {
                Log.d("*****Show","C")
                popupWindow?.showAsDropDown(tvActivityLevel, 0, 8, Gravity.NO_GRAVITY)
            } else {
                Log.d("*****Show","A")
                popupWindow?.showAsDropDown(binding.relTargetWeight, 0, -35, Gravity.NO_GRAVITY)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun openDialogWeight(type:String) {
        val dialogWeight = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialogWeight.setContentView(R.layout.alert_dialog_weight)
        dialogWeight.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogWeight.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogWeight.window?.setGravity(Gravity.BOTTOM)

        val imageCross: ImageView? = dialogWeight.findViewById(R.id.imageCross)
        val btnSave: TextView? = dialogWeight.findViewById(R.id.btnSave)
        val llFt: LinearLayout? = dialogWeight.findViewById(R.id.llft)
        val llCm: LinearLayout? = dialogWeight.findViewById(R.id.llcm)
        val textFt: TextView? = dialogWeight.findViewById(R.id.textft)
        val textCm: TextView? = dialogWeight.findViewById(R.id.textcm)
        val tvtitle: TextView? = dialogWeight.findViewById(R.id.tvtitle)
        val weekdayTimePickerView: WeekdayTimePickerView? = dialogWeight.findViewById(R.id.weekday_time_picker_view)
        heights.clear()
        cmList.clear()
        val bottomSheet = dialogWeight.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isHideable = true // Prevent swipe down to hide
            behavior.state = BottomSheetBehavior.STATE_EXPANDED // Fully expand
            behavior.skipCollapsed = true
        }


        tvtitle?.text=type

        var selectedType=""

        if (type.equals("Height",true)){
            textFt?.text="ft"
            textCm?.text="cm"
            selectedType="ft"
            for (feet in 2..8) {
                for (inch in 0..11) {
                    if (inch == 0) {
                        heights.add("$feet ft")
                    } else {
                        if (feet != 8) {
                            heights.add("$feet ft $inch in")
                        }
                    }
                }
            }
            for (cm in 61..244) {
                cmList.add("$cm cm")
            }
        }else{
            selectedType="lb"
            textFt?.text="lb"
            textCm?.text="kg"

            val weightLocal = binding.tvweight.text.toString()
            val currentWeight = weightLocal.replace("lb", "").replace("kg", "").toDoubleOrNull() ?: 0.0

            val lbRange = 660..4400 // 66.0 to 440.0
            val kgRange = 300..2000 // 30.0 to 200.0

            val lbData = lbRange
                .map { it / 10.0 }
                .filter { value ->
                    if (type.equals("Target Weight",true)) {
                        currentWeight.let { selectedKg ->
                            value <= (selectedKg - 1.0) || value >= (selectedKg + 1.0)
                        } ?: true
                    } else {
                        true
                    }
                }
                .map { String.format("%.1f", it) }

            val kgData = kgRange
                .map { it / 10.0 }
                .filter { value ->
                    if (type.equals("Target Weight",true)) {
                        currentWeight.let { selectedKg ->
                            value <= (selectedKg - 0.5) || value >= (selectedKg + 0.5)
                        } ?: true
                    } else {
                        true
                    }
                }
                .map { String.format("%.1f", it) }

            heights.addAll(lbData)
            cmList.addAll(kgData)

        }

        fun updateButtonStates(isListAActive: Boolean) {
            // Update button appearance to show which list is active
            if (isListAActive) {
                textFt?.setBackgroundResource(R.drawable.select_helth_bg)
                textFt?.setTextColor(Color.parseColor("#FFFFFF"))
                textCm?.setBackgroundResource(R.drawable.unselect_health_bg)
                textCm?.setTextColor(Color.parseColor("#06C169"))
            } else {
                textFt?.setBackgroundResource(R.drawable.unselect_health_bg)
                textFt?.setTextColor(Color.parseColor("#06C169"))
                textCm?.setBackgroundResource(R.drawable.select_helth_bg)
                textCm?.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }

        weekdayTimePickerView?.setLists(heights, cmList)
        // Enable haptic feedback
        weekdayTimePickerView?.isHapticFeedbackEnabled = false
        // Set circular scrolling if needed
        weekdayTimePickerView?.isCircular = true


        if (type.equals("Height",true)){
            if (binding.etHeight.text.toString().equals("Height",true)){
                selectedType="ft"
                weekdayTimePickerView?.showListA()
                weekdayTimePickerView?.showListAWithValue("5 ft 5 in")
                updateButtonStates(true)
            }else{
                if (binding.etHeight.text.toString().contains("cm")){
                    selectedType="cm"
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListB()
                    weekdayTimePickerView?.showListBWithValue(binding.etHeight.text.toString().trim())
                    updateButtonStates(false)
                }else{
                    selectedType="ft"
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListA()
                    weekdayTimePickerView?.showListAWithValue(convertToFeetInches(binding.etHeight.text.toString().trim()))
                    updateButtonStates(true)
                }
            }
        }


        if (type.equals("Weight",true)){
            if (binding.tvweight.text.toString().equals("Weight",true)){
                selectedType="lb"
               val showValue = if (genderType.equals("male",true)){
                   "154.3"
                }else{
                   "132.3"
                }
                weekdayTimePickerView?.showListA()
                weekdayTimePickerView?.showListAWithValue(showValue)
                updateButtonStates(true)
            }else{
                if (binding.tvweight.text.toString().contains("kg")){
                    selectedType="kg"
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListB()
                    weekdayTimePickerView?.showListBWithValue(binding.tvweight.text.toString().replace("kg","").trim())
                    updateButtonStates(false)
                }else{
                    selectedType="lb"
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListA()
                    weekdayTimePickerView?.showListAWithValue(binding.tvweight.text.toString().replace("lb","").trim())
                    updateButtonStates(true)
                }
            }
        }

        if (type.equals("Target Weight",true)){
            if (binding.tvTargetweight.text.toString().equals("Target Weight",true)){
                if (binding.tvweight.text.toString().contains("kg")) {
                    selectedType = "kg"
                    val value=binding.tvweight.text.toString().replace("kg", "").trim().toDouble()+0.5
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListB()
                    weekdayTimePickerView?.showListBWithValue(value.toString())
                    updateButtonStates(false)
                } else {
                    val value=binding.tvweight.text.toString().replace("lb", "").trim().toDouble()+1.0
                    selectedType = "lb"
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListA()
                    weekdayTimePickerView?.showListAWithValue(value.toString())
                    updateButtonStates(true)
                }
            }else {
                if (binding.tvweight.text.toString().contains("kg")) {
                    selectedType = "kg"
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListB()
                    weekdayTimePickerView?.showListBWithValue(binding.tvTargetweight.text.toString().replace("kg", "").trim())
                    updateButtonStates(false)
                } else {
                    selectedType = "lb"
                    // Show List A (feet and inches) by default
                    weekdayTimePickerView?.showListA()
                    weekdayTimePickerView?.showListAWithValue(binding.tvTargetweight.text.toString().replace("lb", "").trim())
                    updateButtonStates(true)
                }
            }
        }


        llFt?.setOnClickListener {
            if (type.equals("Height",true)){
                val dataValue=weekdayTimePickerView?.selectedValue
                selectedType=textFt?.text.toString()
                weekdayTimePickerView?.showListA()
                if (binding.etHeight.text.toString().contains("cm")){
                    weekdayTimePickerView?.showListAWithValue(convertCmStringToFeetAndInches(binding.etHeight.text.toString().replace("cm","").trim()))
                }else{
                    val data=extractNumber(dataValue?:"0 cm")
                    weekdayTimePickerView?.showListAWithValue(cmStringToFeetInches(data.toString()))
                }
                updateButtonStates(true)
            }

            if (type.equals("Weight",true)){
                selectedType=textFt?.text.toString()
                val dataValue=weekdayTimePickerView?.selectedValue
                weekdayTimePickerView?.showListA()
                if (binding.tvweight.text.toString().contains("lb")){
                    weekdayTimePickerView?.showListAWithValue(binding.tvweight.text.toString().replace("lb","").trim())
                }else{
                    weekdayTimePickerView?.showListAWithValue(kgToLb(dataValue?.toDouble()?:0.0).toString())
                }
                updateButtonStates(true)
            }

            if (type.equals("Target Weight",true)){
                if (binding.tvweight.text.toString().contains("lb")){
                    selectedType=textFt?.text.toString()
                    val dataValue=weekdayTimePickerView?.selectedValue
                    weekdayTimePickerView?.showListA()
                    if (binding.tvTargetweight.text.toString().contains("lb")){
                        weekdayTimePickerView?.showListAWithValue(binding.tvTargetweight.text.toString().replace("lb","").trim())
                    }else{
                        weekdayTimePickerView?.showListAWithValue(kgToLb(dataValue?.toDouble()?:0.0).toString())
                    }
                    updateButtonStates(true)

                }else{
                    Toast.makeText(requireContext(),ErrorMessage.kgError,Toast.LENGTH_SHORT).show()
                }
            }

        }

        llCm?.setOnClickListener {

            if (type.equals("Height",true)){
                selectedType=textCm?.text.toString()
                val dataValue=weekdayTimePickerView?.selectedValue
                weekdayTimePickerView?.showListB() // Call showListB for centimeters
                if (binding.etHeight.text.toString().contains("cm")){
                    weekdayTimePickerView?.showListBWithValue(binding.etHeight.text.toString())
                }else{
                    weekdayTimePickerView?.showListBWithValue(feetInchesToCm(dataValue.toString()).toInt().toString()+" cm")
                }
                updateButtonStates(false)
            }

            if (type.equals("Weight",true)){
                selectedType=textCm?.text.toString()
                val dataValue = weekdayTimePickerView?.selectedValue
                weekdayTimePickerView?.showListB() // Call showListB for centimeters
                if (binding.tvweight.text.toString().contains("lb")){
                    weekdayTimePickerView?.showListBWithValue(convertWeightLbToKg(binding.tvweight.text.toString().replace("kg","").trim()).toString())
                }else{
                    weekdayTimePickerView?.showListBWithValue(lbToKg(dataValue?.toDouble()?:0.0).toString())
                }
                updateButtonStates(false)
            }

            if (type.equals("Target Weight",true)){
                if (binding.tvweight.text.toString().contains("kg")){
                    selectedType=textCm?.text.toString()
                    val dataValue = weekdayTimePickerView?.selectedValue
                    weekdayTimePickerView?.showListB() // Call showListB for centimeters
                    if (binding.tvTargetweight.text.toString().contains("lb")){
                        weekdayTimePickerView?.showListBWithValue(convertWeightLbToKg(binding.tvTargetweight.text.toString().replace("kg","").trim()).toString())
                    }else{
                        weekdayTimePickerView?.showListBWithValue(lbToKg(dataValue?.toDouble()?:0.0).toString())
                    }
                    updateButtonStates(false)

                }else{
                    Toast.makeText(requireContext(),ErrorMessage.lbError,Toast.LENGTH_SHORT).show()
                }
            }

        }

        imageCross?.setOnClickListener {
            dialogWeight.dismiss()
        }

        btnSave?.setOnClickListener {
            val selectedValue = weekdayTimePickerView?.selectedValue
            val value = when (selectedType.lowercase()) {
                "ft" -> selectedValue?.let { convertMeasurement(it) }.toString()
                "cm" -> selectedValue.toString()
                "lb", "kg" -> "$selectedValue $selectedType"
                else -> ""
            }
            val valueTrimmed = value.trim()

            when (type.lowercase()) {
                "height" -> binding.etHeight.text = valueTrimmed
                "weight" -> {
                    binding.tvweight.text = valueTrimmed
                    if(viewModel.getProfileData()?.typeStatus.equals("0")){
                        binding.tvTargetweight.text = "Target Weight"
                        binding.tvTargetweight.setTextColor(Color.parseColor("#3C4541"))
                    }else{
                        binding.tvTargetweight.setTextColor(Color.parseColor("#666666"))
                    }
                }
                "target weight" -> binding.tvTargetweight.text = valueTrimmed
            }

            val dob = binding.etDateOfBirth.text.toString()
            val height = binding.etHeight.text.toString()
            val weight = binding.tvweight.text.toString()
            val targetWeight = binding.tvTargetweight.text.toString()
            val activityLevel = binding.tvActivityLevel.text.toString()
            val targetDate = binding.tvTargetdate.text.toString()

            val isFormValid = !dob.equals("dd/mm/yyyy", true)
                    && !height.equals("Height", true)
                    && !weight.equals("Weight", true)
                    && !targetWeight.equals("Target Weight", true)
                    && !activityLevel.equals("Select Your Activity Level", true)
                    && !targetDate.equals("Select", true)

            if(viewModel.getProfileData()?.typeStatus.equals("1")){
                checkButtonDisable()
            }else{
                if (isFormValid) {
                    logicBMR("TargetDate","0")
                } else {
                    checkButtonDisable()
                }
            }

            dialogWeight.dismiss()
        }
        dialogWeight.show()
    }


    @SuppressLint("DefaultLocale")
    fun feetInchesToCm(input: String): Double {
        val feetRegex = Regex("(\\d+)\\s*ft")
        val inchesRegex = Regex("(\\d+)\\s*in")

        val feet = feetRegex.find(input)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        val inches = inchesRegex.find(input)?.groupValues?.get(1)?.toIntOrNull() ?: 0

        val totalInches = (feet * 12) + inches
        val cm = totalInches * 2.54

        return String.format("%.1f", cm).toDouble()
    }

    private fun extractNumber(cmString: String): Int {
        return cmString.filter { it.isDigit() }.toInt()
    }

    fun cmStringToFeetInches(cmStr: String): String {
        val cm = cmStr.toDoubleOrNull() ?: return "Invalid input"
        val totalInches = cm / 2.54
        val feet = totalInches.toInt() / 12
        val inches = Math.round(totalInches % 12).toInt()
        return "$feet ft $inches in"
    }

    @SuppressLint("DefaultLocale")
    private fun kgToLb(kg: Double): Double {
        val conversionFactor = 2.20462262
        return String.format("%.1f", kg * conversionFactor).toDouble()
    }

    @SuppressLint("DefaultLocale")
    private fun lbToKg(lb: Double): Double {
        val conversionFactor = 0.45359237
        val result = lb * conversionFactor
        return String.format("%.1f", result).toDouble()
    }

    private fun convertWeightLbToKg(weight: String): Double {
        val regex = Regex("(\\d+(\\.\\d+)?)\\s*lb")
        val match = regex.find(weight)
        val pounds = match?.groups?.get(1)?.value?.toDoubleOrNull() ?: return 0.0
        val kg = pounds * 0.45359237
        return (kg * 10).roundToInt() / 10.0  // Rounds to 1 decimal
    }

    private fun convertCmStringToFeetAndInches(cmString: String): String {
        val cm = cmString.toDoubleOrNull() ?: return "Invalid input"
        val totalInches = cm / 2.54
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).roundToInt()

        return "$feet ft $inches in"
    }

    private fun convertToFeetInches(input: String): String {
        val regex = Regex("""(?:(\d+)')?\s*(?:(\d+)\")?""")
        val match = regex.find(input.trim())

        val feet = match?.groups?.get(1)?.value?.toIntOrNull() ?: 0
        val inches = match?.groups?.get(2)?.value?.toIntOrNull() ?: 0

        val parts = mutableListOf<String>()
        if (feet > 0) parts.add("$feet ft")
        if (inches > 0) parts.add("$inches in")

        return parts.joinToString(" ")
    }


    private fun convertMeasurement(input: String): String {
        // Remove extra spaces and convert to lowercase
        val cleaned = input.trim().lowercase()

        // Pattern to match "X ft Y in" or "X ft"
        val pattern = Regex("""(\d+)\s*ft(?:\s*(\d+)\s*in)?""")
        val match = pattern.find(cleaned)

        return if (match != null) {
            val feet = match.groupValues[1]
            val inches = match.groupValues.getOrNull(2) ?: "0"

            // If inches is 0 or empty, just show feet
            if (inches == "0" || inches.isEmpty()) {
                "$feet' ${0}\""
            } else {
                "$feet' $inches\""
            }
        } else {
            input // Return original if no match
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun logicBMR(apiType: String,toastStatus:String) {
        BaseApplication.showMe(requireContext())
        val heightLocal=binding.etHeight.text.toString()
        val weightLocal=binding.tvweight.text.toString()
        val profileData = viewModel.getProfileData()

        val targetWeightLocal = if (profileData?.typeStatus == "1") {
            val targetWeight = profileData.target_weight
            val targetWeightType = profileData.target_weight_type
            if (!targetWeight.isNullOrEmpty() && !targetWeight.equals("null", true)) {
                when (targetWeightType?.lowercase()) {
                    "kilograms" -> "$targetWeight kg"
                    "pounds", "lb" -> "$targetWeight lb"
                    else -> targetWeight
                }.also {
                    binding.tvTargetweight.text = it
                }
            } else {
                ""
            }
        } else {
            binding.tvTargetweight.text.toString()
        }


        val heightType= if (heightLocal.contains("cm")){
            "cm"
        }else{
            "feet"
        }
        val weightType= if (weightLocal.contains("lb")){
            "lb"
        }else{
            "Kilograms"
        }

        val targetWeightType = if (targetWeightLocal.contains("lb")){
            "lb"
        }else{
            "Kilograms"
        }

        val targetWeight = targetWeightLocal.replace("lb", "").replace("kg", "")

        val targetDate = binding.tvTargetdate.text.toString()

        val weight=weightLocal.replace("lb", "").replace("kg", "")


        val height= if (heightType.equals("feet",true)){
            convertToDecimalFeet(heightLocal).toString()
        }else{
            heightLocal.replace("cm", "")
        }

        val macros = if (viewModel.getProfileData()?.typeStatus.equals("1")){
            viewModel.getProfileData()?.old_macro
        }else{
            viewModel.getProfileData()?.macros.toString()?:"Balanced"
        }

        val calories=if (macros.equals("Custom",true)){
            viewModel.getProfileData()?.calories.toString()
        }else{
            ""
        }
        val fatPer=if (macros.equals("Custom",true)){
            viewModel.getProfileData()?.macro_per?.fat.toString()
        }else{
            ""
        }
        val proteinPer=if (macros.equals("Custom",true)){
            viewModel.getProfileData()?.macro_per?.protein.toString()
        }else{
            ""
        }
        val carbsPer=if (macros.equals("Custom",true)){
            viewModel.getProfileData()?.macro_per?.carbs.toString()
        }else{
            ""
        }

        Log.d("Height", "******$height")
        Log.d("Height", "******$heightType")
        Log.d("weight", "******$weight")
        Log.d("weight", "******$weightType")
        Log.d("targetWeight", "******$targetWeight")
        Log.d("targetWeight", "******$targetWeightType")

        lifecycleScope.launch {
            viewModel.updateDietSuggestionUrl({
                    BaseApplication.dismissMe()
                if (apiType.equals("AddMoreGoals",true)){
                    handleApiUpdateResponse(it,"BMR",toastStatus)
                }else{
                    handleApiUpdateResponse(it,"BMRUPDATE",toastStatus)
                }},
                genderType,
                binding.etDateOfBirth.text.toString() ,
                height,
                heightType,
                weight,
                weightType,
                binding.tvActivityLevel.text.toString().trim(),targetWeight,targetWeightType,targetDate,macros,
                calories,fatPer,proteinPer,carbsPer,viewModel.getProfileData()?.typeStatus
            )
        }
    }


    private fun convertDecimalToFeetInches(decimalFeet: Double): String {
        val feet = floor(decimalFeet).toInt()
        val remainingInches = (decimalFeet - feet) * 12
        val inches = remainingInches.toInt()
        return "$feet' $inches\""
    }

    private fun convertToDecimalFeet(input: String): Double {
        val parts = input.split("'")
        if (parts.size == 1) {
            // Only feet provided, e.g., "2'" or "2"
            return parts[0].trim().toDoubleOrNull() ?: 0.0
        } else if (parts.size == 2) {
            val feet = parts[0].trim().toDoubleOrNull() ?: 0.0
            val inchesPart = parts[1].trim()

            if (inchesPart.isEmpty()) {
                // Only feet provided with a trailing apostrophe, e.g., "2'"
                return feet
            }

            if (inchesPart.contains("\"")) {
                val inches = inchesPart.replace("\"", "").trim().toDoubleOrNull() ?: 0.0
                return feet + (inches / 12.0)
            } else {
                //Handles cases like "2' 3" without the quote
                return feet
            }


        } else {
            return 0.0 // Invalid format
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun upDateProfile() {

        BaseApplication.showMe(requireContext())

        val heightLocal=binding.etHeight.text.toString()
        val weightLocal=binding.tvweight.text.toString()


        val targetWeightLocal=if (viewModel.getProfileData()?.typeStatus.equals("1")){
             if (viewModel.getProfileData()?.target_weight_type?.contains("lb") == true){
                  viewModel.getProfileData()?.target_weight+ "lb"
             }else{
                 viewModel.getProfileData()?.target_weight+"kg"
             }
        }else{
            binding.tvTargetweight.text.toString()
        }

        val heightType= if (heightLocal.contains("cm")){
            "cm"
        }else{
            "feet"
        }

        val weightType= if (weightLocal.contains("lb")){
            "lb"
        }else{
            "Kilograms"
        }


        val targetWeightType =  if (targetWeightLocal.contains("lb")){
            "lb"
        }else{
            "Kilograms"
        }

        val targetWeight = targetWeightLocal.replace("lb", "").replace("kg", "")

        val targetDate = if (viewModel.getProfileData()?.typeStatus.equals("1")){
            viewModel.getProfileData()?.target
        }else{
            binding.tvTargetdate.text.toString()
        }



        val weight=weightLocal.replace("lb", "").replace("kg", "")

        val height= if (heightType.equals("feet",true)){
            convertToDecimalFeet(heightLocal).toString()
        }else{
            heightLocal.replace("cm", "")
        }


        lifecycleScope.launch {
            viewModel.upDateProfileRequest({
                    BaseApplication.dismissMe()
                    handleApiUpdateResponse(it,"Main","") },
                viewModel.getProfileData()?.name.toString(),
                viewModel.getProfileData()?.bio.toString(),
                genderType,
                binding.etDateOfBirth.text.toString(),
                height,
                heightType,
                binding.tvActivityLevel.text.toString(),
                viewModel.getProfileData()?.macros.toString(),
                viewModel.getProfileData()?.calories.toString(),
                viewModel.getProfileData()?.fat.toString(),
                viewModel.getProfileData()?.carbs.toString(),
                viewModel.getProfileData()?.protein.toString(),
                weight,
                weightType,targetWeight,
                targetWeightType,
                viewModel.getProfileData()?.macros.toString(),
                targetDate.toString(),
                viewModel.getProfileData()?.macro_per?.carbs.toString(),
                viewModel.getProfileData()?.macro_per?.protein.toString(),
                viewModel.getProfileData()?.macro_per?.fat.toString(),
                viewModel.getProfileData()?.time.toString(),
                viewModel.getProfileData()?.value_per_week.toString(),
                viewModel.getProfileData()?.typeStatus.toString(),
                viewModel.getProfileData()?.old_macro.toString()
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleApiUpdateResponse(result: NetworkResult<String>, type:String,toastStatus:String) {
        when (result) {
            is NetworkResult.Success -> handleUpdateSuccessResponse(result.data.toString(),type,toastStatus)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun isValidation(type:String): Boolean {
        if (binding.etDateOfBirth.text.toString().equals("dd/mm/yyyy", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.dobError, false)
            return false
        }else if (binding.etHeight.text.toString().equals("Height", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.heightError, false)
            return false
        } else if (binding.tvweight.text.toString().equals("Weight",true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.weightError, false)
            return false
        }/* else if (binding.tvTargetweight.text.toString().equals("Target Weight", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.targetweightError, false)
            return false
        }*/ else if (binding.tvActivityLevel.text.toString().equals("Select Your Activity Level", true)) {
            BaseApplication.alertError(requireContext(), ErrorMessage.activityTypeError, false)
            return false
        }/*else if (type.equals("1", true)) {
            if (binding.tvTargetdate.text.toString().equals("Select",true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.TargetTypeError, false)
                return false
            }else{
                return true
            }
        }*/
        return true
    }



    private fun isValidationDisable(): Boolean {
        if (binding.etDateOfBirth.text.toString().equals("dd/mm/yyyy", true)) {
            return false
        }else if (binding.etHeight.text.toString().equals("Height", true)) {
            return false
        } else if (binding.tvweight.text.toString().equals("Weight",true)) {
            return false
        } else if (binding.tvTargetweight.text.toString().equals("Target Weight", true)) {
            return false
        } else if (binding.tvActivityLevel.text.toString().equals("Select Your Activity Level", true)) {
            return false
        }else if (binding.tvTargetdate.text.toString().equals("Select",true)){
            return false
        }
        return true
    }


    // This function is use for open the Calendar
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun openCalendarBox() {

        val dialogWeight = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialogWeight.setContentView(R.layout.alert_dialog_helth_date)
        dialogWeight.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialogWeight.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogWeight.window?.setGravity(Gravity.BOTTOM)
        val btnSave: TextView? = dialogWeight.findViewById(R.id.btnDone)
        val weekdayTimePickerView: CustomDatePickerView? = dialogWeight.findViewById(R.id.weekday_time_picker_view)

        lifecycleScope.launch {
            delay(1000L)
            try {
                if (!binding.etDateOfBirth.text.toString().equals("mm/dd/yyyy",true)){
                    val dataDate=binding.etDateOfBirth.text.toString().split("/")
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
                val (day, month, year) = weekdayTimePickerView.getSelectedDateParts()
                Log.d("DATE_PICKER", "Selected: $month/$day/$year")
                Log.d("Selected Date ", "*****$month/$day/$year")
                val dateLocal="$month/$day/$year"
                binding.etDateOfBirth.text = /*formatDate(dateLocal)*/BaseApplication.convertDateFormat(dateLocal)
                viewModel.getProfileData()?.let { data ->
                    data.apply { dob = BaseApplication.convertDateFormat(dateLocal) }
                    viewModel.setProfileData(data)
                }
                if (viewModel.getProfileData()?.typeStatus.equals("1")) {
                    checkButtonDisable()
                } else {
                    if (!binding.etHeight.text.toString().equals("Height", true)
                        && !binding.tvweight.text.toString().equals("Weight", true)
                        && !binding.tvTargetweight.text.toString().equals("Target Weight", true)
                        && !binding.tvActivityLevel.text.toString()
                            .equals("Select Your Activity Level", true)
                        && !binding.tvTargetdate.text.toString().equals("Select", true)
                    ) {
                        logicBMR("TargetDate", "0")
                    } else {
                        checkButtonDisable()
                    }
                }
                dialogWeight.dismiss()
            }
        }


        dialogWeight.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("M/dd/yyyy")
        val outputFormatter = DateTimeFormatter.ofPattern("MM/DD/yyyy")

        return try {
            val date = LocalDate.parse(input, inputFormatter)
            date.format(outputFormatter)
        } catch (e: DateTimeParseException) {
            "Invalid date format: $input"
        }
    }


    private fun setupBackNavigation() {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    backButtonLogic()
                }
            }
        )
    }


    private fun backButtonLogic(){
        viewModel.clearData()
        findNavController().navigateUp()
    }

    // This function is use for get the user profile data when api call
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getUserProfileData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userProfileData {
                BaseApplication.dismissMe()
                handleApiResponse(it)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleUpdateSuccessResponse(data: String, type: String, toastStatus: String) {
        try {

            if (type.equals("Main",true)){
                val apiModel = Gson().fromJson(data, ProfileRootResponse::class.java)
                Log.d("@@@ Health profile", "message :- $data")
                if (apiModel.code == 200 && apiModel.success) {
                    findNavController().navigateUp()
                } else {
                     handleError(apiModel.code,apiModel.message)
                }
            }

            if (type.equals("BMR",true) || type.equals("BMRUPDATE",true)){
                val apiModel = Gson().fromJson(data, ApiModelBMR::class.java)
                Log.d("@@@ BMR profile", "message :- $data")
                if (apiModel.code == 200 && apiModel.success) {
                    popupWindow?.dismiss()
                    apiModel.data?.let { dataModel->
                        viewModel.getProfileData()?.let { data ->
                            data.apply {
                                gender=genderType
                                dob = binding.etDateOfBirth.text.toString()
                                target = dataModel.target
                                height = dataModel.height.toString()
                                height_type = dataModel.height_type.toString()
                                weight =dataModel.weight.toString()
                                weight_type =dataModel.weight_type.toString()
                                activity_level = binding.tvActivityLevel.text.toString()
                                fat = dataModel.fat
                                carbs = dataModel.carbs
                                calories = dataModel.calories
                                protein = dataModel.protein
                                macro_options=dataModel.macro_options
                                disclaimer=dataModel.disclaimer
                                macros=dataModel.macros
                                macro_per=dataModel.macro_per
                                value_per_week = dataModel.value_per_week
                                time = dataModel.time
                                typeStatus="0"
                                old_macro=dataModel.macros
                                goal_in_weeks = dataModel.goal_in_weeks
                                data_per_week= dataModel.data_per_week
                                target_weight = dataModel.target_weight.toString()
                                target_weight_type = dataModel.target_weight_type.toString()
                            }
                            viewModel.setProfileData(data)
                            if (type.equals("BMR",true)){
                                findNavController().navigate(R.id.nutritionGoalFragment)
                            }else{
                                if (toastStatus.equals("1",true)){
                                    Toast.makeText(requireContext(),"Restored successfully.",Toast.LENGTH_SHORT).show()
                                }
                                showDataInUi(viewModel.getProfileData()!!)
                            }
                        }
                    }
                } else {
                    handleError(apiModel.code,apiModel.message)
                }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, ProfileRootResponse::class.java)
            Log.d("@@@ Health profile", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                viewModel.setProfileData(apiModel.data)
                showDataInUi(apiModel.data)
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    private fun showDataInUi(data: Data) {

        data.gender?.let {
            when (it.lowercase()) {
                "male" -> selectGender(true,"0")
                "female" -> selectGender(false,"0")
                else -> resetGenderSelection()
            }
        }

        if (data.height != null && !data.height.equals("null",true)) {
            data.height_type?.let {
                if (!it.equals("",true)){
                    if (it.equals("feet",true)){
                        binding.etHeight.text = data.height?.toDouble()?.let { it1 -> convertDecimalToFeetInches(it1) }
                    }else{
                        binding.etHeight.text =""+ data.height.toString().toInt() +" cm"
                    }
                }

            }
        }

        if (data.weight != null && !data.weight.equals("null",true)) {
            data.weight_type?.let {
                if (!it.equals("",true)){
                    if (it.equals("Kilograms",true)){
                        binding.tvweight.text = data.weight +" kg"
                    }else{
                        binding.tvweight.text = data.weight+" lb"
                    }
                }
            }
        }


        targetList.clear()

        data.data_per_week?.let {
            targetList.addAll(it)
        }?:run {
            binding.tvsms.text= ""
        }

        if (data.dob != null && !data.dob.equals("null",true)) {
            binding.etDateOfBirth.text = BaseApplication.convertDateFormat(data.dob.toString())
        }

        if (data.activity_level != null && !data.activity_level.equals("null",true)) {
            binding.tvActivityLevel.text = data.activity_level
        }

        data.target?.let {
            binding.tvTargetdate.text = it
        }

        if (data.typeStatus.equals("1")){
            binding.tvTargetweight.isEnabled=false
            binding.tvTargetdate.isEnabled=false
            binding.tvTargetdate.setTextColor(Color.parseColor("#999999"))
            binding.tvTargetweight.setTextColor(Color.parseColor("#999999"))
            binding.tvsms.setTextColor(Color.parseColor("#FF3232"))
            binding.tvsmstargetweight.text="You have set your own calories so your target date cannot be calculated."
            binding.tvsms.text="You have set your own calories so your target date cannot be calculated."
            binding.tvsms.visibility= View.GONE
            binding.tvsmstargetweight.visibility= View.GONE
            binding.layShow.visibility= View.VISIBLE
            binding.relTargetWeight.setBackgroundResource(R.drawable.circular_edittext_bg_unseletced)
            binding.relTargetdate.setBackgroundResource(R.drawable.circular_edittext_bg_unseletced)
            binding.tvTargetdate.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
        }else{
            binding.relTargetWeight.setBackgroundResource(R.drawable.circular_edittext_bg)
            binding.relTargetdate.setBackgroundResource(R.drawable.circular_edittext_bg)
            binding.tvTargetweight.setTextColor(Color.parseColor("#3C4541"))
            binding.tvTargetdate.setTextColor(Color.parseColor("#3C4541"))
            binding.tvsms.setTextColor(Color.parseColor("#06C169"))
            binding.tvTargetweight.isEnabled=true
            binding.tvsms.text=""
            binding.tvTargetdate.isEnabled=true
            binding.tvTargetdate.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.drop_down_icon,0)
            binding.tvsmstargetweight.visibility= View.GONE
            binding.tvsms.visibility= View.VISIBLE
            binding.layShow.visibility= View.GONE

            if (targetList.size>0){
                targetList.forEachIndexed { _, dataPerWeek ->
                    if (dataPerWeek.name.equals(binding.tvTargetdate.text.toString())){
                        dataPerWeek.is_selected = 1
                    }else{
                        dataPerWeek.is_selected = 0
                    }
                }
                val dataList=targetList.find { it.name.equals(binding.tvTargetdate.text.toString())}
                binding.tvsms.text= dataList?.description?.trim()
            }else{
                binding.tvsms.text= ""
            }
        }

        if (data.target_weight != null && !data.target_weight.equals("null",true)) {
            data.target_weight_type?.let {
                if (!it.equals("",true)){
                    if (it.equals("Kilograms",true)){
                        binding.tvTargetweight.text = data.target_weight +" kg"
                    }else{
                        binding.tvTargetweight.text = data.target_weight+" lb"
                    }
                }
            }
        }

        if ((sessionManagement.getCountHealth() ?: 0) == 0){
            binding.llCalculateBMR.visibility = View.GONE
            binding.layNutrition.visibility = View.VISIBLE
            binding.layBottom.visibility = View.GONE
            binding.imageEditTargets.visibility = View.GONE
        }else{
            if ((data.calories ?: 0) == 0 && (data.carbs ?: 0) == 0 && (data.fat ?: 0) == 0 && (data.protein ?: 0) == 0) {
                binding.llCalculateBMR.visibility = View.GONE
                binding.layNutrition.visibility = View.VISIBLE
                binding.layBottom.visibility = View.GONE
                binding.imageEditTargets.visibility = View.GONE
            } else {
                binding.llCalculateBMR.visibility = View.VISIBLE
                binding.layNutrition.visibility = View.GONE
                binding.layBottom.visibility = View.VISIBLE
                binding.imageEditTargets.visibility = View.VISIBLE

                if ((data.calories ?: 0) == 0) {
                    binding.tvCalories.text = "" + 0
                } else {
                    binding.tvCalories.text = "" + data.calories
                }

                if ((data.carbs ?: 0) == 0) {
                    binding.tvCarbs.text = "" + 0+"g"
                } else {
                    binding.tvCarbs.text = "" + data.carbs+"g"
                }

                if ((data.fat ?: 0) == 0) {
                    binding.tvFat.text = "" + 0+"g"
                } else {
                    binding.tvFat.text = "" + data.fat+"g"
                }

                if ((data.protein ?: 0) == 0) {
                    binding.tvProtein.text = "" + 0+"g"
                }else {
                    binding.tvProtein.text = "" + data.protein+"g"
                }
            }
        }

        checkButtonDisable()

    }

    private fun checkButtonDisable(){
        if (isValidationDisable()){
            binding.tvNutrition.isEnabled= true
            binding.tvNutrition.setBackgroundResource(R.drawable.select_bg_add)
        }else{
            binding.tvNutrition.isEnabled= false
            binding.tvNutrition.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun selectGender(isMale: Boolean,status:String) {
        val selectedIcon = R.drawable.radio_select_icon
        val unselectedIcon = R.drawable.radio_unselect_icon

        genderType = if (isMale) {
            "male"
        } else {
            "female"
        }

        binding.textMale.setCompoundDrawablesWithIntrinsicBounds(
            if (isMale) selectedIcon else unselectedIcon, 0, 0, 0
        )
        binding.textFemale.setCompoundDrawablesWithIntrinsicBounds(
            if (isMale) unselectedIcon else selectedIcon, 0, 0, 0
        )

        if (status.equals("1",true)){
            val dob = binding.etDateOfBirth.text.toString()
            val height = binding.etHeight.text.toString()
            val weight = binding.tvweight.text.toString()
            val targetWeight = binding.tvTargetweight.text.toString()
            val activityLevel = binding.tvActivityLevel.text.toString()
            val targetDate = binding.tvTargetdate.text.toString()

            val isFormValid = !dob.equals("dd/mm/yyyy", true)
                    && !height.equals("Height", true)
                    && !weight.equals("Weight", true)
                    && !targetWeight.equals("Target Weight", true)
                    && !activityLevel.equals("Select Your Activity Level", true)
                    && !targetDate.equals("Select", true)

            if(viewModel.getProfileData()?.typeStatus.equals("1")){
                checkButtonDisable()
            }else{
                if (isFormValid) {
                    logicBMR("TargetDate","0")
                } else {
                    checkButtonDisable()
                }
            }
        }
    }

    private fun resetGenderSelection() {
        val unselectedIcon = R.drawable.radio_unselect_icon
        binding.textMale.setCompoundDrawablesWithIntrinsicBounds(unselectedIcon, 0, 0, 0)
        binding.textFemale.setCompoundDrawablesWithIntrinsicBounds(unselectedIcon, 0, 0, 0)
    }

    private fun showAlert(message: String?, isError: Boolean) {
        BaseApplication.alertError(requireContext(), message, isError)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun itemClicked(position: Int?, list: MutableList<String>?, status: String?, type: String?) {

        if (status.equals("target",true)){
            popupWindow?.dismiss()
            val dataItem = targetList[position!!]
            binding.tvTargetdate.text=dataItem.name
            binding.tvsms.text=dataItem.description
            logicBMR("TargetDate","0")
        }else{
            popupWindowActivityLevel.dismiss()
            val dataItem = listActivity[position!!]
            binding.tvActivityLevel.text = dataItem.title
            if (viewModel.getProfileData()?.typeStatus.equals("1")){
                checkButtonDisable()
            }else{
                if (!binding.etDateOfBirth.text.toString().equals("dd/mm/yyyy",true)
                    && !binding.etHeight.text.toString().equals("Height",true)
                    && !binding.tvTargetweight.text.toString().equals("Target Weight",true)
                    && !binding.tvweight.text.toString().equals("Weight",true)
                    && !binding.tvTargetdate.text.toString().equals("Select",true)){
                    logicBMR("TargetDate","0")
                }else{
                    checkButtonDisable()
                }
            }
        }
    }
}
