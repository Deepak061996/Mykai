package com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterCookIngredientsItem
import com.mykaimeal.planner.adapter.AdapterCreateIngredientsItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.commonworkutils.MediaUtility
import com.mykaimeal.planner.databinding.FragmentCreateRecipeBinding
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.CreateRecipeNameModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.CreateRecipeNameModelData
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.CreateRecipeSuccessModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.Images
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.Recipe
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.RecyclerViewCookIngModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.SMALL
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.viewmodel.CreateRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefromimage.model.RecyclerViewItemModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File


@AndroidEntryPoint
class CreateRecipeFragment : Fragment(), AdapterCreateIngredientsItem.UploadImage {

    private lateinit var binding: FragmentCreateRecipeBinding
    private var file: File? = null
    private var ingredientList: MutableList<RecyclerViewItemModel> =  mutableListOf()
    private var cookList: MutableList<RecyclerViewCookIngModel> = mutableListOf()
    private var adapter: AdapterCreateIngredientsItem? = null
    private var position: Int = 0
    private var checkBase64Url:Boolean?=false
    private var recipeMainImageUri: String? = null
    private var recipeStatus: String? = "0"
    private var uri: String? = ""
    private var adapterCook: AdapterCookIngredientsItem? = null
    private lateinit var createRecipeViewModel: CreateRecipeViewModel
    private lateinit var commonWorkUtils: CommonWorkUtils
    private var recipeName:String?=""
    private var screenName:String?="Create Recipe"
    private var cookBook:String?="Favorites"
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> =
        mutableListOf()
    private lateinit var recipe:Recipe
    private lateinit var session: SessionManagement
    private var changeStatus="Yes"



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        binding = FragmentCreateRecipeBinding.inflate(layoutInflater, container, false)
        
        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.GONE
            it.llBottomNavigation.visibility = View.GONE
        }

        commonWorkUtils = CommonWorkUtils(requireActivity())
        session = SessionManagement(requireActivity())
        createRecipeViewModel = ViewModelProvider(requireActivity())[CreateRecipeViewModel::class.java]
        recipeName = arguments?.getString("name", "")?:""
        screenName = arguments?.getString("screenName", "Create Recipe")?:"Create Recipe"
        cookBook = arguments?.getString("CookBook", "Favorites")?:"Favorites"

        Log.d("recipeName", "******$recipeName")
        backButton()

        initialize()

        return binding.root
    }


    private fun backButton(){
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    addRecipeDiscardDialog()
                }
            })
    }


    private val pickImageLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                file = MediaUtility.getPath(requireContext(), uri)?.let { File(it) }
                binding.addImageIcon.visibility = View.GONE
                Glide.with(requireActivity())
                    .asBitmap() // Important: get Bitmap instead of Drawable
                    .load(uri)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.layProgess.root.visibility = View.GONE
                            binding.addImageIcon.visibility = View.VISIBLE
                            binding.imgEdit.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.layProgess.root.visibility = View.GONE
                            binding.addImageIcon.visibility = View.GONE
                            binding.imgEdit.visibility = View.VISIBLE
                            // Convert bitmap to base64
                            resource?.let {
                                recipeMainImageUri = bitmapToBase64(it)
                                Log.d("Base64", recipeMainImageUri!!)
                            }
                            return false
                        }
                    })
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            binding.addImages.setImageBitmap(resource) // Set the image
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Handle cleanup
                        }
                    })
            }
        }
    }


    private fun base64ToBitmap(base64Str: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun initialize() {

        binding.tvToolbar.text= screenName?: "Create Recipe"

        changeStatus = if (screenName.equals("Create Recipe")){
            "Yes"
        }else{
            "No"
        }

        cookbookList.clear()

        val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("", "", 0, "", "Favorites", 0, "", 0)
        cookbookList.add(0, data)

        binding.spinnerCookBook.setIsFocusable(true)

        // Add the first blank EditText item
        ingredientList.add(RecyclerViewItemModel("", "", false,"",""))

        // Set up RecyclerView and Adapter
        adapter = AdapterCreateIngredientsItem(ingredientList, requireActivity(), this){ updatedPosition, updatedItem ->
            updatedItem.status = updatedItem.ingredientName?.isNotBlank() == true &&
                    updatedItem.quantity?.isNotBlank() == true &&
                    updatedItem.measurement?.isNotBlank() == true
            ingredientList[updatedPosition] = updatedItem
           binding.rcyCreateIngredients.adapter?.notifyItemChanged(updatedPosition)
        }

        binding.rcyCreateIngredients.adapter = adapter

        // Ingredients Handle "+" button click
        binding.imageCrtIngPlus.setOnClickListener {
            var result = true // Default to true, assuming all values are filled

            // Iterate through each item in the ingredientList and check if all values are filled
            ingredientList.forEachIndexed { _, item ->
                if (item.ingredientName?.isBlank() == true ||
                    item.quantity?.isBlank() == true ||
                    item.measurement?.isBlank() == true) {
                    // If any field is blank in the current position, set result to false
                    result = false
                    // Show a toast message indicating which position has missing values
                    Toast.makeText(requireContext(), ErrorMessage.ingredientInstructions, Toast.LENGTH_LONG).show()
                    return@forEachIndexed // Exit early after finding the first invalid item
                }
            }
            // If all values are filled, add a new ingredient; otherwise, do nothing
            if (result) {
                ingredientList.add(RecyclerViewItemModel("", "", false, "", ""))
                adapter?.update(ingredientList)
            }
        }

        // Update the model when quantity changes
        binding.etRecipeName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateBackground(binding.llCreateTitle, s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Initialize with Step-1 by default
        cookList.add(RecyclerViewCookIngModel(1))

        // Setup RecyclerView
        adapterCook = AdapterCookIngredientsItem(cookList,requireActivity()){ updatedPosition, updatedItem ->
            cookList[updatedPosition] = updatedItem
            binding.rcyCookInstructions.adapter?.notifyItemChanged(updatedPosition)
        }
        binding.rcyCookInstructions.adapter=adapterCook

        binding.imageCookIns.setOnClickListener {
            // Check if any item has a blank or null description
            val hasEmptyField = cookList.any { it.description.isNullOrBlank() }
            if (hasEmptyField) {
                Toast.makeText(requireContext(), ErrorMessage.validCookingInstructions, Toast.LENGTH_LONG).show()
            } else {
                // All descriptions are filled, so add a new step
                cookList.add(RecyclerViewCookIngModel(1)) // You can update '1' to meaningful data if needed
                adapterCook?.update(cookList)
            }
        }


        // serving count - and +
        binding.imgMinus.setOnClickListener {
            val currentValue = binding.textValue.text.toString().toInt()
            if (currentValue > 1) {
                updateValue(currentValue - 1)
            }
        }

        binding.imgPlus.setOnClickListener {
            val currentValue = binding.textValue.text.toString().toInt()
            if (currentValue < 99) {
                updateValue(currentValue + 1)
            }
        }

        // backButton handle
        binding.imageBackIcon.setOnClickListener {
            addRecipeDiscardDialog()
        }

        // save button handle
        binding.layBottom.setOnClickListener {
            if (validate()) {
                if (BaseApplication.isOnline(requireActivity())) {
                    (activity as MainActivity?)?.upDateCookBook()
                    createRecipeApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }

        // Private button handle
        binding.textPrivate.setOnClickListener {
            radioButton(true)
        }

        // Public button handle
        binding.textPublic.setOnClickListener {
            radioButton(false)
        }

        // add Image handle
        binding.addImages.setOnClickListener {
            if (binding.imgEdit.visibility == View.GONE){
                openCameraGallery(false)
            }
        }

        binding.imgEdit.setOnClickListener {
            openCameraGallery(false)
        }

        // cookBookList
        if (BaseApplication.isOnline(requireActivity())) {
            getCookBookList()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }

    }

    private fun radioButton(type: Boolean) {
        if (type){
            recipeStatus="0"
            binding.textPublic.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_uncheck_gray_icon, 0, 0, 0)
            binding.textPrivate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_check_icon, 0, 0, 0)
        }else{
            binding.textPublic.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_check_icon, 0, 0, 0)
            binding.textPrivate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_uncheck_gray_icon, 0, 0, 0)
            recipeStatus="1"
        }
    }

    private fun validate(): Boolean {

        var result = true // Default to true, assuming all values are filled

        // Iterate through each item in the ingredientList and check if all values are filled
        ingredientList.forEachIndexed { _, item ->
            if (item.ingredientName?.isBlank() == true ||
                item.quantity?.isBlank() == true ||
                item.measurement?.isBlank() == true) {
                // If any field is blank in the current position, set result to false
                result = false
                return@forEachIndexed // Exit early after finding the first invalid item
            }
        }

        val hasEmptyField = cookList.any { it.description.isNullOrBlank() }

        if (binding.etRecipeName.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.recipeName, false)
            return false
        } else if (!result) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.ingredientInstructions, false)
            return false
        } else if (hasEmptyField) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.validCookingInstructions, false)
            return false
        }else if (binding.edtTotalTime.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.validTotalTime, false)
            return false
        }
        return true
    }

    private fun updateBackground(llCreateTitle: LinearLayout, text: String) {
        if (text.isNotEmpty()) {
            llCreateTitle.setBackgroundResource(R.drawable.create_select_bg) // Change this drawable
        } else {
            llCreateTitle.setBackgroundResource(R.drawable.create_unselect_bg)  // Default background
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun createRecipeApi() {
        lifecycleScope.launch {
            try {
                val jsonObject = JsonObject()
                Log.d("fdfdf", "ffd:--0" + ingredientList.size)
                // Create a JsonArray for ingredients
                val ingArray = JsonArray()
                val prepArray = JsonArray()
                ingredientList.forEach { item ->
                    val quantity = item.quantity
                    val unit = item.measurement ?: "null"
                    val name = item.ingredientName
                    val ingredientString = if (unit.equals("<unit>", true) || unit.equals("null", true) || unit.isBlank()) {
                        "$quantity $name"
                    } else {
                        "$quantity $unit $name"
                    }
                    ingArray.add(ingredientString)
                }
                // Prepare prep steps
                cookList.forEach { items ->
                    prepArray.add(items.description)
                }

                val matchedId = cookbookList
                    .find { it.name.equals(binding.spinnerCookBook.text.toString(), ignoreCase = true) }
                    ?.id

               /* val cookBookID = if (binding.spinnerCookBook.text.toString().equals("Favorites",true)) {
                    cookbookList[0].id.toString()
                }else{
                    cookbookList[binding.spinnerCookBook.selectedIndex].id.toString()
                }*/


                if (!screenName.equals("Create Recipe")){
                    jsonObject.addProperty("uri", uri)
                }

                // Add data to JSON object
                jsonObject.addProperty("recipe_key", recipeStatus.toString())
                jsonObject.addProperty("cook_book", matchedId)
                jsonObject.addProperty("title", binding.etRecipeName.text.toString().trim())
                jsonObject.add("ingr", ingArray)
                jsonObject.addProperty("summary", binding.edtSummary.text.toString().trim())
                jsonObject.addProperty("yield", binding.textValue.text.toString().trim())
                jsonObject.addProperty("totalTime", binding.edtTotalTime.text.toString().trim())
                jsonObject.addProperty("is_public", recipeStatus.toString())
                jsonObject.add("prep", prepArray)
                jsonObject.addProperty("img", recipeMainImageUri ?: "")  // Ensure it's not null
                Log.d("json object", "******$jsonObject")
                BaseApplication.showMe(requireContext())
                // Call API after everything is ready
                createRecipeViewModel.createRecipeRequestApi({
                    BaseApplication.dismissMe()
                    handleApiCreateRecipeResponse(it)
                }, jsonObject,screenName.toString())
            }catch (e:Exception){
                Log.d("@Error ","*********"+e.message)
            }
        }
    }

    private fun handleApiCreateRecipeResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCreateApiResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    private fun getCookBookList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            createRecipeViewModel.getCookBookRequest {
                BaseApplication.dismissMe()
                handleApiCookBookResponse(it)
            }
        }
    }


    private fun searchRecipeByNameApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            createRecipeViewModel.recipeSearchApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val recipeNameModel = gson.fromJson(it.data, CreateRecipeNameModel::class.java)
                            if (recipeNameModel.code == 200 && recipeNameModel.success == true) {
                                showDataInUi(recipeNameModel.data)
                            } else {
                                if (recipeNameModel.code == ErrorMessage.code) {
                                    showAlert(recipeNameModel.message, true)
                                }else{
                                    showAlert(recipeNameModel.message, false)
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
            },recipeName)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDataInUi(recipeNameModelData: List<CreateRecipeNameModelData>?) {
        try {
            if (recipeNameModelData!=null){
                recipe = recipeNameModelData[0].recipe!!
                setData(recipe)
            }
        }catch (e:Exception){
            Log.d("CreateRecipe:","Message:--"+e.message)
        }
    }


    private fun isBase64Image(data: String): Boolean {
        // Agar "data:image" prefix ke sath ho
        if (data.startsWith("data:image") && data.contains("base64,")) {
            val base64Str = data.substringAfter("base64,")
            return try {
                val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) != null
            } catch (e: Exception) {
                false
            }
        } else {
            // Agar plain Base64 string ho (without prefix)
            return try {
                val decodedBytes = Base64.decode(data, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size) != null
            } catch (e: Exception) {
                false
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun setData(recipe: Recipe) {

        if (recipe !=null){
            if (recipe.label !=null){
                binding.etRecipeName.setText(recipe.label.toString())
            }
            if (recipe.images?.SMALL?.url!=null){
                val imageUrl =   if (isBase64Image(recipe.images.SMALL.url)){
                    base64ToBitmap(recipe.images.SMALL.url)
                }else{
                    recipe.images.SMALL.url
                }
                checkBase64Url=false
                Glide.with(requireActivity())
                    .asBitmap() // Important: get Bitmap instead of Drawable
                    .load(imageUrl)
                    .error(R.drawable.no_image)
                    .placeholder(R.drawable.no_image)
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.layProgess.root.visibility = View.GONE
                            binding.addImageIcon.visibility = View.VISIBLE
                            binding.imgEdit.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Bitmap?,
                            model: Any?,
                            target: Target<Bitmap>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.layProgess.root.visibility = View.GONE
                            binding.addImageIcon.visibility = View.GONE
                            binding.imgEdit.visibility = View.VISIBLE
                            // Convert bitmap to base64
                            resource?.let {
                                recipeMainImageUri = bitmapToBase64(it)
                                Log.d("Base64", recipeMainImageUri!!)
                            }
                            return false
                        }
                    })
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            binding.addImages.setImageBitmap(resource) // Set the image
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Handle cleanup
                        }
                    })

            }else{
                binding.layProgess.root.visibility= View.GONE
                binding.addImageIcon.visibility= View.VISIBLE
            }

            if (cookBook!=null){
                binding.spinnerCookBook.text=cookBook
            }

            if (recipe.uri!=null){
                uri= recipe.uri
            }

            if (recipe.servings!=null){
                binding.textValue.text=""+ recipe.servings
            }

            if (recipe.description!=null){
                binding.edtSummary.setText(recipe.description)
            }

            if (recipe.is_public!=null){
                if (recipe.is_public == 0){
                    recipeStatus="0"
                    binding.textPublic.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_uncheck_gray_icon, 0, 0, 0)
                    binding.textPrivate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_check_icon, 0, 0, 0)
                }else{
                    binding.textPublic.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_check_icon, 0, 0, 0)
                    binding.textPrivate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_uncheck_gray_icon, 0, 0, 0)
                    recipeStatus="1"
                }
            }else{
                recipeStatus="0"
                binding.textPublic.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_uncheck_gray_icon, 0, 0, 0)
                binding.textPrivate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.radio_check_icon, 0, 0, 0)
            }

            if (session.getCreateRecipe().toString().equals("",true)){
                if (recipe.instructionLines!=null){
                    // Map the instruction lines to RecyclerViewCookIngModel
                    cookList = recipe.instructionLines.mapIndexed { index, instruction ->
                        RecyclerViewCookIngModel(
                            count = index + 1,
                            description = instruction,
                            status = false
                        )
                    }.toMutableList()
                    adapterCook?.update(cookList)
                }
            }else{
                cookList.clear()
                recipe.cookList?.let { cookList.addAll(it) }
                adapterCook?.update(cookList)
            }

            if (recipe.totalTime!=null && recipe.totalTime!=0){
                binding.edtTotalTime.setText(recipe.totalTime.toString())
            }

            if (session.getCreateRecipe().toString().equals("",true)){
                if (recipe.ingredients!=null){
                    // Map the response values to your IngredientModel list
                    ingredientList = recipe.ingredients.map { response ->
                        if (!response.food.toString().equals("",true) && !response.quantity.toString().equals("",true)){
                            RecyclerViewItemModel(uri = response.image, ingredientName = response.food.toString(), quantity = response.quantity.toString(), measurement = response.measure.toString(), status = true)
                        }else{
                            RecyclerViewItemModel(uri = response.image, ingredientName = response.food.toString(), quantity = response.quantity.toString(), measurement = response.measure.toString(), status = false)
                        }
                    }.toMutableList()
                    adapter?.update(ingredientList)
                }
            }else{
                ingredientList.clear()
                recipe.ingredientList?.let { ingredientList.addAll(it) }
                adapter?.update(ingredientList)
            }

        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }

        if (session.getCreateRecipe().toString().equals("",true)){
            if (recipeName!=""){
                if (BaseApplication.isOnline(requireActivity())) {
                    searchRecipeByNameApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }else{
            val gson = Gson()
            val recipeFromJson: Recipe = gson.fromJson(session.getCreateRecipe(), Recipe::class.java)
            recipe = recipeFromJson
            cookBook=recipe.cookBook
            setData(recipe)
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

                }
                binding.spinnerCookBook.setItems(cookbookList.map { it.name })
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

    @SuppressLint("SetTextI18n", "SuspiciousIndentation")
    private fun handleSuccessCreateApiResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CreateRecipeSuccessModel::class.java)
                 Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                     addRecipeSuccessDialog()
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

    @SuppressLint("SetTextI18n")
    private fun updateValue(value:Int) {
        binding.textValue.text =""+value
    }

    private fun addRecipeDiscardDialog() {
        val dialogDiscard: Dialog = context?.let { Dialog(it) }!!
        dialogDiscard.setContentView(R.layout.alert_dialog_discard_recipe)
        dialogDiscard.window!!.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialogDiscard.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvDialogYesBtn = dialogDiscard.findViewById<TextView>(R.id.tvDialogYesBtn)
        val tvDialogNoBtn = dialogDiscard.findViewById<TextView>(R.id.tvDialogNoBtn)
        dialogDiscard.show()
        dialogDiscard.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        tvDialogYesBtn.setOnClickListener {
            dialogDiscard.dismiss()
            changeStatus="No"
            session.setCreateRecipe("")
            findNavController().navigateUp()
        }

        tvDialogNoBtn.setOnClickListener {
            changeStatus="Yes"
            storeDataLocal()
            dialogDiscard.dismiss()
            findNavController().navigateUp()
        }
    }


    private fun storeDataLocal(){
        if (changeStatus.equals("Yes",true)){
            if (screenName.equals("Create Recipe")){
                val totalTime: Int = binding.edtTotalTime.text.toString().toIntOrNull() ?: 0
                val images= Images(null,null, SMALL(0,recipeMainImageUri.toString(),0),null)
                val recipeStore = Recipe(
                    calories = null,
                    cautions = null,
                    cuisineType = null,
                    dietLabels = null,
                    digest = null,
                    dishType = null,
                    healthLabels = null,
                    image = null,
                    images = images,
                    ingredientLines = null,
                    ingredients = null,
                    instructionLines = null,
                    label = binding.etRecipeName.text.toString(),
                    mealType = null,
                    shareAs = null,
                    source = null,
                    description = binding.edtSummary.text.toString(),
                    totalDaily = null,
                    totalNutrients = null,
                    totalTime = totalTime,
                    is_public = recipeStatus?.toInt(),
                    servings = binding.textValue.text.toString().toIntOrNull() ?: 0,
                    totalWeight = null,
                    uri = uri,
                    cookBook =binding.spinnerCookBook.text.toString(),
                    yield = binding.textValue.text.toString().toIntOrNull() ?: 0,
                    ingredientList=ingredientList,
                    cookList=cookList
                )
                /*  val recipeStore = Recipe(recipe.calories?:null, recipe.cautions?:null,recipe.cuisineType?:null, recipe.dietLabels, recipe.digest,recipe.dishType,
                  recipe.healthLabels, recipe.image, recipe.images, recipe.ingredientLines, recipe.ingredients, recipe.instructionLines,
                  binding.etRecipeName.text.toString(), recipe.mealType, recipe.shareAs, recipe.source,binding.edtSummary.text.toString(), recipe.totalDaily, recipe.totalNutrients,
                  totalTime,recipeStatus?.toInt(), binding.textValue.text.toString().toInt(), recipe.totalWeight, recipe.uri, recipe.url,binding.textValue.text.toString().toInt())*/
                val gson = Gson()
                // Recipe object ko JSON string me convert karna
                val recipeJson: String = gson.toJson(recipeStore)
                session.setCreateRecipe(recipeJson)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun addRecipeSuccessDialog() {
        val dialogSuccess: Dialog? = context?.let { Dialog(it) }
        dialogSuccess?.setContentView(R.layout.alert_dialog_add_recipe_success)
        dialogSuccess?.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialogSuccess.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val rlOkayBtn = dialogSuccess.findViewById<RelativeLayout>(R.id.rlOkayBtn)
        val tvSuccessDesc = dialogSuccess.findViewById<TextView>(R.id.tvSuccessDesc)

        if (screenName.equals("Create Recipe")){
            tvSuccessDesc.text="Your Recipe has been saved\n successfully."
        }else{
            tvSuccessDesc.text="Your Recipe has been edited\n successfully."
        }

        dialogSuccess.show()
        dialogSuccess.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        rlOkayBtn.setOnClickListener {
            changeStatus="No"
            dialogSuccess.dismiss()
            if (screenName.equals("Create Recipe")){
                findNavController().navigate(R.id.planFragment)
            }else{
                findNavController().navigateUp()
            }
        }
    }

    override fun uploadImage(pos: Int) {
        position = pos
        openCameraGallery(true)
    }

    private fun openCameraGallery(type:Boolean){
        if (type){
            ImagePicker.with(requireActivity())
                .crop() // Crop image (Optional)
                .compress(1024 * 5) // Compress the image to less than 5 MB
                .maxResultSize(250, 250) // Set max resolution
                .createIntent { intent -> pickImageLauncher1.launch(intent) }
        }else{
            ImagePicker.with(requireActivity())
                .crop() // Crop image (Optional)
                .compress(1024 * 5) // Compress the image to less than 5 MB
                .maxResultSize(250, 250) // Set max resolution
                .createIntent { intent -> pickImageLauncher.launch(intent) }
        }
    }

    private val pickImageLauncher1: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                result.data?.data?.let { uri ->
                    val localData=ingredientList[position]
                    localData.uri=uri.toString()
                    localData.status=false
                    ingredientList[position] = localData
                    adapter?.update(ingredientList)
                }
            }catch (e:Exception){
                BaseApplication.alertError(requireContext(), e.message, false)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        storeDataLocal()
    }

    override fun onStop() {
        super.onStop()
        storeDataLocal()
    }

}