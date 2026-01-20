package com.mykaimeal.planner.fragment.mainfragment.hometab

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.activity.MealRatingActivity
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentDirectionSteps2RecipeDetailsFragmentBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Instruction
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.RecipeDetailsViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class DirectionSteps2RecipeDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDirectionSteps2RecipeDetailsFragmentBinding
    private var totalProgressValue:Int=0
    private val START_TIME_IN_MILLIS: Long = 30000
    private var mTimeLeftInMillis = START_TIME_IN_MILLIS
    private lateinit var viewModel: RecipeDetailsViewModel
    private var mealType: String = ""
    private var uri: String = ""
    var count =1
    private lateinit var sessionManagement: SessionManagement
    private var countDownTimer: CountDownTimer? = null



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding=FragmentDirectionSteps2RecipeDetailsFragmentBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[RecipeDetailsViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())
        mealType = arguments?.getString("mealType", "").toString()
        uri = arguments?.getString("uri", "").toString()


        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        backButton()

        totalProgressValue= viewModel.getRecipeData()?.get(0)!!.recipe?.instructions?.size!!
        binding.progressBar.max=totalProgressValue
        updateProgress(1)
       /* viewModel.getRecipeData()?.get(0)!!.recipe?.instructions?.sortWith(
            compareBy<Instruction> { it.header }
                .thenBy { it.step_order }
        )*/




        binding.imgStep2RecipeDetails.setOnClickListener{
            findNavController().navigateUp()
        }

        binding.textStartTimer.setOnClickListener{
            binding.textStartTimer.isEnabled = false
            mTimeLeftInMillis= viewModel.getRecipeData()?.get(0)!!.recipe?.totalTime?.let { it1 -> secondsToMillis(it1) }?:0
            startTime()
        }

        binding.tvPreviousBtn.setOnClickListener{
            if (totalProgressValue>=count){
                count -= 1
                updateProgress(count)
            }
        }

        binding.tvNextStepBtn.setOnClickListener{
            if (totalProgressValue>count){
                count += 1
                updateProgress(count)
            }else{
                cookedMealsDialog()
            }
        }

        setData()

        return binding.root
    }


    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) { override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    fun secondsToMillis(seconds: Int): Long {
        return seconds * 60 * 1000L
    }

    @SuppressLint("SetTextI18n")
    private fun setData() {

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.images?.SMALL?.url != null) {
            Glide.with(requireContext())
                .load(viewModel.getRecipeData()?.get(0)!!.recipe?.images?.SMALL?.url)
                .error(R.drawable.no_image)
                .placeholder(R.drawable.no_image)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.layProgess.root.visibility = View.GONE
                        return false
                    }
                })
                .into(binding.imageData)
        } else {
            binding.layProgess.root.visibility = View.GONE
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.label != null) {
            binding.tvTitle.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.label
        }

        if (viewModel.getRecipeData()?.get(0)!!.recipe?.totalTime!=null){
            if (viewModel.getRecipeData()?.get(0)!!.recipe?.totalTime!=0) {
                binding.layTimmer.visibility=View.VISIBLE
                val mili=viewModel.getRecipeData()?.get(0)!!.recipe?.totalTime?.let { it1 -> secondsToMillis(it1) }?:0
                binding.tvTiming.text = /*viewModel.getRecipeData()?.get(0)!!.recipe?.totalTime?.let { formatToHHMMSS(it) }*/formatMillisToHMS(mili)
            }else{
                binding.layTimmer.visibility=View.GONE
            }
        }else{
            binding.layTimmer.visibility=View.GONE
        }



    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun cookedMealsDialog() {
        val dialogCookedMeals: Dialog = context?.let { Dialog(it) }!!
        dialogCookedMeals.setContentView(R.layout.alert_dialog_cooked_meals)
        dialogCookedMeals.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogCookedMeals.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlNextBtn = dialogCookedMeals.findViewById<RelativeLayout>(R.id.rlNextBtn)
        val tvYes = dialogCookedMeals.findViewById<TextView>(R.id.tvYes)
        val tvNo = dialogCookedMeals.findViewById<TextView>(R.id.tvNo)

        dialogCookedMeals.show()
        dialogCookedMeals.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        var type=""

        tvYes.setOnClickListener{
            type="Yes"
            tvYes.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_select_icon, 0)
            tvNo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_unselect_icon, 0)
        }

        tvNo.setOnClickListener{
            type="NO"
            tvYes.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_unselect_icon, 0)
            tvNo.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_select_icon, 0)
        }

        rlNextBtn.setOnClickListener {
            if (type.equals("",true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.cookedMealsError, false)
            }else{
                if (type.equals("No",true)){
                    type=""
                    dialogCookedMeals.dismiss()
                }else{
                    dialogCookedMeals.dismiss()
                    addFridgeDialog()
                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addFridgeDialog() {
        val dialogCookedMeals: Dialog = context?.let { Dialog(it) }!!
        dialogCookedMeals.setContentView(R.layout.alert_dialog_fridge_freezer)
        dialogCookedMeals.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogCookedMeals.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val relDoneBtn = dialogCookedMeals.findViewById<RelativeLayout>(R.id.relDoneBtn)

        val tvFridge = dialogCookedMeals.findViewById<TextView>(R.id.tvFridge)
        val tvFreezer = dialogCookedMeals.findViewById<TextView>(R.id.tvFreezer)


        dialogCookedMeals.show()
        dialogCookedMeals.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        var type=""

        tvFridge.setOnClickListener{
            type="1"
            tvFridge.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_select_icon, 0)
            tvFreezer.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_unselect_icon, 0)
        }

        tvFreezer.setOnClickListener{
            type="2"
            tvFridge.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_unselect_icon, 0)
            tvFreezer.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_select_icon, 0)
        }

        relDoneBtn.setOnClickListener {
            if (type.equals("",true)){
                BaseApplication.alertError(requireContext(), ErrorMessage.cookedMealsError, false)
            }else{
                BaseApplication.showMe(requireContext())
                lifecycleScope.launch {
                    viewModel.addMealTypeApiUrl({
                        BaseApplication.dismissMe()
                        handleApiAddToPlanResponse(it, dialogCookedMeals)
                    },uri,type,mealType)
                }
            }
        }
    }

    private fun handleApiAddToPlanResponse(
        result: NetworkResult<String>,
        dialogCookedMeals: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddToPlanResponse(
                result.data.toString(),
                dialogCookedMeals
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddToPlanResponse(data: String, dialogCookedMeals: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dialogCookedMeals.dismiss()
                (activity as MainActivity?)?.upDateHomeData()
                (activity as MainActivity?)?.upDateCookTab()
                Toast.makeText(requireContext(),apiModel.message,Toast.LENGTH_LONG).show()
                val intent=Intent(requireActivity(),MealRatingActivity::class.java)
                intent.putExtra("uri",uri)
                startActivity(intent)
//                findNavController().navigate(R.id.rateYourMealFragment)
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

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun startTime() {
        countDownTimer = object : CountDownTimer(mTimeLeftInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                mTimeLeftInMillis = millisUntilFinished
                binding.textStartTimer.setTextColor(Color.parseColor("#828282"))
                updateCountDownText()
            }
            override fun onFinish() {
                mTimeLeftInMillis = 0L
                updateCountDownText()
                binding.textStartTimer.setTextColor(Color.parseColor("#FFFFFF"))
                binding.textStartTimer.isEnabled = true
            }
        }.start()

    }

    @SuppressLint("DefaultLocale")
    private fun formatToHHMMSS(timeInSeconds: Int): String {
        // Convert time in seconds to hours, minutes, and seconds
        val hours = timeInSeconds / 3600
        val minutes = (timeInSeconds % 3600) / 60
        val seconds = timeInSeconds % 60
        // Format the time in HH:MM:SS format
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun formatMillisToHMS(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        return String.format("%02d : %02d : %02d", hours, minutes, seconds)
    }

    @SuppressLint("SetTextI18n")
    private fun updateCountDownText() {
        val hours = (mTimeLeftInMillis / (1000 * 60 * 60)) % 24
        val minutes = (mTimeLeftInMillis / (1000 * 60)) % 60
        val seconds = (mTimeLeftInMillis / 1000) % 60
        val timeLeftFormatted = String.format(Locale.getDefault(), "%02d : %02d : %02d", hours, minutes, seconds)
        binding.tvTiming.text = timeLeftFormatted
    }

    @SuppressLint("SetTextI18n")
    private fun updateProgress(progress: Int) {
        binding.progressBar.progress = progress
        binding.tvProgressText.text = "$progress /$totalProgressValue"
        binding.textPrepareDesc.text = viewModel.getRecipeData()?.get(0)!!.recipe?.instructions?.get(progress-1)?.text
        if (viewModel.getRecipeData()?.get(0)!!.recipe?.instructions?.get(progress-1)?.header!=null){
            if (!viewModel.getRecipeData()?.get(0)!!.recipe?.instructions?.get(progress-1)?.header.equals("Recipe")){
                binding.textPrepare.text = "" + viewModel.getRecipeData()?.get(0)!!.recipe?.instructions?.get(progress-1)?.header
                binding.textPrepare.visibility = View.VISIBLE
            }else{
                binding.textPrepare.visibility = View.GONE
            }
        }else{
            binding.textPrepare.visibility = View.GONE
        }

        if (progress==1){
            binding.tvPreviousBtn.visibility=View.GONE
        }else{
            binding.tvPreviousBtn.visibility=View.VISIBLE
        }
    }

    override fun onStart() {
        super.onStart()
        if (!sessionManagement.getMoveScreen()) {
            findNavController().navigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }

}