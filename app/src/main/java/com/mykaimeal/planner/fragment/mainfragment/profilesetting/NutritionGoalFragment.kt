package com.mykaimeal.planner.fragment.mainfragment.profilesetting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickedListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.adapter.HelthActivityAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentNutritionGoalBinding
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.ApiModelBMR
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.Macroper
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.SettingViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.activitymodel.ActivityDataModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.settingviewmodel.apiresponse.ProfileRootResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class NutritionGoalFragment : Fragment(), OnItemClickedListener {

    private lateinit var binding: FragmentNutritionGoalBinding
    private lateinit var viewModel: SettingViewModel
    private lateinit var sessionManagement: SessionManagement
    private var calories=0
    private var fat=0
    private var carb=0
    private var protine=0
    private var listActivity:MutableList<ActivityDataModel> = mutableListOf()
    private lateinit var adapterHealthActivity: HelthActivityAdapter
    private lateinit var popupWindowActivityLevel: PopupWindow
    private var genderType: String = ""
    private var macroOption=""
    private var disclaimer=""
    private var calStatus="0"
    var isSeekAllowed = false // Flag to control seek permission
    // Add these flags outside the listener (as class-level variables)
    var alertShown = false
    var lastAlertType: String? = null  // "increase" or "decrease"
    var lastAlertFat:String=""
    var marcoType:String=""
    var lastAlertCarb:String=""
    var lastAlertProtin:String=""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNutritionGoalBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())

        holdList()
        setupBackPressHandler()
        initializeUI()
        loadProfileData()

        return binding.root
    }

    private fun holdList(){
        listActivity.clear()
        listActivity.add(ActivityDataModel("Balanced","Supports overall health"))
        listActivity.add(ActivityDataModel("Low Carb","Helps with weight management"))
        listActivity.add(ActivityDataModel("High Protein","Supports muscle strength"))
        listActivity.add(ActivityDataModel("Keto","Promotes the use of fat for energy"))
        listActivity.add(ActivityDataModel("Low Fat","Good for heart health"))
        listActivity.add(ActivityDataModel("Custom","Create your own customization"))
    }


    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()
            }
        })
    }

    private fun loadProfileData() {
        val profileData = viewModel.getProfileData()
        if (profileData != null) {
            updateUI(profileData)
        } else if (BaseApplication.isOnline(requireActivity())) {
            fetchUserProfileData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchUserProfileData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.userProfileData { result ->
                BaseApplication.dismissMe()
                handleApiResponse(result)
            }
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> parseAndHandleSuccess(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun parseAndHandleSuccess(data: String) {
        try {
            val response = Gson().fromJson(data, ProfileRootResponse::class.java)
            if (response.code == 200 && response.success) {
                response.data.let {
                    viewModel.setProfileData(it)
                    updateUI(it)
                }
            } else {
                showAlert(response.message, response.code == ErrorMessage.code)
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

    private fun showAlert(message: String?, isError: Boolean) {
        BaseApplication.alertError(requireContext(), message, isError)
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(data: Data) {

        lastAlertFat=""
        lastAlertCarb=""
        lastAlertProtin=""

        if (data.macros!=null && !data.macros.equals("null",true)){
            binding.tvHighProtein.text = data.macros
            marcoType=data.macros.toString()
        }

        hideIcon()

        val matchedActivity = listActivity.find { it.title == binding.tvHighProtein.text.toString() }

        matchedActivity?.let {
            binding.tvTitle.text=it.description
        }

        if (data.calories!=null){
            calories= data.calories ?: 0
            binding.seekbarcalories.progress = data.calories ?: 0
        }else{
            calories= 0
            binding.seekbarcalories.progress =  0
        }

        data.macro_options?.let {
            macroOption=it
        }

        data.disclaimer?.let {
            disclaimer=it
        }

        data.macro_per?.fat?.let {
            binding.seekbarFats.progress = it
            fat=it
        }

        data.macro_per?.protein?.let {
            binding.seekbarProtein.progress = it
            protine=it
        }

        data.macro_per?.carbs?.let {
            binding.seekbarCarbs.progress = it
            carb=it
        }

    }

    private fun hideIcon(){
        if (binding.tvHighProtein.text.toString().equals("Custom",true)){
            binding.imgInfo.visibility=View.GONE
        }else{
            binding.imgInfo.visibility=View.VISIBLE
        }
    }

    private fun hideRefresh(){
        binding.imgCalRefresh.visibility=View.GONE
        binding.imgCarbRefresh.visibility=View.GONE
        binding.imgFatRefresh.visibility=View.GONE
        binding.imgProteinRefresh.visibility=View.GONE
    }

    @SuppressLint("SetTextI18n")
    private fun initializeUI() {

        binding.imageBackNutrition.setOnClickListener {
            findNavController().navigateUp()
        }

        if (sessionManagement.getUserName()!=null && sessionManagement.getUserName().equals("null")){
            binding.tvName.text=BaseApplication.capitalLatter(sessionManagement.getUserName().toString())+"’s Nutrition Goals"
        }

        binding.tvHighProtein.setOnClickListener {
            openDialogActivityLevel()
        }

        binding.imgInfo.setOnClickListener {
            if (!binding.tvHighProtein.text.equals("Custom")) {
                openInfoBox()
            }

        }


        setSeekBarValue()

        setupUpdateButton()
    }

    @SuppressLint("SetTextI18n")
    private fun openInfoBox(){

        val dialogWeight = BottomSheetDialog(requireContext(),R.style.BottomSheetDialog)
        dialogWeight.setContentView(R.layout.alert_dialog_info)
        dialogWeight.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogWeight.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogWeight.window?.setGravity(Gravity.BOTTOM)

        val tvTitle=dialogWeight.findViewById<TextView>(R.id.tvTitle)
        val tvMacro=dialogWeight.findViewById<TextView>(R.id.tvMacro)
        val tvName=dialogWeight.findViewById<TextView>(R.id.tvName)
        val img=dialogWeight.findViewById<ImageView>(R.id.img)
        val tvDisclaimer=dialogWeight.findViewById<TextView>(R.id.tvDisclaimer)

        macroOption.let {
            tvMacro?.text=it
        }

        disclaimer.let {
            tvDisclaimer?.text=it
        }

        val bottomSheet = dialogWeight.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isHideable = true // Prevent swipe down to hide
            behavior.state = BottomSheetBehavior.STATE_EXPANDED // Fully expand
            behavior.skipCollapsed = true
        }


        tvTitle?.text=binding.tvHighProtein.text.toString()
        tvName?.text= binding.tvHighProtein.text.toString()+" Macro Distribution"

        val dietType = binding.tvHighProtein.text.toString()
        val imageRes = when (dietType) {
            "Balanced" -> R.drawable.balanced_img
            "Low Carb" -> R.drawable.low_carb_img
            "High Protein" -> R.drawable.high_protein_img
            "Keto" -> R.drawable.keto_img
            "Low Fat" -> R.drawable.low_fat_img
            else -> null
        }

        imageRes?.let {
            img?.let {
                Glide.with(requireContext())
                    .load(imageRes)
                    .into(it)
            }
        }

        dialogWeight.show()
    }


    private fun openDialogActivityLevel() {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater?
        val popupView: View? = inflater?.inflate(R.layout.item_show_data_health, null)
        popupWindowActivityLevel  = PopupWindow(popupView, binding.tvHighProtein.width, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
        popupWindowActivityLevel.showAsDropDown(binding.tvHighProtein,  0, 0, Gravity.CENTER)
        // Access views inside the inflated layout using findViewById
        val rcyActivity = popupView?.findViewById<RecyclerView>(R.id.rcyActivity)



        val selectedDate = binding.tvHighProtein.text.toString()
        listActivity.forEach { dataPerWeek ->
            dataPerWeek.is_selected = if (dataPerWeek.title == selectedDate) 1 else 0
        }
        adapterHealthActivity=HelthActivityAdapter(listActivity,requireActivity(),this)
        rcyActivity?.adapter=adapterHealthActivity
    }

    private fun setSeekBarValue() {

        binding.seekbarFats.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (binding.tvHighProtein.text.toString().trim().equals("Custom",true)){
                    seekBar?.progress = progress
                }else{
                    seekBar?.progress = progress
                }
                binding.tvfat.text=""+seekBar?.progress+"%"
                val gram= seekBar?.progress?.let { calculateGram(binding.seekbarcalories.progress, it,9) }
                binding.textFatTotal.text="("+gram+"g)"
                totalCount()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val dataList=listActivity.last()
                binding.tvHighProtein.text = dataList.title
                binding.tvTitle.text =dataList.description
                hideIcon()
                binding.seekbarFats.isEnabled=true
                binding.imgFatRefresh.visibility=View.VISIBLE
                if (lastAlertFat.equals("",true)){
                    if (binding.seekbarFats.progress==0){
                        alertMacros("Fat")
                    }else{
                        if (binding.seekbarFats.progress==0 && binding.seekbarProtein.progress < 10){
                            alertMacros("Fat")
                        }else{
                            binding.seekbarFats.isEnabled=true
                        }
                    }
                }else{
                    binding.seekbarFats.isEnabled=true
                }
            }
        })
        binding.seekbarCarbs.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (binding.tvHighProtein.text.toString().trim().equals("Custom",true)){
                    seekBar?.progress = progress
                }else {
                    seekBar?.progress = progress
                }
                binding.tvcarb.text=""+seekBar?.progress+"%"
                val gram= seekBar?.progress?.let { calculateGram(binding.seekbarcalories.progress, it,4) }
                binding.textCarbsTotal.text="("+gram+"g)"
                totalCount()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val dataList=listActivity.last()
                binding.tvHighProtein.text = dataList.title
                binding.tvTitle.text =dataList.description
                hideIcon()
                binding.seekbarCarbs.isEnabled=true
                binding.imgCarbRefresh.visibility=View.VISIBLE
                if (lastAlertCarb.equals("",true)){
                    if (binding.seekbarCarbs.progress==0){
                        alertMacros("Carb")
                    }else{
                        if (binding.seekbarCarbs.progress==0 && binding.seekbarProtein.progress < 10){
                            alertMacros("Carb")
                        }else{
                            binding.seekbarCarbs.isEnabled=true
                        }
                    }
                }else{
                    binding.seekbarCarbs.isEnabled=true
                }

            }
        })
        binding.seekbarProtein.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Enforce the minimum value constraint
                if (binding.tvHighProtein.text.toString().trim().equals("Custom",true)){
                    seekBar?.progress = progress
                }else {
                    seekBar?.progress = progress
                }
                binding.tvprotin.text=""+seekBar?.progress+"%"
                val gram= seekBar?.progress?.let { calculateGram(binding.seekbarcalories.progress, it,4) }
                binding.textProtienTotal.text="("+gram+"g)"
                totalCount()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Optional: Do something when touch starts
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val dataList=listActivity.last()
                binding.tvHighProtein.text = dataList.title
                binding.tvTitle.text =dataList.description
                hideIcon()
                binding.seekbarProtein.isEnabled=true
                binding.imgProteinRefresh.visibility=View.VISIBLE
                if (lastAlertProtin.equals("",true)){
                    if (binding.seekbarProtein.progress==0){
                        alertMacros("Protein")
                    }else{
                        if ((binding.seekbarFats.progress==0 || binding.seekbarCarbs.progress==0) && binding.seekbarProtein.progress < 10){
                            alertMacros("Protein")
                        }else{
                            binding.seekbarProtein.isEnabled=true
                        }
                    }
                }else{
                    binding.seekbarProtein.isEnabled=true
                }

            }
        })
        binding.seekbarcalories.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Nutrition calculations
                val gramFat = calculateGram(progress, binding.seekbarFats.progress, 9)
                val gramCarbs = calculateGram(progress, binding.seekbarCarbs.progress, 4)
                val gramProtein = calculateGram(progress, binding.seekbarProtein.progress, 4)
                binding.textCalories.text = "$progress"
                binding.textFatTotal.text = "(${gramFat}g)"
                binding.textCarbsTotal.text = "(${gramCarbs}g)"
                binding.textProtienTotal.text = "(${gramProtein}g)"

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                binding.imgCalRefresh.visibility=View.VISIBLE
                if (!binding.tvHighProtein.text.equals("Custom")){
                    val increaseLimit = (calories * 1.15).toInt()
                    val decreaseLimit = (calories * 0.75).toInt()
                    val calories = seekBar?.progress ?: 0
                    // Show alert only once per threshold breach
                    if (calories > increaseLimit) {
                        if (!alertShown || lastAlertType != "increase") {
                            alertShown = true
                            lastAlertType = "increase"
                            if (!isSeekAllowed) {
                                isSeekAllowed=true
                                binding.seekbarcalories.isEnabled=true
                                openBox("Heads up")
                            }
                            return
                        }
                    } else if (calories < decreaseLimit) {
                        if (!alertShown || lastAlertType != "decrease") {
                            alertShown = true
                            lastAlertType = "decrease"
                            if (!isSeekAllowed) {
                                isSeekAllowed=true
                                binding.seekbarcalories.isEnabled=true
                                openBox("Heads up")
                            }
                            return
                        }
                    } else {
                        // Reset alert state and show safe range toast only once
                        if (!alertShown || lastAlertType != "safe") {
                            alertShown = true
                            lastAlertType = "safe"
                            if (!isSeekAllowed) {
                                isSeekAllowed = true
                                binding.seekbarcalories.isEnabled = true
                                openBox("Custom calories")
                            }
                            return
                        }
                    }
                }
            }
        })



        binding.imgCalRefresh.setOnClickListener {
            binding.imgCalRefresh.visibility=View.GONE
            isSeekAllowed = false
            alertShown = false
            lastAlertType = null
            binding.seekbarcalories.progress=calories
            setMacroType()
        }
        binding.imgFatRefresh.setOnClickListener {
            binding.imgFatRefresh.visibility=View.GONE
            binding.seekbarFats.progress=fat
            lastAlertFat=""
            setMacroType()
        }
        binding.imgCarbRefresh.setOnClickListener {
            binding.imgCarbRefresh.visibility=View.GONE
            binding.seekbarCarbs.progress=carb
            lastAlertCarb=""
            setMacroType()
        }
        binding.imgProteinRefresh.setOnClickListener {
            binding.imgProteinRefresh.visibility=View.GONE
            binding.seekbarProtein.progress=protine
            lastAlertProtin=""
            setMacroType()
        }

    }

    private fun setMacroType(){
        if (binding.imgCalRefresh.visibility == View.GONE &&
            binding.imgFatRefresh.visibility == View.GONE &&
            binding.imgCarbRefresh.visibility == View.GONE &&
            binding.imgProteinRefresh.visibility == View.GONE){
            val dataList=listActivity.find { it.title == marcoType}
            binding.tvHighProtein.text = dataList?.title
            binding.tvTitle.text =dataList?.description
        }
        hideIcon()
    }

    @SuppressLint("SetTextI18n")
    private fun alertMacros(typeStats: String){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.alert_dialog_health_popup)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Set width and height
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvCancel = dialog.findViewById<TextView>(R.id.tvCancel)
        val tvContinue = dialog.findViewById<TextView>(R.id.tvContinue)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val tvDes = dialog.findViewById<TextView>(R.id.tvDes)

        tvTitle.text="Macros"

        tvDes.text="The calorie goal you’ve entered is outside recommended limits and beyond what’s considered safe. Making extreme changes is at your own risk and may affect your health. If you’re unsure, please speak with a doctor or dietitian before continuing."
        tvCancel.text="Cancel"
        tvContinue.text="Continue"

        tvContinue.setBackgroundResource(R.drawable.outline_green_border_nutrition)
        tvContinue.setTextColor(Color.parseColor("#FE9F45"))

        tvCancel.setOnClickListener {
            dialog.dismiss()
            if (typeStats.equals("Fat",true)){
                lastAlertFat=""
                binding.seekbarFats.progress=fat
                binding.seekbarFats.isEnabled=true
                binding.imgFatRefresh.visibility = View.GONE
            }
            if (typeStats.equals("Carb",true)){
                lastAlertCarb=""
                binding.seekbarCarbs.progress=carb
                binding.seekbarCarbs.isEnabled=true
                binding.imgCarbRefresh.visibility = View.GONE
            }

            if (typeStats.equals("Protein",true)){
                lastAlertProtin=""
                binding.seekbarProtein.progress=carb
                binding.seekbarProtein.isEnabled=true
                binding.imgProteinRefresh.visibility = View.GONE
            }
        }

        tvContinue.setOnClickListener {
            dialog.dismiss()
            if (typeStats.equals("Fat",true)){
                lastAlertFat="Fat"
                binding.seekbarFats.progress=binding.seekbarFats.progress
                binding.seekbarFats.isEnabled=true
            }
            if (typeStats.equals("Carb",true)){
                lastAlertCarb="Carb"
                binding.seekbarCarbs.progress=binding.seekbarCarbs.progress
                binding.seekbarCarbs.isEnabled=true
            }

            if (typeStats.equals("Protein",true)){
                lastAlertProtin="Protein"
                binding.seekbarProtein.progress=binding.seekbarProtein.progress
                binding.seekbarProtein.isEnabled=true
            }
        }

        dialog.show()
    }

    @SuppressLint("SetTextI18n")
    private fun openBox(typeStats:String){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.alert_dialog_health_popup)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Set width and height
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val tvCancel = dialog.findViewById<TextView>(R.id.tvCancel)
        val tvContinue = dialog.findViewById<TextView>(R.id.tvContinue)
        val tvTitle = dialog.findViewById<TextView>(R.id.tvTitle)
        val tvDes = dialog.findViewById<TextView>(R.id.tvDes)

        tvTitle.text=typeStats
        if (typeStats.equals("Custom calories",true)){
            tvDes.text=ErrorMessage.caloriesSms
            tvCancel.text="Restore Plan"
            tvContinue.text="Proceed anyway"
        }else{
            tvDes.text=ErrorMessage.HeadsSms
            tvCancel.text="Cancel"
            tvContinue.text="Continue"
        }

        tvContinue.setBackgroundResource(R.drawable.outline_green_border_nutrition)
        tvContinue.setTextColor(Color.parseColor("#FE9F45"))

        tvCancel.setOnClickListener { dialog.dismiss()
            isSeekAllowed=false
            binding.seekbarcalories.isEnabled=true
            calStatus="0"
            binding.seekbarcalories.progress=calories
            binding.imgCalRefresh.visibility = View.GONE
            alertShown = false
            lastAlertType = null
        }

        tvContinue.setOnClickListener {
            dialog.dismiss()
            isSeekAllowed=true
            binding.seekbarcalories.isEnabled=true
            val dataList = listActivity.last()
            binding.tvHighProtein.text = dataList.title
            binding.tvTitle.text = dataList.description
            hideIcon()
            calStatus="1"
        }

        dialog.show()
    }

    private fun calculateGram(calorieTarget: Int, percentage: Int, divide: Int): Int {
        val totalGram = (calorieTarget * percentage / 100.0) / divide
        return totalGram.roundToInt()
    }

    @SuppressLint("SetTextI18n")
    private fun totalCount(){
        val total =  binding.seekbarFats.progress + binding.seekbarCarbs.progress + binding.seekbarProtein.progress
        binding.tvTotal.text= "$total%"
        if (total == 100){
            binding.rlUpdateButton.isEnabled = true
            binding.rlUpdateButton.setBackgroundResource(R.drawable.button_bg)
            binding.tvTotal.setTextColor(Color.parseColor("#000000"))
        }else{
            binding.rlUpdateButton.isEnabled = false
            binding.rlUpdateButton.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            binding.tvTotal.setTextColor(Color.parseColor("#FE9F45"))
        }
    }

    private fun setupUpdateButton() {
        binding.rlUpdateButton.setOnClickListener {
            if (binding.seekbarcalories.progress>=1200){
                viewModel.getProfileData()?.let { data ->
                    data.apply {
                        val fatPercentage=binding.tvfat.text.toString().replace("%","").trim()
                        val carbsPercentage=binding.tvcarb.text.toString().replace("%","").trim()
                        val proteinPercentage=binding.tvprotin.text.toString().replace("%","").trim()
                        val macroPer= Macroper(carbsPercentage.toInt(),fatPercentage.toInt(),proteinPercentage.toInt())
                        val fatGram = extractGrams(binding.textFatTotal.text.toString())
                        val fatCarbs = extractGrams(binding.textCarbsTotal.text.toString())
                        val fatProtein = extractGrams(binding.textProtienTotal.text.toString())
                        typeStatus = if (isSeekAllowed){
                            "1"
                        }else{
                            "0"
                        }
                        if (typeStatus.equals("0")){
                            macros=binding.tvHighProtein.text.toString()
                            old_macro=macros
                        }else{
                            old_macro=macros
                            macros=binding.tvHighProtein.text.toString()
                        }
                        fat = fatGram
                        carbs = fatCarbs
                        calories = binding.seekbarcalories.progress
                        protein = fatProtein
                        macro_per=macroPer
                        val totalCount = binding.seekbarcalories.progress +
                                (fatCarbs ?: 0) +
                                (fatGram ?: 0) +
                                (fatProtein ?: 0)
                        sessionManagement.setCountHealth(totalCount)
                    }
                    viewModel.setProfileData(data)

                    findNavController().navigateUp()
                }
            }else{
                Toast.makeText(requireContext(),"Alert: Minimum 1200 calories required.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to extract number from string like "(123g)"
    private fun extractGrams(text: String): Int? {
        val regex = Regex("\\((\\d+)g\\)")
        return regex.find(text)?.groups?.get(1)?.value?.toIntOrNull()
    }

    override fun itemClicked(position: Int?, list: MutableList<String>?, status: String?, type: String?) {
        if (BaseApplication.isOnline(requireActivity())) {
            popupWindowActivityLevel.dismiss()
            val dataItem = listActivity[position!!]
            binding.tvHighProtein.text = dataItem.title
            binding.tvTitle.text =dataItem.description
            isSeekAllowed=false
            alertShown = false
            lastAlertType = null
            calStatus="0"
            hideIcon()
            if (!binding.tvHighProtein.text.toString().equals("Custom",true)){
                logicBMR()
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


    private fun selectGender(isMale: Boolean) {
        genderType = if (isMale) {
            "male"
        } else {
            "female"
        }
    }
    private fun logicBMR() {
        BaseApplication.showMe(requireContext())
        val data = viewModel.getProfileData()
        data?.gender?.let {
            when (it.lowercase()) {
                "male" -> selectGender(true)
                "female" -> selectGender(false)
            }
        }
        val heightLocal=data?.height
        val tvActivityLevel=data?.activity_level
        val tvTargetdate=data?.target
        val dob=data?.dob
        val weightLocal=data?.weight
        val targetWeightLocal=data?.target_weight
        val oldMacro=data?.old_macro
        val heightType= if (data?.height_type?.contains("cm") == true){
            "cm"
        }else{
            "feet"
        }
        val weightType= if (data?.weight_type?.contains("lb") == true){
            "lb"
        }else{
            "Kilograms"
        }
        val targetWeightType= if (data?.target_weight_type?.contains("lb") == true){
            "lb"
        }else{
            "Kilograms"
        }
        val weight= weightLocal?.replace("lb", "")?.replace("kg", "")
        val targetWeight= targetWeightLocal?.replace("lb", "")?.replace("kg", "")
        val height= if (heightType.equals("feet",true)){
            convertToDecimalFeet(heightLocal.toString()).toString()
        }else{
            heightLocal?.replace("cm", "")
        }
        val fatPercentage=binding.tvfat.text.toString().replace("%","").trim()
        val carbsPercentage=binding.tvcarb.text.toString().replace("%","").trim()
        val proteinPercentage=binding.tvprotin.text.toString().replace("%","").trim()
        val calories=if (binding.tvHighProtein.text.toString().equals("Custom",true)){
            binding.seekbarcalories.progress.toString()
        }else{
            ""
        }
        val fatPer=if (binding.tvHighProtein.text.toString().equals("Custom",true)){
            fatPercentage
        }else{
            ""
        }
        val proteinPer=if (binding.tvHighProtein.text.toString().equals("Custom",true)){
            proteinPercentage
        }else{
            ""
        }
        val carbsPer=if (binding.tvHighProtein.text.toString().equals("Custom",true)){
            carbsPercentage
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
                handleApiUpdateResponse(it,oldMacro)},
                genderType,
                dob,
                height,
                heightType,
                weight,
                weightType,
                tvActivityLevel,
                targetWeight,
                targetWeightType,
                tvTargetdate,
                binding.tvHighProtein.text.toString(),
                calories,fatPer,proteinPer,carbsPer,"0"
            )
        }
    }

    private fun handleApiUpdateResponse(result: NetworkResult<String>,oldMacro:String?) {
        when (result) {
            is NetworkResult.Success -> handleUpdateSuccessResponse(result.data.toString(),oldMacro)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleUpdateSuccessResponse(data: String,oldMacro:String?) {
        try {
                val apiModel = Gson().fromJson(data, ApiModelBMR::class.java)
                Log.d("@@@ BMR profile", "message :- $data")
                if (apiModel.code == 200 && apiModel.success) {
                    hideRefresh()
                    apiModel.data?.let { dataModel->
                        val dataModel=Data("","",dataModel.calories,
                            dataModel.carbs,"",dataModel.fat,"","",
                            "","","","",
                            "",dataModel.target_weight_type,"","",dataModel.protein,
                            dataModel.goal_in_weeks,dataModel.target_weight,"",dataModel.time,
                            dataModel.value_per_week,binding.tvHighProtein.text.toString(),oldMacro,dataModel.macro_per,dataModel.macro_options,dataModel.disclaimer,dataModel.data_per_week)
                        updateUI(dataModel)
                        setSeekBarValue()
                    }
                } else {
                    handleError(apiModel.code,apiModel.message)
                }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
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


}
