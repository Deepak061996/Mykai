package com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.appsflyer.AppsFlyerLib
import com.appsflyer.share.ShareInviteHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.apiInterface.BaseUrl
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.formatMonthYear
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.RoundedBarChartRenderer
import com.mykaimeal.planner.databinding.FragmentStatisticsGraphBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsGraphModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.model.StatisticsGraphModelData
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.statistics.viewmodel.StatisticsViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.graphics.toColorInt
import com.appsflyer.CreateOneLinkHttpTask


@AndroidEntryPoint
class StatisticsGraphFragment : Fragment() {

    private lateinit var binding: FragmentStatisticsGraphBinding
    private lateinit var sessionManagement: SessionManagement
    private lateinit var statisticsViewModel: StatisticsViewModel
    private var referLink: String = ""
    private var lastSelectedDate: Long? = null
    private var currentMonth: String = ""
    private var weekOfMonth: String = ""
    private var year: String = ""
    private  var clickCount=0
    private var currentDate = Date() // Current date

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentStatisticsGraphBinding.inflate(layoutInflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.VISIBLE
            llBottomNavigation.visibility = View.VISIBLE
        }

        statisticsViewModel = ViewModelProvider(requireActivity())[StatisticsViewModel::class.java]
        sessionManagement = SessionManagement(requireActivity())

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

    private fun loadGraph(){
        if (BaseApplication.isOnline(requireContext())) {
            getGraphList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initialize() {

        sessionManagement.getUserName()?.let {
            binding.tvStatsNames.text= "Good job ${BaseApplication.capitalLatter(it)} you're on track to big savings! Stick with your plan and watch the results add up."
        }

        sessionManagement.getImage()?.let {
            Glide.with(requireContext())
                .load(BaseUrl.imageBaseUrl+it)
                .placeholder(R.drawable.mask_group_icon)
                .error(R.drawable.mask_group_icon)
                .into(binding.imageProfile)
        }

        // Set currentMonth from today's date
        val calendar = Calendar.getInstance()

        statisticsViewModel.dataGraph?.let {
            currentMonth=statisticsViewModel.dataCurrentMonth.toString()
            year=statisticsViewModel.dataCurrentYear.toString()
            currentDate= statisticsViewModel.currentDate!!
            showSpendingChart(it)
        }?:run {
            weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH).toString()
            currentMonth = (calendar.get(Calendar.MONTH) + 1).toString()
            year = calendar.get(Calendar.YEAR).toString()
            loadGraph()
        }


        binding.tvDateCalendar.text = formatMonthYear(currentMonth.toInt(),year.toInt())

        // Enable pinch zoom and double tap zoom
        binding.barChart.setPinchZoom(false)
        binding.barChart.setScaleEnabled(false)
        binding.barChart.isDoubleTapToZoomEnabled = false

        // Optional: enable dragging and scaling
        binding.barChart.isDragEnabled = false
        binding.barChart.isScaleXEnabled = false
        binding.barChart.isScaleYEnabled = false


        binding.imgBackStats.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.relInvite.setOnClickListener {
            findNavController().navigate(R.id.invitationsScreenFragment)
        }

        binding.textInviteFriends.setOnClickListener {
            shareImageWithText(
                description="Hey, My kai an all-in-one app that’s completely changed the way I shop. It saves me time, money," +
                        " and even helps with meal planning without having to step into a supermarket." +
                        " See for yourself with a free gift from me. \nClick on link below:\n\n",
                referLink)
        }

        /*binding.barChart.setOnClickListener {
            if (clickCount!=0){
                Log.d("datebargraph", "*******$currentDate")
                statisticsViewModel.setGraphDataList(null,currentDate)
                findNavController().navigate(R.id.statisticsWeekYearFragment)
            }
        }*/


//        binding.barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
//            @SuppressLint("DefaultLocale")
//            override fun onValueSelected(e: Entry?, h: Highlight?) {
//                if (e != null) {
//                    val amount = e.y
//                    val index = e.x
//                    if (amount.toInt()!=0){
//                        val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH)
//                        val date = sdf.parse(currentDate.toString())
//                        val calendar = Calendar.getInstance().apply {
//                            time = date!!
//                        }
//                        val month = calendar.get(Calendar.MONTH) + 1
//                        val year = calendar.get(Calendar.YEAR)
//                        val formattedMonth = String.format("%02d", month) // e.g., "05" for May
//                        val weekLabels: MutableList<Date?> = mutableListOf(
//                            BaseApplication.parseToDate("$year-$formattedMonth-01"),
//                            BaseApplication.parseToDate("$year-$formattedMonth-08"),
//                            BaseApplication.parseToDate("$year-$formattedMonth-15"),
//                            BaseApplication.parseToDate("$year-$formattedMonth-22"))
//                        statisticsViewModel.setGraphDataList(null,weekLabels[index.toInt()])
//                        findNavController().navigate(R.id.statisticsWeekYearFragment)
//                    }
//                }
//            }
//            override fun onNothingSelected() {
//                // Optional: handle if nothing is selected
//            }
//        })

        deepLink()

        binding.relMonthYear.setOnClickListener {
            openDialog()
        }

    }


    private fun openDialog() {
        val dialog = Dialog(requireActivity())
        dialog.setContentView(R.layout.dialog_calendar)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val calendarView = dialog.findViewById<CalendarView>(R.id.calendar)

        dialog.setOnShowListener {
            calendarView?.date = lastSelectedDate ?: Calendar.getInstance().timeInMillis
        }


        calendarView?.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            lastSelectedDate = calendar.timeInMillis

            val date = calendar.time  // This is the Date object
            // Format the Date object to the desired string format
            val dateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.getDefault())
            val currentDateString = dateFormat.format(date)  // This is the formatted string
            // To convert the string back to a Date object:
            currentDate = dateFormat.parse(currentDateString)!!  // This is the Date object

            weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH).toString()

