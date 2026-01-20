package com.mykaimeal.planner.fragment.mainfragment.hometab

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.OnItemSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterRecipeItem
import com.mykaimeal.planner.adapter.ChooseDayAdapter
import com.mykaimeal.planner.adapter.CookWareAdapter
import com.mykaimeal.planner.adapter.IngredientsRecipeAdapter
import com.mykaimeal.planner.adapter.NewIngredientsRecipeDetailsAdapter
import com.mykaimeal.planner.adapter.NewRecipeDetailRecipeAdapter
import com.mykaimeal.planner.adapter.RecipeDetailCookWareAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.databinding.FragmentRecipeDetailsBinding
import com.mykaimeal.planner.databinding.FragmentRecipeDetailssBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.convertmodel.ConvertModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.RecipeDetailsViewModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.Data
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.recipedetails.apiresponse.RecipeDetailsApiResponse
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import com.mykaimeal.planner.model.DataModel
import com.mykaimeal.planner.model.DateModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class NewRecipeDetailsFragment : Fragment(), OnItemSelectListener {

    private lateinit var binding: FragmentRecipeDetailssBinding
    private var ingredientsRecipeAdapter: NewIngredientsRecipeDetailsAdapter? = null
    private var adapterRecipeItem: NewRecipeDetailRecipeAdapter? = null
    private var adapterCookwareItem: RecipeDetailCookWareAdapter? = null
    val dataList = ArrayList<DataModel>()
    private var tvWeekRange: TextView? = null
    private var rcyChooseDaySch: RecyclerView? = null
    private var selectAll: Boolean = false
    private lateinit var viewModel: RecipeDetailsViewModel
    private var uri: String = ""
    private var mealType: String = ""
    private var recipeID: String = ""
    private var statusType: String = ""
    private var serving: String = ""
    private var currentDate = Date() // Current date
    private lateinit var sessionManagement: SessionManagement
    // Define global variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private var convertUnitStatus="Original"
    private lateinit var dialogWeight :BottomSheetDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentRecipeDetailssBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[RecipeDetailsViewModel::class.java]
        sessionManagement = SessionManagement(requireContext())
        uri = arguments?.getString("uri", "")?:""
        mealType = arguments?.getString("mealType", "")?:""
        recipeID = arguments?.getString("recipeID", "")?:""
        statusType = arguments?.getString("statusType", "")?:""
        serving = arguments?.getString("serving", "")?:""

        Log.d("@@@@@ ERROR", "uri :- $uri----$serving")

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        setupBackNavigation()
        
        initialize()

        // When screen load then api call
        fetchDataOnLoad()

        return binding.root
    }

    private fun fetchDataOnLoad() {
        if (BaseApplication.isOnline(requireActivity())) {
            fetchRecipeDetailsData()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun fetchRecipeDetailsData() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeDetailsRequest({
                BaseApplication.dismissMe()
                handleApiResponse(it)
            }, uri,recipeID,statusType,serving)
        }
    }

    private fun handleApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun handleBasketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessBasketResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessBasketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ Recipe Details ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
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

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, RecipeDetailsApiResponse::class.java)
            Log.d("@@@ Recipe Details", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                Log.d("@@@ Recipe Detailsssss", "message :- $apiModel")
                if (apiModel.data != null && apiModel.data.size > 0) {
                    showData(apiModel.data)
                } else {
                    binding.layBottom.visibility = View.GONE
                }
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showData(data: MutableList<Data>) {
        /*localData.clear()
        localData.addAll(data)*/
        viewModel.setRecipeData(data)
        if (viewModel.getRecipeData()?.get(0)?.recipe?.images?.SMALL?.url != null) {
            Glide.with(requireContext())
                .load(viewModel.getRecipeData()?.get(0)?.recipe?.images?.SMALL?.url)
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

        if (viewModel.getRecipeData()?.get(0)?.recipe?.source!=null){
            binding.tvSourcesName.text=viewModel.getRecipeData()?.get(0)?.recipe?.source
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.description!=null){
            binding.etRecipeName.text=viewModel.getRecipeData()?.get(0)?.recipe?.description
        }

        if (viewModel.getRecipeData()?.get(0)?.servings!=null){
            binding.tvValues.text=""+ viewModel.getRecipeData()?.get(0)!!.servings +" servings"
        }

        if (viewModel.getRecipeData()?.get(0)?.review!=null){
             binding.tvRating.text = ""+viewModel.getRecipeData()?.get(0)!!.review+" ("+BaseApplication.formatRatingCount(viewModel.getRecipeData()?.get(0)?.review_number?:0)+")"
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.label != null) {
            binding.tvTitle.text = "" + viewModel.getRecipeData()?.get(0)?.recipe?.label
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.calories != null) {
            binding.tvCalories.text = "" + viewModel.getRecipeData()?.get(0)?.recipe?.calories?.toInt()
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.totalNutrients?.FAT?.quantity != null) {
            binding.tvFat.text = "" + viewModel.getRecipeData()?.get(0)?.recipe?.totalNutrients?.FAT?.quantity?.toInt()+"g"
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.totalNutrients?.PROCNT?.quantity != null) {
            binding.tvProtein.text = "" + viewModel.getRecipeData()?.get(0)?.recipe?.totalNutrients?.PROCNT?.quantity?.toInt()+"g"
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.totalNutrients?.CHOCDF?.quantity != null) {
            binding.tvCarbs.text = "" + viewModel.getRecipeData()?.get(0)?.recipe?.totalNutrients?.CHOCDF?.quantity?.toInt()+"g"
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.totalTime != null) {
            binding.tvTotaltime.text = "" + viewModel.getRecipeData()?.get(0)?.recipe?.totalTime + " min "
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.prep_time != null) {
            binding.tvPretime.text = "" + viewModel.getRecipeData()?.get(0)?.recipe?.prep_time + " min "
        }

        viewModel.getRecipeData()?.get(0)?.recipe?.instructions?.forEach {
            Log.d("@@@@@", "message :- ${it.text}")
        }

        val instructions = viewModel.getRecipeData()
            ?.get(0)
            ?.recipe
            ?.instructions
        instructions?.let { list ->
            val allHeadersAreRecipe = list.all { it.header == "Recipe" }
            if (!allHeadersAreRecipe) {
                list.sortWith { a, b ->
                    when {
                        a.header == "Recipe" && b.header != "Recipe" -> -1
                        b.header == "Recipe" && a.header != "Recipe" -> 1
                        else -> compareValuesBy(a, b, { it.header }, { it.step_order })
                    }
                }
            }
        }

        viewModel.getRecipeData()?.get(0)?.recipe?.instructions?.let {
            if (it.isNotEmpty()){
                adapterRecipeItem = NewRecipeDetailRecipeAdapter(it)
            }
        }

        viewModel.getRecipeData()?.get(0)?.recipe?.cookware?.let {
            if (it.isNotEmpty()){
                adapterCookwareItem = RecipeDetailCookWareAdapter(it, requireActivity())
            }
        }

        if (viewModel.getRecipeData()?.get(0)?.recipe?.ingredients != null &&
            viewModel.getRecipeData()?.get(0)?.recipe?.ingredients!!.isNotEmpty()) {

            viewModel.getRecipeData()?.get(0)?.recipe?.ingredients?.let {
                ingredientsRecipeAdapter = NewIngredientsRecipeDetailsAdapter(it, requireActivity(), this)
                binding.rcyIngCookWareRecipe.adapter = ingredientsRecipeAdapter
            }
            selectAll=false
            selectAll = !selectAll
            val drawableRes = if (selectAll) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
            binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)
            viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEach { ingredient ->
                ingredient.status = selectAll
                ingredient.header = ingredient.header?:"Ingredients"
            }
            ingredientsRecipeAdapter?.updateList(viewModel.getRecipeData()?.get(0)?.recipe?.ingredients!!)
            binding.layBottom.visibility = View.VISIBLE
        } else {
            binding.layBottom.visibility = View.GONE
        }


    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }


    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    private fun initialize() {

        binding.imgPlusValue.setOnClickListener {
            val qty=binding.tvValues.text.toString().replace("servings","").trim()
            if (qty.toInt() < 99) {
                val data=qty.toInt()+1
                updateValue(data.toString())
            }
        }

        binding.imgMinusValue.setOnClickListener {
            val qty=binding.tvValues.text.toString().replace("servings","").trim()
            if (qty.toInt()  > 1) {
                val data=qty.toInt()-1
                updateValue(data.toString())
            } else {
                Toast.makeText(requireActivity(), ErrorMessage.servingError, Toast.LENGTH_LONG).show()
            }
        }

        binding.tvAddToPlan.setOnClickListener {
            if ((activity as? MainActivity)?.Subscription_status==1){
                if ((activity as? MainActivity)?.addmeal!! < 1){
                    // Safely get the item and position
                    chooseDayDialog()
                }else{
                    (activity as? MainActivity)?.subscriptionAlertError(requireContext())
                }
            }else{
                chooseDayDialog()
            }
        }

        binding.relBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.llIngredients.setOnClickListener {
            showIngredientData()
        }

        binding.llCookWare.setOnClickListener {
            showCookwareDate()
        }

        binding.llRecipe.setOnClickListener {
            if (viewModel.getRecipeData()?.get(0)!!.recipe?.instructions != null && viewModel.getRecipeData()?.get(0)!!.recipe?.instructions!!.size > 0) {
                viewModel.getRecipeData()?.get(0)!!.recipe?.createdType?.let {
                    if (it.equals("import",true)){
                        alertFullRecipe()
                        /*binding.layBottom.visibility = View.GONE
                        binding.rcyIngCookWareRecipe.visibility = View.GONE
                        binding.webView.visibility = View.VISIBLE*/
                    }else{
                        showRecipeData()
                        viewModel.getRecipeData()?.get(0)!!.recipe?.instructions?.let { it1 ->
                            adapterRecipeItem?.updateList(it1)
                            binding.rcyIngCookWareRecipe.adapter = adapterRecipeItem
                        }
                        binding.rcyIngCookWareRecipe.visibility = View.VISIBLE
                        binding.layBottom.visibility = View.VISIBLE
                        binding.webView.visibility = View.GONE
                    }
                }?:run {
                    showRecipeData()
                    binding.layBottom.visibility = View.GONE
                    binding.rcyIngCookWareRecipe.visibility = View.GONE
                    binding.webView.visibility = View.GONE
                }
            }else{
                viewModel.getRecipeData()?.get(0)!!.recipe?.createdType?.let {
                    if (it.equals("import", true)) {
                        alertFullRecipe()
                       /* binding.layBottom.visibility = View.GONE
                        binding.rcyIngCookWareRecipe.visibility = View.GONE
                        binding.webView.visibility = View.VISIBLE*/
                    } else {
                        showRecipeData()
                        binding.layBottom.visibility = View.GONE
                        binding.rcyIngCookWareRecipe.visibility = View.GONE
                        binding.webView.visibility = View.GONE
                    }
                }?:run {
                    showRecipeData()
                    binding.layBottom.visibility = View.GONE
                    binding.rcyIngCookWareRecipe.visibility = View.GONE
                    binding.webView.visibility = View.GONE
                }
            }

        }

        binding.textStepInstructions.setOnClickListener {
            if (viewModel.getRecipeData()!=null){
                if (viewModel.getRecipeData()?.get(0)!!.recipe?.instructionLines!!.isNotEmpty()) {
                    val bundle=Bundle()
                    bundle.putString("uri",uri)
                    bundle.putString("mealType",mealType)
                    sessionManagement.setMoveScreen(true)
                    findNavController().navigate(R.id.directionSteps1RecipeDetailsFragment,bundle)
                }
            }
        }

        binding.tvSelectAllBtn.setOnClickListener {
            if (viewModel.getRecipeData()?.size!!>0) {
                selectAll = !selectAll // Toggle the selectAll value
                // Update the drawable based on the selectAll state
                val drawableRes = if (selectAll) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
                binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)

                // Update the status of each ingredient dynamically
                viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEach { ingredient ->
                    ingredient.status = selectAll
                }
                // Notify adapter with updated data
                ingredientsRecipeAdapter?.updateList(viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients!!)
            }
        }

        binding.layBasket.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (viewModel.getRecipeData()?.size!!> 0) {
                    try {
                        (activity as MainActivity?)?.upDateHomeData()
                        (activity as MainActivity?)?.upBasket()
                        var status=false
                        // Create a JsonArray for ingredients
                        val jsonArray = JsonArray()
                        // Iterate through the ingredients and add them to the array if status is true
                        viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEach { ingredientsModel ->

                            if (ingredientsModel.status == true) {
                                // Create a JsonObject for each ingredient
                                val ingredientObject = JsonObject()
                                ingredientObject.addProperty("name", ingredientsModel.text)
                                ingredientObject.addProperty("image", ingredientsModel.image)
                                ingredientObject.addProperty("food", ingredientsModel.food ?:ingredientsModel.text?:"")
                                ingredientObject.addProperty("quantity", ingredientsModel.quantity)
                                ingredientObject.addProperty("ingredient_cost", ingredientsModel.ingredient_cost)
//                                ingredientObject.addProperty("quantity", "1")
                                ingredientObject.addProperty("foodCategory", ingredientsModel.foodCategory)
                                ingredientObject.addProperty("measure", ingredientsModel.measure)
                                ingredientObject.addProperty("food_id", ingredientsModel.id)
                                ingredientObject.addProperty("status", "0")
                                // Add the ingredient object to the array
                                jsonArray.add(ingredientObject)
                                status=true
                            }
                        }
                        if (status){
                            // Create a JsonObject for the main JSON structure
                            val jsonObject = JsonObject()
                            jsonObject.addProperty("serving", binding.tvValues.text.toString())
                            jsonObject.addProperty("uri", uri)
                            jsonObject.addProperty("type", mealType)
                            // Add the ingredients array to the main JSON object
                            jsonObject.add("ingredients", jsonArray)
                            // Log the final JSON data
                            Log.d("final data", "******$jsonObject")
                            addBasketDetailsApi(jsonObject)
                        }else{
                            BaseApplication.alertError(requireContext(), ErrorMessage.ingredientError, false)
                        }

                    } catch (e: Exception) {
                        BaseApplication.alertError(requireContext(), e.message, false)
                    }
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        binding.btnConvertUnit.setOnClickListener {
            viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.let {
                if (it.isNotEmpty()){
                    openBoxConvertUnit()
                }
            }
        }

    }

    private fun showIngredientData(){
        binding.textIngredients.setBackgroundResource(R.drawable.select_bg)
        binding.textCookWare.setBackgroundResource(R.drawable.unselect_bg)
        binding.textRecipe.setBackgroundResource(R.drawable.unselect_bg)

        binding.textIngredients.setTextColor(Color.parseColor("#FFFFFF"))
        binding.textCookWare.setTextColor(Color.parseColor("#3C4541"))
        binding.textRecipe.setTextColor(Color.parseColor("#3C4541"))


        binding.rcyIngCookWareRecipe.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE

        binding.btnConvertUnit.visibility = View.VISIBLE
        binding.textStepInstructions.visibility = View.GONE
        binding.relServingsPeople.visibility = View.VISIBLE
        binding.layBottomPlanBasket.visibility = View.VISIBLE
        binding.relIngSelectAll.visibility = View.VISIBLE
        binding.layAuthor.visibility = View.VISIBLE
        binding.llCreateTitle.visibility = View.VISIBLE



        if (viewModel.getRecipeData()?.size!! > 0) {
            // Update the drawable based on the selectAll state
            val drawableRes = if (selectAll) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
            binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)

            viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.let {
                if (it.isNotEmpty()){
                    // Notify adapter with updated data
                    ingredientsRecipeAdapter?.updateList(it)
                    binding.rcyIngCookWareRecipe.adapter = ingredientsRecipeAdapter
                    binding.layBottom.visibility = View.VISIBLE
                }else{
                    binding.layBottom.visibility = View.GONE
                }
            }?:run {
                binding.layBottom.visibility = View.GONE
            }


        }else{
            binding.layBottom.visibility = View.GONE
        }
    }

    private fun showCookwareDate(){
        binding.textIngredients.setBackgroundResource(R.drawable.unselect_bg)
        binding.textCookWare.setBackgroundResource(R.drawable.select_bg)
        binding.textRecipe.setBackgroundResource(R.drawable.unselect_bg)
        binding.textIngredients.setTextColor(Color.parseColor("#3C4541"))
        binding.textCookWare.setTextColor(Color.parseColor("#FFFFFF"))
        binding.textRecipe.setTextColor(Color.parseColor("#3C4541"))
        binding.layBottom.visibility = View.GONE
        binding.btnConvertUnit.visibility = View.GONE
        binding.layBottomPlanBasket.visibility = View.GONE

        binding.relIngSelectAll.visibility = View.GONE
        binding.textStepInstructions.visibility = View.GONE
        binding.relServingsPeople.visibility = View.GONE

        binding.rcyIngCookWareRecipe.visibility = View.VISIBLE
        binding.webView.visibility = View.GONE

        binding.layAuthor.visibility = View.VISIBLE
        binding.llCreateTitle.visibility = View.VISIBLE


        if (viewModel.getRecipeData()?.get(0)!!.recipe?.cookware != null && viewModel.getRecipeData()?.get(0)!!.recipe?.cookware!!.size > 0) {
            viewModel.getRecipeData()?.get(0)!!.recipe?.cookware?.let { it1 ->
                adapterCookwareItem?.updateList(it1)
            }
            binding.rcyIngCookWareRecipe.adapter = adapterCookwareItem
            binding.layBottom.visibility = View.VISIBLE
            binding.rcyIngCookWareRecipe.visibility = View.VISIBLE
        }else{
            binding.layBottom.visibility = View.GONE
            binding.rcyIngCookWareRecipe.visibility = View.GONE
        }
    }


    private fun showRecipeData(){
        binding.textIngredients.setBackgroundResource(R.drawable.unselect_bg)
        binding.textCookWare.setBackgroundResource(R.drawable.unselect_bg)
        binding.textRecipe.setBackgroundResource(R.drawable.select_bg)
        binding.textIngredients.setTextColor(Color.parseColor("#3C4541"))
        binding.textCookWare.setTextColor(Color.parseColor("#3C4541"))
        binding.textRecipe.setTextColor(Color.parseColor("#FFFFFF"))
        binding.textStepInstructions.visibility = View.VISIBLE
        binding.btnConvertUnit.visibility = View.GONE
        binding.relServingsPeople.visibility = View.GONE
        binding.relIngSelectAll.visibility = View.GONE
        binding.layBottomPlanBasket.visibility = View.GONE

        binding.layAuthor.visibility = View.GONE
        binding.llCreateTitle.visibility = View.GONE
    }



    @SuppressLint("ClickableViewAccessibility", "SetJavaScriptEnabled")
    private fun alertFullRecipe(){
        val dialog = Dialog(requireContext(), R.style.BottomSheetDialog)
        dialog.setContentView(R.layout.fullrecipedialog)
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(false)

        // Force solid white background
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        val relBack: RelativeLayout? = dialog.findViewById(R.id.relBack)
        val llIngredients: LinearLayout? = dialog.findViewById(R.id.llIngredients)
        val llCookWare: LinearLayout? = dialog.findViewById(R.id.llCookWare)
        val llRecipe: LinearLayout? = dialog.findViewById(R.id.llRecipe)
        val webView: WebView? = dialog.findViewById(R.id.webView)
        val imgrefresh: LinearLayout? = dialog.findViewById(R.id.imgrefresh)
        val imgshare: LinearLayout? = dialog.findViewById(R.id.imgshare)
        val imgright: LinearLayout? = dialog.findViewById(R.id.imgright)
        val imgleft: LinearLayout? = dialog.findViewById(R.id.imgleft)
        val webSettings = webView?.settings
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.loadsImagesAutomatically = true
        webSettings?.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        webView?.isVerticalScrollBarEnabled = true
        webView?.isHorizontalScrollBarEnabled = false
        webView?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView?.webChromeClient = WebChromeClient()
        webView?.webViewClient = WebViewClient()
        webView?.loadUrl(viewModel.getRecipeData()?.get(0)!!.recipe?.source_url ?: "")
        relBack?.setOnClickListener {
            dialog.dismiss()
        }
        llIngredients?.setOnClickListener {
            dialog.dismiss()
            showIngredientData()
        }
        llCookWare?.setOnClickListener {
            dialog.dismiss()
            showCookwareDate()
        }

        llRecipe?.setOnClickListener {
            webView?.loadUrl(viewModel.getRecipeData()?.get(0)!!.recipe?.source_url ?: "")
        }

        imgrefresh?.setOnClickListener {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getRecipeData()?.get(0)!!.recipe?.source_url ?: ""))
                intent.setPackage("com.android.chrome") // Force to open in Chrome
                requireActivity().startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Chrome is not installed, open with any browser
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getRecipeData()?.get(0)!!.recipe?.source_url ?: ""))
                requireActivity().startActivity(intent)
            }
        }

        imgright?.setOnClickListener {
            if (webView?.canGoForward() == true) webView.goForward()
        }

        imgleft?.setOnClickListener {
            if (webView?.canGoBack() == true) webView.goBack()
        }

        imgshare?.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, viewModel.getRecipeData()?.get(0)!!.recipe?.source_url ?: "")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share link via")
            requireActivity().startActivity(shareIntent)
        }


        dialog.show()
    }

    private fun openBoxConvertUnit(){
        dialogWeight = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialogWeight.setContentView(R.layout.alert_dialog_convertunit)
        dialogWeight.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogWeight.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogWeight.window?.setGravity(Gravity.BOTTOM)
        dialogWeight.setCanceledOnTouchOutside(true)
        dialogWeight.setCancelable(true)
        val bottomSheet = dialogWeight.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }

        val btnOriginal: TextView? = dialogWeight.findViewById(R.id.btnOriginal)
        val btnMetric: TextView? = dialogWeight.findViewById(R.id.btnMetric)
        val btnImperial: TextView? = dialogWeight.findViewById(R.id.btnImperial)


        if (convertUnitStatus.equals("Original",true)){
            btnOriginal?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_check_icon, 0)
            btnMetric?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnImperial?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
        }

        if (convertUnitStatus.equals("Metric",true)){
            btnOriginal?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnMetric?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_check_icon, 0)
            btnImperial?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
        }

        if (convertUnitStatus.equals("Imperial",true)){
            btnOriginal?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnMetric?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnImperial?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_check_icon, 0)
        }

        btnOriginal?.setOnClickListener {
            convertUnitStatus="Original"
            btnOriginal.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_check_icon, 0)
            btnMetric?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnImperial?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            convertApi()
        }

        btnMetric?.setOnClickListener {
            convertUnitStatus="Metric"
            btnOriginal?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnMetric.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_check_icon, 0)
            btnImperial?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            convertApi()
        }

        btnImperial?.setOnClickListener {
            convertUnitStatus="Imperial"
            btnOriginal?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnMetric?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_uncheck_gray_icon, 0)
            btnImperial.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.radio_check_icon, 0)
            convertApi()
        }
        dialogWeight.show()
    }

    private fun convertApi(){
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            val jsonObject = JsonObject()
            val unitArray = JsonArray()
            val quantityArray = JsonArray()
            viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEach { item ->
                val quantity = item.quantity
                val unit = item.measure.toString()
                unitArray.add(unit)
                quantityArray.add(quantity)
            }

            if (convertUnitStatus.equals("Imperial",true) || convertUnitStatus.equals("Original",true)){
                jsonObject.addProperty("type", "2")
            }else{
                jsonObject.addProperty("type", "1")
            }
            jsonObject.add("unit", unitArray)
            jsonObject.add("quantity", quantityArray)
            lifecycleScope.launch {
                viewModel.convertUnitRequestApi({
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> {
                            try {
                                val gson = Gson()
                                val convertModel = gson.fromJson(it.data, ConvertModel::class.java)
                                if (convertModel.code == 200 && convertModel.success) {
                                    if (convertModel.data!=null){
                                        updateIngredientList(convertModel.data)
                                    }
                                } else {
                                    if (convertModel.code == ErrorMessage.code) {
                                        showAlert(convertModel.message, true)
                                    }else{
                                        showAlert(convertModel.message, false)
                                    }
                                }
                            }catch (e:Exception){
                                Log.d("CreateRecipe:","Message:--"+e.message)
                            }
                        }
                        is NetworkResult.Error -> {
                            showAlert(it.message, false)
                        }
                        else -> {
                            showAlert(it.message, false)
                        }
                    }
                },jsonObject)
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun updateIngredientList(dataList: MutableList<com.mykaimeal.planner.fragment.mainfragment.addrecipetab.convertmodel.Data>?) {
        viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients =
            viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.mapIndexed { index, ingredient ->
                val data = dataList?.getOrNull(index)
                if (data != null) {
                    ingredient.copy(
                        quantity = data.converted.toString().toDouble(),
                        measure = data.target_unit
                    )
                } else {
                    ingredient
                }
            }?.toMutableList()
        viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.let {
            ingredientsRecipeAdapter?.updateList(
                it
            )
        }
        dialogWeight.dismiss()
    }


    private fun addBasketDetailsApi(jsonObject: JsonObject) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeAddBasketRequest({
                BaseApplication.dismissMe()
                handleBasketApiResponse(it)
            }, jsonObject)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun updateValue(data: String) {
        binding.tvValues.text = "$data servings"
    }

    @SuppressLint("SetTextI18n")
    private fun chooseDayDialog() {
        val dialogChooseDay: Dialog = context?.let { Dialog(it) }!!
        dialogChooseDay.setContentView(R.layout.alert_dialog_choose_day)
        dialogChooseDay.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogChooseDay.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        rcyChooseDaySch = dialogChooseDay.findViewById(R.id.rcyChooseDaySch)
        tvWeekRange = dialogChooseDay.findViewById(R.id.tvWeekRange)
        val rlDoneBtn = dialogChooseDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        val btnPrevious = dialogChooseDay.findViewById<ImageView>(R.id.btnPrevious)
        val btnNext = dialogChooseDay.findViewById<ImageView>(R.id.btnNext)
        dialogChooseDay.show()
        dialogChooseDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        dataList.clear()
        val daysOfWeek =
            listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        for (day in daysOfWeek) {
            val data = DataModel().apply {
                title = day
                isOpen = false
                type = "CookingSchedule"
                date = ""
            }
            dataList.add(data)
        }

        showWeekDates()


        rlDoneBtn.setOnClickListener {
            var status = false
            for (it in dataList) {
                if (it.isOpen) {
                    status = true
                    break // Exit the loop early
                }
            }
            if (status){
                chooseDayMealTypeDialog()
                dialogChooseDay.dismiss()
            }else{
                BaseApplication.alertError(requireContext(), ErrorMessage.weekNameError, false)
            }

        }

        btnPrevious.setOnClickListener {
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
                Toast.makeText(requireContext(),ErrorMessage.slideError,Toast.LENGTH_LONG).show()
            }
        }

        btnNext.setOnClickListener {
            // Simulate clicking the "Next" button to move to the next week
            val calendar = Calendar.getInstance()
            calendar.time = currentDate
            calendar.add(Calendar.WEEK_OF_YEAR, 1) // Move to next week
            currentDate = calendar.time
            // Display next week dates
            println("\nAfter clicking 'Next':")
            showWeekDates()
        }


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
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        return dateFormat.format(date)
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

    @SuppressLint("SetTextI18n")
    fun showWeekDates() {
        Log.d("currentDate :- ", "******$currentDate")
        val (startDate, endDate) = getWeekDates(currentDate)
        this.startDate = startDate
        this.endDate = endDate

        println("Week Start Date: ${formatDate(startDate)}")
        println("Week End Date: ${formatDate(endDate)}")

        // Get all dates between startDate and endDate
        val daysBetween = getDaysBetween(startDate, endDate)
        // Mark the current date as selected in the list
        daysBetween.zip(dataList).forEach { (dateModel, dataModel) ->
            dataModel.date = dateModel.date
            dataModel.isOpen = false
        }

        rcyChooseDaySch?.adapter = ChooseDayAdapter(dataList, requireActivity())


        // Print the dates
        println("Days between $startDate and ${endDate}:")
        daysBetween.forEach { println(it) }
        tvWeekRange?.text = "" + formatDate(startDate) + "-" + formatDate(endDate)

    }


    private fun chooseDayMealTypeDialog() {
        val dialogChooseMealDay: Dialog = context?.let { Dialog(it) }!!
        dialogChooseMealDay.setContentView(R.layout.alert_dialog_choose_day_meal_type)
        dialogChooseMealDay.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogChooseMealDay.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlDoneBtn = dialogChooseMealDay.findViewById<RelativeLayout>(R.id.rlDoneBtn)
        // button event listener
        val tvBreakfast = dialogChooseMealDay.findViewById<TextView>(R.id.tvBreakfast)
        val tvLunch = dialogChooseMealDay.findViewById<TextView>(R.id.tvLunch)
        val tvDinner = dialogChooseMealDay.findViewById<TextView>(R.id.tvDinner)
        val tvSnacks = dialogChooseMealDay.findViewById<TextView>(R.id.tvSnacks)
        val tvTeatime = dialogChooseMealDay.findViewById<TextView>(R.id.tvTeatime)
        val tvDessert = dialogChooseMealDay.findViewById<TextView>(R.id.tvDessert)
        dialogChooseMealDay.show()
        dialogChooseMealDay.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        var type = ""

        fun updateSelection(
            selectedType: String,
            selectedView: TextView,
            allViews: List<TextView>
        ) {
            type = selectedType
            allViews.forEach { view ->
                val drawable =
                    if (view == selectedView) R.drawable.radio_select_icon else R.drawable.radio_unselect_icon
                view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0)
            }
        }

        val allViews = listOf(tvBreakfast, tvLunch, tvDinner, tvSnacks, tvTeatime, tvDessert)

        tvBreakfast.setOnClickListener {
            updateSelection(ErrorMessage.Breakfast, tvBreakfast, allViews)
        }

        tvLunch.setOnClickListener {
            updateSelection(ErrorMessage.Lunch, tvLunch, allViews)
        }

        tvDinner.setOnClickListener {
            updateSelection(ErrorMessage.Dinner, tvDinner, allViews)
        }

        tvSnacks.setOnClickListener {
            updateSelection(ErrorMessage.Snacks, tvSnacks, allViews)
        }

        tvTeatime.setOnClickListener {
            updateSelection(ErrorMessage.Brunch, tvTeatime, allViews)
        }

        tvDessert.setOnClickListener {
            updateSelection(ErrorMessage.Dessert, tvTeatime, allViews)
        }


        rlDoneBtn.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                if (type.equals("",true)){
                    BaseApplication.alertError(requireContext(), ErrorMessage.mealTypeError, false)
                }else {
                    (activity as MainActivity?)?.upDateHomeData()
                    (activity as MainActivity?)?.upBasket()
                    addToPlan(dialogChooseMealDay, type)
                }
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }


    @SuppressLint("DefaultLocale")
    private fun addToPlan(dialogChooseMealDay: Dialog, selectType: String) {

        // Create a JsonObject for the main JSON structure
        val jsonObject = JsonObject()
        if (uri != null) {
            jsonObject.addProperty("type", selectType)
            jsonObject.addProperty("uri", uri)
            jsonObject.addProperty("serving", binding.tvValues.text.toString())
            // Create a JsonArray for ingredients
            val jsonArray = JsonArray()
            val latestList = getDaysBetween(startDate, endDate)
            for (i in dataList.indices) {
                val data = DataModel()
                data.isOpen = dataList[i].isOpen
                data.title = dataList[i].title
                data.date = latestList[i].date
                dataList[i] = data
            }
            // Iterate through the ingredients and add them to the array if status is true
            dataList.forEach { data ->
                if (data.isOpen) {
                    // Create a JsonObject for each ingredient
                    val ingredientObject = JsonObject()
                    ingredientObject.addProperty("date", data.date)

                    ingredientObject.addProperty("day", data.title)
                    // Add the ingredient object to the array
                    jsonArray.add(ingredientObject)
                }
            }

            // Add the ingredients array to the main JSON object
            jsonObject.add("slot", jsonArray)
        }
        Log.d("json object ", "******$jsonObject")

        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            viewModel.recipeAddToPlanRequest({
                BaseApplication.dismissMe()
                handleApiAddToPlanResponse(it, dialogChooseMealDay)
            }, jsonObject)
        }
    }

    private fun handleApiAddToPlanResponse(
        result: NetworkResult<String>,
        dialogChooseMealDay: Dialog
    ) {
        when (result) {
            is NetworkResult.Success -> handleSuccessAddToPlanResponse(
                result.data.toString(),
                dialogChooseMealDay
            )

            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessAddToPlanResponse(data: String, dialogChooseMealDay: Dialog) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                dataList.clear()
                dialogChooseMealDay.dismiss()
                Toast.makeText(requireContext(), apiModel.message, Toast.LENGTH_LONG).show()
            } else {
                handleError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    override fun itemSelect(position: Int?, status: String?, type: String?) {
        val position = viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.indexOfFirst { it.id.equals(status,true) }
        viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.forEachIndexed { index, ingredient ->
            if (index == position) {
                ingredient.status = viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.get(position)?.status != true
            }
        }
        // Notify adapter with updated data
        ingredientsRecipeAdapter?.updateList(viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients!!)

        selectAll = viewModel.getRecipeData()?.get(0)!!.recipe?.ingredients?.all { it.status } == true

        // Update the drawable based on the selectAll state
        val drawableRes = if (selectAll) R.drawable.orange_checkbox_images else R.drawable.orange_uncheck_box_images
        binding.tvSelectAllBtn.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableRes, 0)

    }

}