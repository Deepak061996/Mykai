package com.mykaimeal.planner.fragment.mainfragment.hometab.fullcookedScheduleFragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipDescription
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.DragEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemLongClickListener
import com.mykaimeal.planner.OnItemSelectUnSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.CalendarDayAdapter
import com.mykaimeal.planner.adapter.CalendarDayDateAdapter
import com.mykaimeal.planner.adapter.IngredientsBreakFastAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.databinding.FragmentFullCookedScheduleBinding
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.Breakfast
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.CookedTabModel
import com.mykaimeal.planner.fragment.mainfragment.cookedtab.cookedfragment.model.CookedTabModelData
import com.mykaimeal.planner.fragment.mainfragment.hometab.fullcookedScheduleFragment.viewmodel.FullCookingScheduleViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DateModel
import com.skydoves.powerspinner.PowerSpinnerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class FullCookedScheduleFragment : Fragment(), OnItemSelectUnSelectListener,
    OnItemLongClickListener {

    private lateinit var binding: FragmentFullCookedScheduleBinding
    private var ingredientBreakFastAdapter: IngredientsBreakFastAdapter? = null
    private var ingredientLunchAdapter: IngredientsBreakFastAdapter? = null
    private var ingredientDinnerAdapter: IngredientsBreakFastAdapter? = null
    private var ingredientSnacksAdapter: IngredientsBreakFastAdapter? = null
    private var ingredientTeaTimeAdapter: IngredientsBreakFastAdapter? = null
    private var ingredientDessertAdapter: IngredientsBreakFastAdapter? = null
    private var tvWeekRange: TextView? = null
    private var calendarDayAdapter: CalendarDayAdapter? = null
    private var calendarAdapter: CalendarDayDateAdapter? = null

    private var recipesDateModel: CookedTabModelData? = null
    private lateinit var fUllCookingScheduleViewModel: FullCookingScheduleViewModel
    private lateinit var sessionManagement: SessionManagement
    private lateinit var commonWorkUtils: CommonWorkUtils

    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private var dropDate: String? = ""
    private var id: String? = "null"
    private var uri: String? = ""
    private var mealType: String? = ""
    private var dropDay: String? = null
    private var currentDate = Date() // Current date
    private var currentDateSelected: String = ""
    private lateinit var spinnerActivityLevel: PowerSpinnerView
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private var updatedDaysBetween: List<DateModel> = emptyList()
    private var lastDateSelected: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentFullCookedScheduleBinding.inflate(inflater, container, false)

        (activity as? MainActivity)?.binding?.let { binding ->
            binding.llIndicator.visibility = View.VISIBLE
            binding.llBottomNavigation.visibility = View.VISIBLE
        }

        binding.rlChangeCookSchedule.isClickable = false
        binding.rlChangeCookSchedule.isEnabled = false

        sessionManagement = SessionManagement(requireContext())
        commonWorkUtils = CommonWorkUtils(requireContext())
        fUllCookingScheduleViewModel = ViewModelProvider(requireActivity())[FullCookingScheduleViewModel::class.java]

        backButton()

        cookbookList.clear()

        val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("", "", 0, "", "Favorites", 0, "", 0)
        cookbookList.add(0, data)

        if (sessionManagement.getUserName() != null) {
            binding.tvName.text = BaseApplication.capitalLatter(sessionManagement.getUserName().toString()) + "'s week"
        }

        fUllCookingScheduleViewModel.date?.let {
           lastDateSelected=it
            currentDateSelected=it
            currentDate= BaseApplication.parseToDate(it)!!
        }?:run {
            currentDateSelected = BaseApplication.currentDateFormat().toString()
            lastDateSelected=currentDateSelected
            currentDate= BaseApplication.parseToDate(currentDateSelected)!!
        }

        fUllCookingScheduleViewModel.data?.let {
            showDataAccordingDate(it)
        }?:run {
            loadApi("1")
        }

        onClickFalseEnabled()

        initialize()

        // Display current week dates
        showWeekDates()

        return binding.root
    }

    private fun loadApi(type:String){
        if (BaseApplication.isOnline(requireActivity())) {
            if (type.equals("1",true)){
                currentDateSelected=""
            }
            dataFetchByDate(currentDateSelected, "1")
        } else {
            hideData()
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    @SuppressLint("SetTextI18n")
    fun showWeekDates() {
        Log.d("currentDate :- ", "******$currentDate")
        Log.d("lastDateSelected :- ", "******$lastDateSelected")
        // Get the start and end dates of the week
        val (startDate, endDate) = getWeekDates(currentDate)
        this.startDate = startDate
        this.endDate = endDate

        // Get all dates between startDate and endDate
        val daysBetween = getDaysBetween(startDate, endDate)

        // Mark the current date as selected in the list
        updatedDaysBetween = daysBetween.map { dateModel ->
            dateModel.apply {
                status = (date == lastDateSelected) // Compare formatted strings
            }
        }
        // Print the dates for debugging
        println("Days between $startDate and $endDate:")
        updatedDaysBetween.forEach { println(it) }
        binding.tvCustomDates.text = "${formatDate(startDate)} - ${formatDate(endDate)}"
        binding.textWeekRange.text = "${formatDate(startDate)} - ${formatDate(endDate)}"
        tvWeekRange?.text = "${formatDate(startDate)} - ${formatDate(endDate)}"

        // Initialize the adapter with the updated date list
        calendarAdapter = CalendarDayDateAdapter(updatedDaysBetween.toMutableList()) { selectedPosition ->
                // Update the list to reflect the selected date
                updatedDaysBetween.forEachIndexed { index, dateModel ->
                    dateModel.status = (index == selectedPosition)
                    lastDateSelected=updatedDaysBetween[selectedPosition].date
                }

                Log.d("Date ", "*****$updatedDaysBetween")

                // Notify the adapter to refresh the data
                calendarAdapter?.updateList(updatedDaysBetween.toMutableList())

                // Update the current date selection
                currentDateSelected = updatedDaysBetween[selectedPosition].date
                binding.rlChangeCookSchedule.isClickable = false
                binding.rlChangeCookSchedule.isEnabled = false
                dropDate=""
                dropDay=""
                binding.rlChangeCookSchedule.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                // Fetch data for the selected date if online
                if (BaseApplication.isOnline(requireActivity())) {
                    dataFetchByDate(currentDateSelected, "1")
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }

        // Update the RecyclerView with the adapter
        binding.recyclerViewWeekDays.adapter = calendarAdapter

        binding.recyclerViewWeekDays.setOnDragListener { view, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Accept the drag only if the MIME type matches
                    dragEvent.clipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Optional: Highlight the RecyclerView background
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val recyclerView = view as RecyclerView // Cast the view to RecyclerView
                    val childView = recyclerView.findChildViewUnder(dragEvent.x, dragEvent.y) // Call findChildViewUnder on RecyclerView

                    val targetPosition = if (childView != null) {
                        recyclerView.getChildAdapterPosition(childView) // Get the position of the child
                    } else {
                        RecyclerView.NO_POSITION // Return NO_POSITION if no child is found
                    }

                    if (targetPosition != RecyclerView.NO_POSITION) {
                        if (getDaysBetween(startDate, endDate)[targetPosition].date>=BaseApplication.currentDateFormat().toString()){
                            updatedDaysBetween = daysBetween.mapIndexed { index, dateModel ->
                                dateModel.apply {
                                    status = (index == targetPosition) // Change status based on position
                                    lastDateSelected=updatedDaysBetween[targetPosition].date
                                }
                            }
                            Log.d("lastDateSelected","******"+lastDateSelected)
                            // Notify the adapter to refresh the changed position
                            calendarAdapter!!.updateList(updatedDaysBetween.toMutableList())
                            calendarAdapter!!.notifyItemChanged(targetPosition)
                            /*calendarAdapter!!.updateList(dateList)*/
                        }
                    } else {
                        Log.d("date position ", "No valid position under drag location")
                    }
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    // Optional: Reset background or remove highlight
//                    view.setBackgroundColor(Color.WHITE)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    // Find the target position in RecyclerView
                    val recyclerView = view as RecyclerView // Cast the view to RecyclerView
                    val dropX = dragEvent.x // X-coordinate of the drop
                    val dropY = dragEvent.y // Y-coordinate of the drop
                    val childView = recyclerView.findChildViewUnder(
                        dropX,
                        dropY
                    ) // Find the child view under the drop position

                    if (childView != null) {
                        // Get the adapter position of the target item
                        val dropPosition = recyclerView.getChildAdapterPosition(childView)

                        if (dropPosition != RecyclerView.NO_POSITION) {
                            Log.d("ACTION_DROP", "Item dropped at position: $dropPosition")
                            if (getDaysBetween(startDate, endDate)[dropPosition].date>=BaseApplication.currentDateFormat().toString()){
                                dropDate = getDaysBetween(startDate, endDate)[dropPosition].date
                                /*dropMealType*/
                                dropDay = getDaysBetween(startDate, endDate)[dropPosition].day
                                Log.d("ACTION_DROP", "*******$dropPosition")
                                Log.d("date position ", "******" + getDaysBetween(startDate, endDate)[dropPosition].date)
                                Log.d("drop date and days", "******" + getDaysBetween(startDate, endDate)[dropPosition].date + "-" + getDaysBetween(startDate, endDate)[dropPosition].day)
                                Log.d("ACTION_DROP", "Target position: $dropPosition")
                                binding.rlChangeCookSchedule.isClickable = true
                                binding.rlChangeCookSchedule.isEnabled = true
                                binding.rlChangeCookSchedule.setBackgroundResource(R.drawable.gray_btn_select_background)
                                updatedDaysBetween = daysBetween.mapIndexed { index, dateModel ->
                                    dateModel.apply {
                                        status = (index == dropPosition) // Change status based on position
                                        lastDateSelected=updatedDaysBetween[dropPosition].date
                                        dropDate=updatedDaysBetween[dropPosition].date
                                    }
                                }
                                // Notify the adapter to refresh the changed position
                                calendarAdapter!!.updateList(updatedDaysBetween.toMutableList())
                                // Optional: Notify the source RecyclerView to remove the dragged item
                            }
                        } else {
                            Log.d("ACTION_DROP", "No valid drop position found")
                        }
                    } else {
                        Log.d("ACTION_DROP", "Dropped outside valid child views")
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    // Reset background or clean up
//                    view.setBackgroundColor(Color.WHITE)
                    true
                }

                else -> false
            }
        }

    }

    private fun dataFetchByDate(date: String, status: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            fUllCookingScheduleViewModel.fullCookingSchedule({
                BaseApplication.dismissMe()
                if (status.equals("2",true)) {
                    binding.rlChangeCookSchedule.isClickable = false
                    binding.rlChangeCookSchedule.isEnabled = false
                    binding.rlChangeCookSchedule.setBackgroundResource(R.drawable.gray_btn_unselect_background)
                    // Display current week dates
                    showWeekDates()
                }
                handleApiPlanDateResponse(it)
            }, date, "0")
        }
    }

    private fun handleApiPlanDateResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessPlanDateResponse(result.data.toString())
            is NetworkResult.Error -> {
                hideData()
                showAlert(result.message, false)
            }
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessPlanDateResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookedTabModel::class.java)
            Log.d("@@@ FullSchedule List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null) {
                    showDataAccordingDate(apiModel.data)
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

    private fun handleError(code: Int, message: String) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showDataAccordingDate(data: CookedTabModelData) {
        try {

            recipesDateModel = data

            currentDateSelected = data.date.toString()
            lastDateSelected=currentDateSelected

            BaseApplication.parseToDate(lastDateSelected)?.let { date->
                currentDate=date
            }
            // Display current week dates
            showWeekDates()

            fUllCookingScheduleViewModel.setData(data,lastDateSelected)



            recipesDateModel?.let {

                fun setupMealAdapter(mealRecipes: MutableList<Breakfast>?, recyclerView: RecyclerView, type: String): IngredientsBreakFastAdapter? {
                    return if (mealRecipes != null && mealRecipes.isNotEmpty()) {
                        setupDragScrollForRecyclerView(recyclerView)
                        val adapter = IngredientsBreakFastAdapter(mealRecipes, requireActivity(), this,this, type)
                        recyclerView.adapter = adapter
                        adapter
                    } else {
                        null
                    }
                }

                // Breakfast
                if (it.Breakfast != null && it.Breakfast.isNotEmpty()) {
                    ingredientBreakFastAdapter = setupMealAdapter(it.Breakfast, binding.rcySearchBreakFast, ErrorMessage.Breakfast)
                    binding.llBreakFast.visibility = View.VISIBLE
                } else {
                    binding.llBreakFast.visibility = View.GONE
                }


                // Lunch
                if (it.Lunch != null && it.Lunch.isNotEmpty()) {
                    ingredientLunchAdapter = setupMealAdapter(it.Lunch, binding.rcySearchLunch, ErrorMessage.Lunch)
                    binding.llLunch.visibility = View.VISIBLE
                } else {
                    binding.llLunch.visibility = View.GONE
                }

                // Dinner
                if (it.Dinner != null && it.Dinner.isNotEmpty()) {
                    ingredientDinnerAdapter = setupMealAdapter(it.Dinner, binding.rcySearchDinner, ErrorMessage.Dinner)
                    binding.llDinner.visibility = View.VISIBLE
                } else {
                    binding.llDinner.visibility = View.GONE
                }

                // Snacks
                if (it.Snacks != null && it.Snacks.isNotEmpty()) {
                    ingredientSnacksAdapter = setupMealAdapter(it.Snacks, binding.rcySearchSnacks, ErrorMessage.Snacks)
                    binding.llSnacks.visibility = View.VISIBLE
                } else {
                    binding.llSnacks.visibility = View.GONE
                }

                // Teatime
                if (it.Teatime != null && it.Teatime.isNotEmpty()) {
                    ingredientTeaTimeAdapter = setupMealAdapter(it.Teatime, binding.rcySearchTeaTime, ErrorMessage.Brunch)
                    binding.llTeaTime.visibility = View.VISIBLE
                } else {
                    binding.llTeaTime.visibility = View.GONE
                }

                // Dessert
                if (it.Dessert != null && it.Dessert.isNotEmpty()) {
                    ingredientDessertAdapter = setupMealAdapter(it.Dessert, binding.rcySearchDessert, ErrorMessage.Dessert)
                    binding.llDessert.visibility = View.VISIBLE
                } else {
                    binding.llDessert.visibility = View.GONE
                }


                val count = (it.Breakfast?.count() ?: 0) +
                        (it.Teatime?.count() ?: 0) +
                        (it.Lunch?.count() ?: 0) +
                        (it.Snacks?.count() ?: 0) +
                        (it.Dessert?.count() ?: 0) +
                        (it.Dinner?.count() ?: 0)

                if (count==0){
                    binding.layNoData.visibility=View.VISIBLE
                }else{
                    binding.layNoData.visibility=View.GONE
                }

            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun getDaysBetween(startDate: Date, endDate: Date): MutableList<DateModel> {
        val dateList = mutableListOf<DateModel>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Format for the date
        val dayFormat =
            SimpleDateFormat("EEEE", Locale.getDefault()) // Format for the day name (e.g., Monday)

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        while (!calendar.time.after(endDate)) {
            val date = dateFormat.format(calendar.time)  // Get the formatted date (yyyy-MM-dd)
            val dayName =
                dayFormat.format(calendar.time)  // Get the day name (Monday, Tuesday, etc.)

            val localDate = DateModel()
            localDate.day = dayName
            localDate.date = date
            // Combine both the day name and the date
//            dateList.add("$dayName, $date")
            dateList.add(localDate)

            // Move to the next day
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateList
    }

    private fun getWeekDates(currentDate: Date): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        // Set the calendar to the start of the week (Monday)
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = calendar.time

        // Set the calendar to the end of the week (Saturday)
        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time
        return Pair(startOfWeek, endOfWeek)
    }

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun initialize() {

        binding.relCalendarView.setOnClickListener {
            openDialog()
        }

        // Initialize RecyclerView and Adapter
        setupRecyclerView()

        ///relCookChangeSchedule
        binding.rlChangeCookSchedule.setOnClickListener {
            if (validate()) {
                if (!dropDate.equals("",true)){
                    removeCurrentDayDialog()
                }
            }
        }


        binding.imagePrevious.setOnClickListener {
            hidPastDate()
        }

        binding.imageNext.setOnClickListener {
            // Simulate clicking the "Next" button to move to the next week
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, 1) // Move to next week
            currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDates()
        }

        binding.rlMainFullCooked.setOnLongClickListener {
            onClickEnabled()
            true
        }

        binding.rlMainFullCooked.setOnClickListener {
            onClickFalseEnabled()
        }

        binding.imgBackCookingSchedule.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun hidPastDate(){
        if (updatedDaysBetween.isNotEmpty()){
            // Define the date format (update to match your `date` string format)
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedCurrentDate = dateFormat.format(currentDate)
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
            val currentDate1 = calendar.time
            val (startDate, endDate) = getWeekDates(currentDate1)
            println("Week Start Date: ${formatDate(startDate)}")
            println("Week End Date: ${formatDate(endDate)}")
            // Get all dates between startDate and endDate
            val daysBetween = getDaysBetween(startDate, endDate)
            // Mark the current date as selected in the list
            val updatedDaysBetween1 = daysBetween.map { dateModel ->
                dateModel.apply {
                    status = (date == formattedCurrentDate) // Compare formatted strings
                }
            }
            var status=false
            updatedDaysBetween1.forEach {
                status = it.date >= BaseApplication.currentDateFormat().toString()
            }
            if (status){
                val calendar = Calendar.getInstance()
                calendar.time = currentDate
                calendar.add(Calendar.WEEK_OF_YEAR, -1) // Move to next week
                currentDate = calendar.time
                // Display next week dates
                println("\nAfter clicking 'Next':")
                showWeekDates()
            }else{
                Toast.makeText(requireContext(),ErrorMessage.slideError, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun removeCurrentDayDialog() {
        val dialogRemoveDay: Dialog = context?.let { Dialog(it) }!!
        dialogRemoveDay.setContentView(R.layout.alert_dialog_current_day)
        dialogRemoveDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogRemoveDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogNoBtn = dialogRemoveDay.findViewById<TextView>(R.id.tvDialogNoBtn)
        val tvDialogYesBtn = dialogRemoveDay.findViewById<TextView>(R.id.tvDialogYesBtn)
        dialogRemoveDay.show()
        dialogRemoveDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogNoBtn.setOnClickListener {

            binding.rlChangeCookSchedule.isClickable = false
            binding.rlChangeCookSchedule.isEnabled = false
            binding.rlChangeCookSchedule.setBackgroundResource(R.drawable.gray_btn_unselect_background)
            // Display current week dates
            showWeekDates()
            dialogRemoveDay.dismiss()
        }

        tvDialogYesBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                updateMealApi(dialogRemoveDay)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    /// add validation based on valid email or phone
    private fun validate(): Boolean {
        // Check if email/phone is empty
        if (id == "") {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.changeScheduleItem, false)
            return false
        } else if (dropDate == "") {
            ///
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.changeScheduleDate, false)
            return false
        }

        return true
    }

    private fun updateMealApi(dialogRemoveDay: Dialog) {

        val jsonObject = JsonObject()

        jsonObject.addProperty("type", mealType)
        if (!id.equals("null")) {
            jsonObject.addProperty("id", id)
            jsonObject.addProperty("date", dropDate)
            jsonObject.addProperty("day", dropDay)
        } else {
            jsonObject.addProperty("uri", uri)
            val jsonArray = JsonArray()
            // Create a JsonObject for each ingredient
            val ingredientObject = JsonObject()
            ingredientObject.addProperty("date", dropDate)
            ingredientObject.addProperty("day", dropDay)
            // Add the ingredient object to the array
            jsonArray.add(ingredientObject)
            // Add the ingredients array to the main JSON object
            jsonObject.add("slot", jsonArray)
        }


        Log.d("json object ", "******$jsonObject")
        BaseApplication.showMe(requireContext())
        if (!id.equals("null")) {
            lifecycleScope.launch {
                fUllCookingScheduleViewModel.updateMealUrl({
                    BaseApplication.dismissMe()
                    handleApiUpdateScheduleResponse(it,dialogRemoveDay)
                }, jsonObject)
            }
        } else {
            lifecycleScope.launch {
                fUllCookingScheduleViewModel.recipeAddToPlanRequestApi({
                    BaseApplication.dismissMe()
                    handleApiUpdateScheduleResponse(it,dialogRemoveDay)
                }, jsonObject)
            }
        }
    }

    private fun handleApiUpdateScheduleResponse(result: NetworkResult<String>, dialogRemoveDay: Dialog) {
        when (result) {
            is NetworkResult.Success -> handleUpdateScheduleResponse(result.data.toString(),dialogRemoveDay)
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleUpdateScheduleResponse(data: String,dialogRemoveDay: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogRemoveDay.dismiss()
                mealType = ""
                id = "null"
                uri = ""
                currentDateSelected=dropDate.toString()
                dataFetchByDate(currentDateSelected, "2")
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun setupRecyclerView() {
        calendarDayAdapter = CalendarDayAdapter(emptyList()) {
            // Handle item clicks if needed
//            Toast.makeText(context, "Clicked on ${day.dayName}, ${day.date}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerViewWeekDays.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewWeekDays.adapter = calendarDayAdapter

        binding.recyclerViewWeekDays.setOnDragListener { view, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    // Accept the drag only if the MIME type matches
                    dragEvent.clipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true
                }

                DragEvent.ACTION_DRAG_ENTERED -> {
                    // Optional: Highlight the RecyclerView background
//                    view.setBackgroundColor(Color.LTGRAY)
                    true
                }

                DragEvent.ACTION_DRAG_LOCATION -> {
                    val recyclerView = view as RecyclerView // Cast the view to RecyclerView
                    val childView = recyclerView.findChildViewUnder(
                        dragEvent.x,
                        dragEvent.y
                    ) // Call findChildViewUnder on RecyclerView
                    val targetPosition = if (childView != null) {
                        recyclerView.getChildAdapterPosition(childView) // Get the position of the child
                    } else {
                        RecyclerView.NO_POSITION // Return NO_POSITION if no child is found
                    }

                    if (targetPosition != RecyclerView.NO_POSITION) {
//                        highlightDropPosition(targetPosition)
                        Log.d("ACTION_DRAG_LOCATION", "Target position: $targetPosition")
                    } else {
                        Log.d("ACTION_DRAG_LOCATION", "No valid position under drag location")
                    }
                    true
                }

                DragEvent.ACTION_DRAG_EXITED -> {
                    // Optional: Reset background or remove highlight
//                    view.setBackgroundColor(Color.WHITE)
                    true
                }
                DragEvent.ACTION_DROP -> {
                    // Retrieve the dragged data
                    // Find the target position in RecyclerView
                    val recyclerView = view as RecyclerView // Cast the view to RecyclerView
                    val dropX = dragEvent.x // X-coordinate of the drop
                    val dropY = dragEvent.y // Y-coordinate of the drop
                    val childView = recyclerView.findChildViewUnder(
                        dropX,
                        dropY
                    ) // Find the child view under the drop position

                    if (childView != null) {
                        // Get the adapter position of the target item
                        val dropPosition = recyclerView.getChildAdapterPosition(childView)

                        if (dropPosition != RecyclerView.NO_POSITION) {
                            Log.d("ACTION_DROP", "Item dropped at position: $dropPosition")
                            Log.d("ACTION_DROP", "*******$dropPosition")
                            // Optional: Notify the source RecyclerView to remove the dragged item
                            // notifyItemRemovedInSource(draggedItem)
                        } else {
                            Log.d("ACTION_DROP", "No valid drop position found")
                        }
                    } else {
                        Log.d("ACTION_DROP", "Dropped outside valid child views")
                    }
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    // Reset background or clean up
                    true
                }

                else -> false
            }
        }
    }


    private fun openDialog() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_calendar)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val calendarView = dialog.findViewById<CalendarView>(R.id.calendar)

        // Disable previous dates
        calendarView.minDate = System.currentTimeMillis()

        // Set previously selected date if available
        lastDateSelected.let {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val date = dateFormat.parse(it)
                calendarView.setDate(date!!.time, true, true)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }

        // Hide navigation arrows
        try {
            val fields = CalendarView::class.java.declaredFields
            for (field in fields) {
                if (field.name == "mNextButton" || field.name == "mPrevButton") {
                    field.isAccessible = true
                    val button = field.get(calendarView) as ImageButton
                    button.isEnabled = false
                    button.visibility = View.INVISIBLE
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        calendarView.setOnDateChangeListener { _: CalendarView?, year: Int, month: Int, dayOfMonth: Int ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val date = calendar.time

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDateString = dateFormat.format(date)

            currentDate = dateFormat.parse(currentDateString)!!
            lastDateSelected = currentDateString
            currentDateSelected = currentDateString

            loadApi("2")
            showWeekDates()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupDragScrollForRecyclerView(recyclerView: RecyclerView) {
        recyclerView.setOnDragListener { _, dragEvent ->
            when (dragEvent.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    binding.scroll.fullScroll(0)
                    true
                }

                DragEvent.ACTION_DROP -> {
                    // Get the dropped item position
                    true
                }

                DragEvent.ACTION_DRAG_ENDED -> {
                    true
                }

                else -> false
            }
        }
    }


    override fun itemSelectUnSelect(id: Int?, status: String?, type: String?, position: Int?) {

        val (mealList,adapter) = when (type) {
            ErrorMessage.Breakfast -> recipesDateModel?.Breakfast to ingredientBreakFastAdapter
            ErrorMessage.Lunch -> recipesDateModel?.Lunch to ingredientLunchAdapter
            ErrorMessage.Dinner -> recipesDateModel?.Dinner to ingredientDinnerAdapter
            ErrorMessage.Snacks -> recipesDateModel?.Snacks to ingredientSnacksAdapter
            ErrorMessage.Brunch -> recipesDateModel?.Teatime to ingredientTeaTimeAdapter
            ErrorMessage.Dessert -> recipesDateModel?.Dessert to ingredientDessertAdapter
            else -> null to null
        }

        // Safely get the item and position
        val item = mealList?.get(position!!)


        if (status.equals("minus",true)){
            removeDayDialog(item,adapter, position,mealList, type)
        }

        if (status.equals("missingIng",true)){
            val bundle = Bundle().apply {
                        putString("uri", item?.recipe?.uri)
                        putString("schId", item?.id.toString())
                        val data= item?.recipe?.mealType?.get(0)?.split(",")
                        val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
                        putString("mealType", formattedFoodName)
                    }
            findNavController().navigate(R.id.missingIngredientsFragment, bundle)
        }

        if (status.equals("recipeDetails",true)){
            val bundle = Bundle().apply {
                        putString("uri", item?.recipe?.uri)
                val data= item?.recipe?.mealType?.get(0)?.split(",")
                val formattedFoodName = data?.get(0)!!.replaceFirstChar { it.uppercase() }
                        putString("mealType", formattedFoodName)
                    }
            findNavController().navigate(R.id.recipeDetailsFragment, bundle)
        }

        if (status.equals("heart",true)){
            if ((activity as? MainActivity)?.Subscription_status==1){
                if ((activity as? MainActivity)?.favorite!! <= 2){
                    if (BaseApplication.isOnline(requireActivity())) {
                        toggleIsLike(item,adapter, position,mealList, type)
                    } else {
                        BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                    }
                }else{
                    (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                }
            }else{
                if (BaseApplication.isOnline(requireActivity())) {
                    toggleIsLike(item,adapter, position,mealList, type)
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
    }

    private fun toggleIsLike(item: Breakfast?, adapter: IngredientsBreakFastAdapter?, position: Int?, mealList: MutableList<Breakfast>?, type: String?) {
        if (item != null) {
            if (item.recipe?.uri != null) {
                val newLikeStatus = if (item.is_like == 0) "1" else "0"
                if (newLikeStatus.equals("0", true)) {
                    recipeLikeAndUnlikeData(item,adapter, type, mealList, position, newLikeStatus, "", null)
                } else {
                    addFavTypeDialog(item, adapter,type, mealList, position, newLikeStatus)
                }
            }
        }
    }

    private fun recipeLikeAndUnlikeData(
        item: Breakfast,
        adapter: IngredientsBreakFastAdapter?,
        type: String?,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        likeType: String,
        cookBookType: String,
        dialogAddRecipe: Dialog?
    ) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            fUllCookingScheduleViewModel.likeUnlikeRequest({
                BaseApplication.dismissMe()
                handleLikeAndUnlikeApiResponse(it, item,adapter, type, mealList, position, dialogAddRecipe)
            }, item.recipe?.uri.toString(), likeType, cookBookType)

        }
    }

    private fun handleLikeAndUnlikeApiResponse(
        result: NetworkResult<String>,
        item: Breakfast,
        adapter: IngredientsBreakFastAdapter?,
        type: String?,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        when (result) {
            is NetworkResult.Success -> handleLikeAndUnlikeSuccessResponse(
                result.data.toString(),
                item,
                adapter,
                type,
                mealList,
                position,
                dialogAddRecipe
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleLikeAndUnlikeSuccessResponse(
        data: String,
        item: Breakfast,
        adapter: IngredientsBreakFastAdapter?,
        type: String?,
        mealList: MutableList<Breakfast>?,
        position: Int?,
        dialogAddRecipe: Dialog?
    ) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Plan List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogAddRecipe?.dismiss()
                // Toggle the is_like value
                item.is_like = if (item.is_like == 0) 1 else 0
                mealList?.set(position!!, item)
                // Update the adapter
                adapter?.updateList(mealList, type)
                (activity as MainActivity?)?.upDateHomeData()
                (activity as MainActivity?)?.upDatePlan()
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun addFavTypeDialog(
        item: Breakfast,adapter: IngredientsBreakFastAdapter?, type: String?,
        mealList: MutableList<Breakfast>?, position: Int?, likeType: String
    ) {
        val dialogAddRecipe: Dialog = context?.let { Dialog(it) }!!
        dialogAddRecipe.setContentView(R.layout.alert_dialog_add_recipe)
        dialogAddRecipe.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogAddRecipe.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogAddRecipe.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        spinnerActivityLevel = dialogAddRecipe.findViewById(R.id.spinnerActivityLevel)
        val relCreateNewCookBook = dialogAddRecipe.findViewById<RelativeLayout>(R.id.relCreateNewCookBook)
        val imgCheckBoxOrange = dialogAddRecipe.findViewById<ImageView>(R.id.imgCheckBoxOrange)

        spinnerActivityLevel.setItems(cookbookList.map { it.name })

        dialogAddRecipe.show()
        dialogAddRecipe.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        getCookBookList()

        relCreateNewCookBook.setOnClickListener {
            relCreateNewCookBook.setBackgroundResource(R.drawable.light_green_rectangular_bg)
            imgCheckBoxOrange.setImageResource(R.drawable.orange_uncheck_box_images)
            dialogAddRecipe.dismiss()
            val bundle = Bundle()
            bundle.putString("value", "New")
            bundle.putString("uri", item.recipe?.uri)
            findNavController().navigate(R.id.createCookBookFragment, bundle)
        }


        rlDoneBtn.setOnClickListener {
            if (spinnerActivityLevel.text.toString().equals("", true)) {
                BaseApplication.alertError(requireContext(), ErrorMessage.selectCookBookError, false)
            } else {
                val cookBookType = cookbookList[spinnerActivityLevel.selectedIndex].id
                recipeLikeAndUnlikeData(item, adapter,type, mealList, position, likeType, cookBookType.toString(), dialogAddRecipe)
            }
        }
    }

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            fUllCookingScheduleViewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }

    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    cookbookList.retainAll { it == cookbookList[0] }
                    cookbookList.addAll(apiModel.data)
                    // OR directly modify the original list
                    spinnerActivityLevel.setItems(cookbookList.map { it.name })
                }
            } else {
                if (apiModel.code == ErrorMessage.code) {
                    showAlert(apiModel.message, true)
                } else {
                    showAlert(apiModel.message, false)
                }
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun removeDayDialog(item: Breakfast?, adapter: IngredientsBreakFastAdapter?, position: Int?, mealList: MutableList<Breakfast>?, type: String?) {
        val dialogScheduleDay: Dialog = context?.let { Dialog(it) }!!
        dialogScheduleDay.setContentView(R.layout.alert_dialog_remove_day)
        dialogScheduleDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogScheduleDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogNoBtn = dialogScheduleDay.findViewById<TextView>(R.id.tvDialogNoBtn)
        val tvDialogYesBtn = dialogScheduleDay.findViewById<TextView>(R.id.tvDialogYesBtn)
        dialogScheduleDay.show()
        dialogScheduleDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogNoBtn.setOnClickListener {
            dialogScheduleDay.dismiss()
        }

        tvDialogYesBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                removeCookBookApi(item,adapter, dialogScheduleDay, position,mealList, type)
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun removeCookBookApi(item: Breakfast?, adapter: IngredientsBreakFastAdapter?, dialogScheduleDay: Dialog, position: Int?, mealList: MutableList<Breakfast>?, type: String?) {
        BaseApplication.showMe(requireActivity())
        lifecycleScope.launch {
            fUllCookingScheduleViewModel.removeMealApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        val gson = Gson()
                        val cookedModel = gson.fromJson(it.data, CookedTabModel::class.java)
                        if (cookedModel.code == 200 && cookedModel.success) {
                            // Remove item from the list
                            mealList?.removeAt(position!!)
                            // Define meal types and corresponding UI elements
                            val mealVisibilityMap = mapOf(
                                ErrorMessage.Breakfast to binding.llBreakFast,
                                ErrorMessage.Lunch to binding.llLunch,
                                ErrorMessage.Dinner to binding.llDinner,
                                ErrorMessage.Snacks to binding.llSnacks,
                                ErrorMessage.Dessert to binding.llDessert,
                                ErrorMessage.Brunch to binding.llTeaTime
                            )
                            // Update adapter and visibility
                            mealVisibilityMap[type]?.let { view ->
                                if (mealList?.isNotEmpty() == true) {
                                    adapter?.updateList(mealList, type)
                                    view.visibility = View.VISIBLE
                                } else {
                                    view.visibility = View.GONE
                                }
                            }


                            val count = (recipesDateModel?.Breakfast?.count() ?: 0) +
                                    (recipesDateModel?.Teatime?.count() ?: 0) +
                                    (recipesDateModel?.Lunch?.count() ?: 0) +
                                    (recipesDateModel?.Snacks?.count() ?: 0) +
                                    (recipesDateModel?.Dessert?.count() ?: 0) +
                                    (recipesDateModel?.Dinner?.count() ?: 0)

                            if (count==0){
                                binding.layNoData.visibility=View.VISIBLE
                                loadApi("1")
                            }else{
                                binding.layNoData.visibility=View.GONE
                            }
                            (activity as MainActivity?)?.upDateHomeData()
                            dialogScheduleDay.dismiss()
                        } else {
                            handleError(cookedModel.code,cookedModel.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        showAlertFunction(it.message, false)
                    }
                    else -> {
                        showAlertFunction(it.message, false)
                    }
                }
            }, item?.id.toString())
        }
    }

    private fun showAlertFunction(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    override fun itemLongClick(position: Int?, status: String?, type: String?, uriItem: String) {
        id = status.toString()
        uri = uriItem
        mealType = type
        onClickEnabled()
    }

    private fun onClickEnabled() {
        ingredientBreakFastAdapter?.setZiggleEnabled(true)
        ingredientLunchAdapter?.setZiggleEnabled(true)
        ingredientDinnerAdapter?.setZiggleEnabled(true)
        ingredientSnacksAdapter?.setZiggleEnabled(true)
        ingredientTeaTimeAdapter?.setZiggleEnabled(true)
        ingredientDessertAdapter?.setZiggleEnabled(true)
    }

    private fun onClickFalseEnabled() {
        ingredientBreakFastAdapter?.setZiggleEnabled(false)
        ingredientLunchAdapter?.setZiggleEnabled(false)
        ingredientDinnerAdapter?.setZiggleEnabled(false)
        ingredientSnacksAdapter?.setZiggleEnabled(false)
        ingredientTeaTimeAdapter?.setZiggleEnabled(false)
        ingredientDessertAdapter?.setZiggleEnabled(false)
    }


    private fun hideData(){
        binding.llBreakFast.visibility = View.GONE
        binding.llTeaTime.visibility = View.GONE
        binding.llLunch.visibility = View.GONE
        binding.llSnacks.visibility = View.GONE
        binding.llDinner.visibility = View.GONE
        binding.llDessert.visibility = View.GONE
        binding.layNoData.visibility = View.VISIBLE
        // Display current week dates
        showWeekDates()
    }

    override fun onDestroy() {
        super.onDestroy()
        fUllCookingScheduleViewModel.setData(null,null)
    }
}