            // Format for tvMonthYear: "June 2024"
            val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.tvDateCalendar.text = monthYearFormat.format(calendar.time)
            // Update currentMonth (month is 0-based, so add +1)
            currentMonth = (month + 1).toString()
            loadGraph()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getGraphList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            statisticsViewModel.getGraphScreenUrl({
                BaseApplication.dismissMe()
                handleApiGraphResponse(it)
            }, currentMonth,year)
        }
    }

    private fun handleApiGraphResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessGraphResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessGraphResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, StatisticsGraphModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                apiModel.data?.let {
                    showSpendingChart(apiModel.data)
                }?: run {

                }
            } else {
               handleError(apiModel.code,apiModel.message)
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

    fun getShortMonth(monthName: String): String {
        return monthName.trim().take(3).replaceFirstChar { it.uppercase() }
    }

    fun hasMoreThan28Days(monthName: String): Boolean {
        return when (monthName.trim().lowercase()) {
            "january", "jan" -> true
            "february", "feb" -> false
            "march", "mar" -> true
            "april", "apr" -> true
            "may" -> true
            "june", "jun" -> true
            "july", "jul" -> true
            "august", "aug" -> true
            "september", "sep" -> true
            "october", "oct" -> true
            "november", "nov" -> true
            "december", "dec" -> true
            else -> false
        }
    }


    @SuppressLint("SetTextI18n", "DefaultLocale")
    private fun showSpendingChart(response: StatisticsGraphModelData) {
        statisticsViewModel.setGraphData(response,currentMonth,year,weekOfMonth,currentDate)
        statisticsViewModel.setGraphDataList(null,currentDate)
        val month = getShortMonth(response.month)
        val graphData = response.graph_data
        val weekValues = mutableListOf<Float>()
        weekValues.add(graphData.week_1 ?: 0f)
        weekValues.add((graphData.week_2 ?: 0f))
        weekValues.add((graphData.week_3 ?: 0f))
        weekValues.add((graphData.week_4 ?: 0f))
        if (hasMoreThan28Days(month)){
            weekValues.add((graphData.week_5 ?: 0f))
        }
        clickCount=weekValues.count { it.toInt() !=0 }
        val entries = mutableListOf<BarEntry>()
        for (i in weekValues.indices) {
            entries.add(BarEntry(i.toFloat(), weekValues[i]))
        }

        val weekLabels =  if (hasMoreThan28Days(month)){
            mutableListOf("01 \n$month", "08 \n$month", "15 \n$month", "22 \n$month", "29 \n$month")
        }else{
            mutableListOf("01 \n$month", "08 \n$month", "15 \n$month", "22 \n$month",)
        }

        val barDataSet = BarDataSet(entries, "Spending($)").apply {
            valueTextColor = Color.BLACK
            valueTextSize = 14f
            setDrawValues(true)
            colors = listOf(
                "#FE9F45".toColorInt(), // Orange
                "#06C169".toColorInt(), // Green
                "#F21B1B".toColorInt(), // Red
                "#FE9F45".toColorInt()  // Orange again
            )
            // Dollar formatting on top of bars
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "$${value.toInt()}"
                }
            }
        }

        val barData = BarData(barDataSet).apply {
            barWidth = 1f // tighter spacing
        }

        with(binding.barChart) {
            renderer = RoundedBarChartRenderer(this, animator, viewPortHandler, 30f)
            data = barData
            setFitBars(false)
            animateY(800)
            description.isEnabled = false // Hide description
            axisRight.isEnabled = false
            axisLeft.apply {
                textColor = Color.DKGRAY
                textSize = 12f
                setDrawGridLines(true)
                isHighlightPerTapEnabled = false   // no ripple/highlight when clicking
                isHighlightPerDragEnabled = false  // no highlight when dragging
                gridColor = Color.LTGRAY
                axisMinimum = 0f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "$${value.toInt()}"
                    }
                }
            }

            legend.apply {
                isEnabled = true
                textColor = Color.BLACK
                textSize = 12f
                form = Legend.LegendForm.SQUARE
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            }

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                labelCount = weekValues.size
                textColor = Color.BLACK
                textSize = 12f
                axisMinimum = -0.5f
                axisMaximum = weekValues.size - 0.5f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return weekLabels.getOrNull(value.toInt()) ?: ""
                    }
                }
            }

            invalidate()
        }

        if (response.total_spent != null) {
            val formattedSpent = if (response.total_spent % 1 == 0.0)
                response.total_spent.toInt().toString()
            else
                String.format("%.2f", response.total_spent)

            binding.textSpent.text = "Total spent $$formattedSpent"
        }

        if (response.saving != null) {
            val formattedSaving = if (response.saving % 1 == 0.0)
                response.saving.toInt().toString()
            else
                String.format("%.2f", response.saving)

            binding.tvSavings.text = "Your savings are $$formattedSaving"
        }
    }

    private fun shareImageWithText(description: String, link: String) {
        // Download image using Glide
        Glide.with(requireContext())
            .asBitmap() // Request a Bitmap image
            .load(R.drawable.shareicon) // Provide the URL to load the image from
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition:Transition<in Bitmap>?) {
                    try {
                        // Save the image to a file in the app's external storage
                        val file = File(
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "shared_image.png"
                        )
                        val fos = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        fos.close()
                        // Create URI for the file using FileProvider
                        val uri: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().packageName + ".provider", // Make sure this matches your manifest provider
                            file
                        )
                        // Format the message with line breaks
                        val formattedText = """$description$link""".trimIndent()
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Description", formattedText)
                        clipboard.setPrimaryClip(clip)
                        // Create an intent to share the image and text
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, formattedText)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        // Launch the share dialog
                        requireContext().startActivity(Intent.createChooser(shareIntent, "Share Image"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("ImageShareError", "onResourceReady: ${e.message}")
                    }
                }
                override fun onLoadCleared(placeholder: Drawable?) {
                    // Optional: Handle if the image load is cleared or cancelled
                }
            })
    }

    /*private fun shareImageWithText(description: String, link: String) {
        // Download image using Glide
        Glide.with(requireContext())
            .asBitmap() // Request a Bitmap image
            .load(R.drawable.shareicon) // Provide the URL to load the image from
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    try {
                        // Save the image to a file in the app's external storage
                        val file = File(
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "shared_image.png"
                        )
                        val fos = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        fos.close()

                        // Create URI for the file using FileProvider
                        val uri: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().packageName + ".provider", // Make sure this matches your manifest provider
                            file
                        )

                        // Format the message with line breaks
                        val formattedText = """$description$link""".trimIndent()

                        // Create an intent to share the image and text
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, formattedText)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        // Launch the share dialog
                        requireContext().startActivity(
                            Intent.createChooser(
                                shareIntent,
                                "Share Image"
                            )
                        )

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Log.d("ImageShareError", "onResourceReady: ${e.message}")
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Optional: Handle if the image load is cleared or cancelled
                }
            })

//        val rating = 4.5f // Example rating
//
//        Glide.with(requireContext())
//            .asBitmap()
//            .load(R.drawable.shareicon)
//            .into(object : CustomTarget<Bitmap>() {
//                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                    try {
//                        // Add rating on image
////                        val ratedImage = addRatingToImage(resource, rating)
//                        val ratedImage = addRatingToImage(resource, rating)
//
//                        // Save image
//                        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "rated_image.png")
//                        val fos = FileOutputStream(file)
//                        ratedImage.compress(Bitmap.CompressFormat.PNG, 100, fos)
//                        fos.close()
//
//                        val uri = FileProvider.getUriForFile(
//                            requireContext(),
//                            requireActivity().packageName + ".provider",
//                            file
//                        )
//
//                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
//                            type = "image/png"
//                            putExtra(Intent.EXTRA_STREAM, uri)
//                            putExtra(Intent.EXTRA_TEXT, "$description\n$link")
//                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
//                        }
//
//                        startActivity(Intent.createChooser(shareIntent, "Share Image with Rating"))
//
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                override fun onLoadCleared(placeholder: Drawable?) {}
//            })

    }*/


    fun addRatingToImage(baseImage: Bitmap, rating: Float): Bitmap {
        val result = baseImage.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val starPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 70f
            isAntiAlias = true
        }

        val ratingPaint = Paint().apply {
            color = Color.BLACK  // Change this to your desired color
            textSize = 70f
            isAntiAlias = true
        }

        // Stars string, e.g. "⭐⭐⭐⭐"
        val stars = "⭐".repeat(rating.toInt())
        // Rating text, e.g. " (4/5)"
        val ratingText = " $rating"

        val startX = 20f
        val startY = baseImage.height - 50f

        // Draw stars first
        canvas.drawText(stars, startX, startY, starPaint)

        // Measure stars width to position rating text
        val starsWidth = starPaint.measureText(stars)

        // Draw rating text right after stars
        canvas.drawText(ratingText, startX + starsWidth, startY, ratingPaint)

        return result
    }

    private fun deepLink() {
        val afUserId = sessionManagement.getId().orEmpty()
        val referrerCode = sessionManagement.getReferralCode().orEmpty()
        val providerName = sessionManagement.getUserName().orEmpty()
        val providerImage = sessionManagement.getImage().orEmpty()

        val deepLink = "mykai://property?" +
                "af_user_id=$afUserId" +
                "&Referrer=$referrerCode" +
                "&providerName=$providerName" +
                "&providerImage=$providerImage"

        val webLink = "https://www.mykaimealplanner.com"

        // Correct: Use OneLink template ID (not Dev Key)
        AppsFlyerLib.getInstance().setAppInviteOneLink("mPqu") // replace with your OneLink template

        val linkGenerator = ShareInviteHelper.generateInviteUrl(requireContext())
        linkGenerator
            .addParameter("providerName", providerName)
            .addParameter("providerImage", providerImage)
            .addParameter("Referrer", referrerCode)
            .addParameter("af_dp", deepLink)
            .addParameter("af_web_dp", webLink)

        linkGenerator.generateLink(requireContext(), object: CreateOneLinkHttpTask.ResponseListener {
            override fun onResponse(link: String) {
                referLink = link
                Log.d("AppsFlyer", "Short OneLink URL: $link")
            }

            override fun onResponseError(p0: String?) {
                Log.e("AppsFlyer", "Error generating short link: $p0")
            }
        })
    }

    /*private fun deepLink(){
        val afUserId = sessionManagement.getId().orEmpty()
        val referrerCode = sessionManagement.getReferralCode().orEmpty()
        val providerName = sessionManagement.getUserName().orEmpty()
        val providerImage = sessionManagement.getImage().orEmpty()

        // Base URL for the OneLink template
        val baseURL = "https://mykaimealplanner.onelink.me/mPqu/" // Replace with your OneLink template
        // Deep link URL for when the app is installed
        val deepLink = "mykai://property?" +
                "af_user_id=$afUserId" +
                "&Referrer=$referrerCode" +
                "&providerName=$providerName" +
                "&providerImage=$providerImage"
        // Web fallback URL (e.g., if app is not installed)
        val webLink = "https://www.mykaimealplanner.com" // Replace with your fallback web URL
        // Build OneLink parameters
        val parameters = mapOf(
            "af_dp" to deepLink, // App deep link
            "af_web_dp" to webLink // Web fallback URL
        )
        // Use Uri.Builder to construct the URL with query parameters
        val uriBuilder = Uri.parse(baseURL).buildUpon()
        for ((key, value) in parameters) {
            uriBuilder.appendQueryParameter(key, value)
        }
        // Convert the URI to string and call the completion handler
        val fullURL = uriBuilder.toString()
        referLink = fullURL
        Log.d("link ", "Generated OneLink URL: $fullURL")

    }*/





}