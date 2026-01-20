package com.mykaimeal.planner.fragment.mainfragment.addrecipetab

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.NewCookWareCreateRecipeAdapter
import com.mykaimeal.planner.adapter.NewIngredientsCreateRecipeAdapter
import com.mykaimeal.planner.adapter.NewRecipeAdapter
import com.mykaimeal.planner.adapter.SearchIngredientItemAdapter
import com.mykaimeal.planner.adapter.SearchUnitItemAdapter
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.basedata.SessionManagement
import com.mykaimeal.planner.commonworkutils.CommonWorkUtils
import com.mykaimeal.planner.commonworkutils.MediaUtility
import com.mykaimeal.planner.databinding.FragmentNewCreateRecipeBinding
import com.mykaimeal.planner.di.TimePickerView
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.convertmodel.ConvertModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.model.CreateRecipeSuccessModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.createrecipefragment.viewmodel.CreateRecipeViewModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.CookWare
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Ingredient
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Instruction
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Recipe
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.RecipeModel
import com.mykaimeal.planner.fragment.mainfragment.addrecipetab.unitList.UnitModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.ingredientresponsemodel.Data
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.ingredientresponsemodel.IngredientModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.CookBookListResponse
import com.mykaimeal.planner.listener.CreateRecipeListener
import com.mykaimeal.planner.listener.CreateRecipeSelectListener
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import androidx.core.graphics.drawable.toDrawable


@AndroidEntryPoint
class NewCreateRecipeFragment : Fragment(), CreateRecipeListener, CreateRecipeSelectListener {

    private lateinit var binding: FragmentNewCreateRecipeBinding
    private var serving = 1
    private var recipeStatus: String? = "0"
    private var file: File? = null
    private var uri: String? = ""
    private var checkBase64Url:Boolean?=false
    private var recipeMainImageUri: String? = null
    private lateinit var adapterIngredientsCreateRecipeAdapter: NewIngredientsCreateRecipeAdapter
    private lateinit var adapterCookWareCreateRecipeAdapter: NewCookWareCreateRecipeAdapter
    private lateinit var adapterSearchIngredient: SearchIngredientItemAdapter
    private lateinit var adapterRecipeAdapter: NewRecipeAdapter
    private var recipeList:MutableList<Instruction> = mutableListOf()
    private var cookwareList:MutableList<CookWare> = mutableListOf()
    private var cookwareSelectPostion: Int? = null
    private var recipeSelectPostion: Int? = null
    private var ingredientSelectPostion: Int? = null
    private var ingredientList:MutableList<Ingredient> = mutableListOf()
    private lateinit var createRecipeViewModel: CreateRecipeViewModel
    private lateinit var session: SessionManagement
    private lateinit var commonWorkUtils: CommonWorkUtils
    private var cookbookList: MutableList<com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data> = mutableListOf()
    private var recipeName:String?=""
    private var lastSelect:String?="ingredient"
    private var apiCall:String?=""
    private var screenName:String?="Create Recipe"
    private var cookBook:String?="Favorites"
    private var cookBookId:String?="0"
    private var changeStatus="Yes"
    private lateinit var textListener: TextWatcher
    private lateinit var textListenerCookWare: TextWatcher
    private var textChangedJob: Job? = null
    private lateinit var popupWindow : PopupWindow
    private lateinit var popupWindowUnit : PopupWindow
    private var ingredientStatus="yes"
    private var cookWareStatus="yes"
    private var loadImage=""
    private var loadCookWareImage=""
    private var selectType = "1"
    private var source_url = ""
    private var convertUnitStatus="Original"
    private lateinit var dialogWeight :BottomSheetDialog
    private var dataRecipe : Recipe? = null

    private val keyboardListener = ViewTreeObserver.OnGlobalLayoutListener {
        val rootView = requireActivity().window.decorView
        val rect = Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val keypadHeight = screenHeight - rect.bottom

        if (keypadHeight > screenHeight * 0.15) {
            // Keyboard open
            if (binding.edAmount.isFocused) {
                binding.customAccessoryView.visibility = View.VISIBLE
            }
        } else {
            // Keyboard closed
            binding.customAccessoryView.visibility = View.GONE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentNewCreateRecipeBinding.inflate(layoutInflater, container, false)


        // Call setupUI with the root view of your activity
        setupUI(binding.root)

        // Attach keyboard listener
        requireActivity().window.decorView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardListener)

        (activity as? MainActivity)?.binding?.let {
            it.llIndicator.visibility = View.GONE
            it.llBottomNavigation.visibility = View.GONE
        }

        cookbookList.clear()
        val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("", "", 0, "", "Favorites", 0, "", 0)
        cookbookList.add(0, data)

        createRecipeViewModel = ViewModelProvider(requireActivity())[CreateRecipeViewModel::class.java]

        recipeName = arguments?.getString("name", "")?:""
        apiCall = arguments?.getString("apiCall", "")?:""
        screenName = arguments?.getString("screenName", "Create Recipe")?:"Create Recipe"
        cookBook = arguments?.getString("CookBook", "Favorites")?:"Favorites"
        cookBookId = arguments?.getString("cookBookId", "0")?:"0"


        Log.d("recipeName", "*****$recipeName")

        binding.spinnerCookBook.setIsFocusable(true)
        commonWorkUtils = CommonWorkUtils(requireActivity())
        session = SessionManagement(requireActivity())
        createRecipeViewModel = ViewModelProvider(requireActivity())[CreateRecipeViewModel::class.java]

        adapterIngredientsCreateRecipeAdapter=NewIngredientsCreateRecipeAdapter(requireActivity(),ingredientList,this)
        adapterCookWareCreateRecipeAdapter=NewCookWareCreateRecipeAdapter(requireActivity(),cookwareList,this)
        adapterRecipeAdapter=NewRecipeAdapter(requireActivity(),recipeList,this)

        binding.rcyIngredient.adapter=adapterIngredientsCreateRecipeAdapter
        binding.rcyCookWare.adapter=adapterCookWareCreateRecipeAdapter
        binding.rcyRecipe.adapter=adapterRecipeAdapter

        if (!apiCall.equals("",true)){
            loadRecipe()
        }else{
            if (!session.getCreateRecipe().equals("")){
                val gson = Gson()
                val recipeFromJson: Recipe = gson.fromJson(session.getCreateRecipe(), Recipe::class.java)
                dataRecipe = recipeFromJson
                cookBook=dataRecipe?.cookBook
                cookBookId=dataRecipe?.cookBookId
                apiCall=dataRecipe?.apiCall
                recipeName=dataRecipe?.recipeName
                lastSelect=dataRecipe?.lastSelect
                convertUnitStatus= dataRecipe?.convertSelect.toString()
                loadImage=dataRecipe?.ingredientImg.toString()
                dataRecipe?.let {
                    setData(it)
                }

            }
        }

        binding.tvToolbar.text = screenName

        setClickEvent()

        backButton()

        return binding.root
    }

    private fun loadRecipe(){
        if (BaseApplication.isOnline(requireActivity())) {
            getRecipeDetails()
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun getRecipeDetails(){
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            createRecipeViewModel.recipeSearchFromURLApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val gson = Gson()
                            val recipeNameModel = gson.fromJson(it.data, RecipeModel::class.java)
                            if (recipeNameModel.code == 200 && recipeNameModel.success) {
                                recipeNameModel.data?.let { it1 -> showDataInUi(it1) }
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
            },recipeName,apiCall)
        }
    }

    private fun showDataInUi(data: MutableList<com.mykaimeal.planner.fragment.mainfragment.addrecipetab.recipemodel.Data>) {
        dataRecipe = data[0].recipe
        dataRecipe?.let {
            setData(it)
        }

    }

    @SuppressLint("SetTextI18n")
    private fun setData(recipe:Recipe) {
        try {
            if (recipe !=null){

                if (recipe.label !=null){
                    binding.etRecipeName.setText(recipe.label)
                }
                if (recipe.image!=null){
                    val imageUrl =   if (isBase64Image(recipe.image)){
                        base64ToBitmap(recipe.image)?:""
                    }else{
                        recipe.image
                    }

                    checkBase64Url=false
                    Glide.with(requireActivity())
                        .asBitmap()
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

                if (recipe.source_url!=null){
                    source_url= recipe.source_url
                }

                if (recipe.createdType!=null){
                    apiCall= recipe.createdType
                }

                if (recipe.servings!=null){
                    serving = recipe.servings.toInt()
                    binding.tvServing.text=""+recipe.servings +" Servings"
                }else{
                    serving = 1
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

                if (recipe.prep_time!=null){
                    binding.tvPrepTime.text=""+recipe.prep_time+" min"
                }


                if (recipe.total_time!=null){
                    binding.tvcookTime.text=
                        "" + ((recipe.total_time - recipe.prep_time!!) ?: 0) + " min"
                }

                recipeList.clear()
                cookwareList.clear()
                ingredientList.clear()

                recipe.instructions?.let { instructions ->
                    instructions.mapIndexed { index, instruction ->
                        recipeList.add(Instruction(
                            header = instruction.header,
                            step_order = instruction.step_order,
                            text = instruction.text,
                            status = "1",
                            itemId = index
                        ))
                    }
                    adapterRecipeAdapter.updateList(recipeList)
                }


                recipe.cookware?.let {
                    cookwareList.addAll(it)
                    adapterCookWareCreateRecipeAdapter.updateList(cookwareList)
                }

                recipe.ingredients?.let { ingredientData->
                    ingredientData.mapIndexed { index, instruction ->
                        ingredientList.add(Ingredient(
                            header = instruction.header,
                            image = instruction.image,
                            image_url = instruction.image_url,
                            name = instruction.name,
                            quantity = instruction.quantity,
                            text = instruction.text,
                            unit = instruction.unit,
                            itemId = index
                        ))
                    }
                    adapterIngredientsCreateRecipeAdapter.updateList(ingredientList)
                }
                selectHeader()
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

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
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


    @SuppressLint("ClickableViewAccessibility")
    private fun setupUI(view: View) {
        // Hide keyboard if the touched view is not EditText
        if (view !is EditText  && view.id != binding.btnHalf.id
            && view.id != binding.btnThird.id && view.id != binding.btnQuarter.id && view.id != binding.btnEight.id
            && view.id != binding.btnTwoFour.id && view.id != binding.btnThreeFour.id) {
            view.setOnTouchListener { _, _ ->
                hideKeyboard(view)
                false
            }
        }
        // If the view is a container, loop through its children
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                setupUI(child)
            }
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("SetTextI18n", "ClickableViewAccessibility")
    private fun setClickEvent() {

        binding.imageBackIcon.setOnClickListener {
            addRecipeDiscardDialog()
        }

        binding.btnHeader.setOnClickListener {
            if (binding.layHrader.visibility == View.VISIBLE){
                binding.layHrader.visibility = View.GONE
                binding.btnHeader.text="+ Header"
                binding.btnHeader.setTextColor(Color.parseColor("#FE9F45"))
            }else{
                binding.layHrader.visibility = View.VISIBLE
                binding.btnHeader.text="Header"
                binding.btnHeader.setTextColor(Color.parseColor("#3C4541"))
            }
        }

        binding.btnHeaderRecipe.setOnClickListener {
            if (binding.layHeaderRecipe.visibility == View.VISIBLE){
                binding.layHeaderRecipe.visibility = View.GONE
                binding.btnHeaderRecipe.text="+ Header"
                binding.btnHeaderRecipe.setTextColor(Color.parseColor("#FE9F45"))
            }else{
                binding.layHeaderRecipe.visibility = View.VISIBLE
                binding.btnHeaderRecipe.text="Header"
                binding.btnHeaderRecipe.setTextColor(Color.parseColor("#3C4541"))
            }
        }

        binding.imgCrossIngredient.setOnClickListener {
            binding.layHrader.visibility = View.GONE
            binding.btnHeader.text="+ Header"
            binding.ingredientHeader.text.clear()
            binding.btnHeader.setTextColor(Color.parseColor("#FE9F45"))
        }

        binding.imgCrossHeaderRecipe.setOnClickListener {
            binding.layHeaderRecipe.visibility = View.GONE
            binding.btnHeaderRecipe.text="+ Header"
            binding.btnHeaderRecipe.setTextColor(Color.parseColor("#FE9F45"))
            binding.recipeHeader.text.clear()
        }
        binding.llIngredients.setOnClickListener {
            lastSelect="ingredient"
            selectHeader()
        }
        binding.llCookWare.setOnClickListener {
            lastSelect="cookware"
            selectHeader()

        }
        binding.llRecipe.setOnClickListener {
            lastSelect="recipe"
            selectHeader()
        }
        binding.imgPlus.setOnClickListener {
            val currentValue = serving
            if (currentValue < 99) {
                upDateServing(currentValue + 1)
            }
        }
        binding.imgMinus.setOnClickListener {
            val currentValue = serving
            if (currentValue > 1) {
                upDateServing(currentValue - 1)
            }
        }
        binding.textPrivate.setOnClickListener {
            radioButton(true)
        }
        binding.textPublic.setOnClickListener {
            radioButton(false)
        }
        binding.btnPrepTime.setOnClickListener {
            openTimeAlert("prepare")
        }
        binding.btnCookTime.setOnClickListener {
            openTimeAlert("cook")
        }
        binding.addImages.setOnClickListener {
            if (binding.imgEdit.visibility == View.GONE){
                openCameraGallery()
            }
        }
        binding.imgEdit.setOnClickListener {
            openCameraGallery()
        }
        binding.edRecipe.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.edRecipe.setBackgroundResource(R.drawable.create_select_header)
                // Force focus
                binding.edRecipe.requestFocus()
                // Show keyboard
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.edRecipe, InputMethodManager.SHOW_IMPLICIT)
            }
            false
        }
        binding.edRecipe.isSingleLine = false // visually allow multi-line
        binding.edRecipe.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.edRecipe.setRawInputType(InputType.TYPE_CLASS_TEXT) // remove multiline input type for IME
        binding.edRecipe.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || (event == null || (event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN))) {
                val text: String = binding.edRecipe.text.toString().trim()
                if (text.isNotEmpty()) {
                    addRecipe()
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                    binding.edRecipe.text.clear()
                    binding.edRecipe.clearFocus()
                    recipeSelectPostion = null
                    binding.edRecipe.setBackgroundResource(0)
                    binding.layHeaderRecipe.visibility = View.GONE
                    binding.btnHeaderRecipe.text = "+ Header"
                    binding.btnHeaderRecipe.setTextColor(Color.parseColor("#FE9F45"))
                    binding.recipeHeader.text.clear()
                }
                return@setOnEditorActionListener true
            }
            removeObserver()
            false
        }
        binding.recipeHeader.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val text: String =  binding.recipeHeader.text.toString().trim()
                if (text.trim().isNotEmpty()){
                    if (binding.edRecipe.text.toString().trim().isNotEmpty()){
                        addRecipe()
                        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                        binding.edRecipe.text.clear()
                        binding.edRecipe.clearFocus()
                        recipeSelectPostion = null
                        binding.layHeaderRecipe.visibility = View.GONE
                        binding.btnHeaderRecipe.text="+ Header"
                        binding.btnHeaderRecipe.setTextColor(Color.parseColor("#FE9F45"))
                        binding.recipeHeader.text.clear()
                    }
                }
                return@setOnEditorActionListener false
            }
            false
        }
        // This line for cookware section event handle
        binding.edCookWare.setOnTouchListener { _, _ ->
            binding.edCookWare.setBackgroundResource(R.drawable.create_select_header)
            // Force focus
            binding.edCookWare.requestFocus()
            // Show keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.edCookWare, InputMethodManager.SHOW_IMPLICIT)
            false
        }
        binding.edCookWare.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val text: String =  binding.edCookWare.text.toString().trim()
                if (text.trim().isNotEmpty()){
                    addCookWare()
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
                return@setOnEditorActionListener true
            }
            removeObserver()
            false
        }
        textListenerCookWare = object : TextWatcher {
            private var searchFor = "" // Or view.editText.text.toString()
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (searchText != searchFor) {
                    searchFor = searchText
                    textChangedJob?.cancel()
                    // Launch a new coroutine in the lifecycle scope
                    textChangedJob = lifecycleScope.launch {
                        delay(1000)  // Debounce time
                        if (searchText.equals(searchFor,true)) {
                            if (searchText.isNotEmpty() && cookWareStatus.equals("yes",true)){
                                searchApi(searchText,"CookWare")
                            }else{
                                cookWareStatus ="yes"
                            }
                        }
                    }
                }
            }
        }
        // This line for cookBook section event handle
        binding.spinnerCookBook.setOnClickListener {
            cookbookList.clear()
            val data = com.mykaimeal.planner.fragment.mainfragment.viewmodel.planviewmodel.apiresponsecookbooklist.Data("", "", 0, "", "Favorites", 0, "", 0)
            cookbookList.add(0, data)
            if (BaseApplication.isOnline(requireActivity())) {
                getCookBookList()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
        binding.spinnerCookBook.setOnSpinnerItemSelectedListener<String> { _, _, newIndex, newItem ->
            cookBookId=cookbookList[newIndex].id.toString()
            Log.d("SpinnerCookBook", "Selected item: $newItem at position $cookBookId")
        }
        binding.btnSave.setOnClickListener {
            if (validation()){
                if (BaseApplication.isOnline(requireActivity())) {
                    (activity as MainActivity?)?.upDateCookBook()
                    createRecipeApi()
                } else {
                    BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
                }
            }
        }
        binding.edAmount.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                binding.layamount.visibility = View.VISIBLE
                binding.edAmount.setBackgroundResource(R.drawable.create_select_header)
                // Force focus
                binding.edAmount.requestFocus()
                // Show keyboard
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.edAmount, InputMethodManager.SHOW_IMPLICIT)
            }
            false
        }


        binding.edAmount.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val text: String =  binding.edAmount.text.toString().trim()
                if (text.isNotEmpty()){
                    binding.edAmount.setBackgroundResource(0)
                    binding.layamount.visibility = View.VISIBLE
                }
                addIngredient()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                binding.edAmount.clearFocus()
                return@setOnEditorActionListener true
            }
            false
        }

        binding.unitName.setOnClickListener {
            if (BaseApplication.isOnline(requireActivity())) {
                getUnitList()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        setupAccessoryClicks()
        // use Debouncing for text
        textListener = object : TextWatcher {
            private var searchFor = "" // Or view.editText.text.toString()
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString()
                if (searchText != searchFor) {
                    searchFor = searchText
                    textChangedJob?.cancel()
                    // Launch a new coroutine in the lifecycle scope
                    textChangedJob = lifecycleScope.launch {
                        delay(1000)  // Debounce time
                        if (searchText.equals(searchFor,true)) {
                            if (searchText.isNotEmpty() && ingredientStatus.equals("yes",true)){
                                searchApi(searchText,"Ingredient")
                            }else{
                                ingredientStatus ="yes"
                            }
                        }
                    }
                }
            }
        }

        binding.edIngredient.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val text: String =  binding.edIngredient.text.toString().trim()
                if (text.isNotEmpty()){
                    binding.layamount.visibility = View.VISIBLE
                } else{
                    binding.layamount.visibility = View.GONE
                }
                binding.edIngredient.setBackgroundResource(R.drawable.create_select_header)
            }
            false
        }
        binding.edIngredient.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val text: String =  binding.edIngredient.text.toString().trim()
                if (text.isNotEmpty()){
                    binding.layamount.visibility = View.VISIBLE
                    binding.edAmount.setBackgroundResource(R.drawable.create_select_header)
                } else{
                    binding.layamount.visibility = View.GONE
                }
                binding.edIngredient.setBackgroundResource(0)
                addIngredient()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                binding.edIngredient.clearFocus()
                return@setOnEditorActionListener true
            }
            removeObserver()
            false
        }

        binding.btnConvertUnit.setOnClickListener {
            if (ingredientList.size>0){
                openBoxConvertUnit()
            }
        }

    }


    @SuppressLint("SetTextI18n")
    private fun addRecipe(){
        val headerText = binding.recipeHeader.text.toString().trim().ifEmpty {
            "Recipe"
        }
        recipeList.add(recipeSelectPostion?:recipeList.size,Instruction(headerText,0,binding.edRecipe.text.toString(),"1",recipeList.size))
        adapterRecipeAdapter.updateList(recipeList)
        binding.tvStep.text="Step-"+(recipeList.size+1)
    }

    private fun validation():Boolean{
        if (binding.etRecipeName.text.toString().trim().isEmpty()) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.recipeName, false)
            return false
        } else if (ingredientList.size<=0) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.ingredientCreateError, false)
            return false
        } else if (!apiCall.equals("import",true)) {
            if (recipeList.size<=0){
                commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.recipeCreateError, false)
                return false
            }else{
                return true
            }
        }else if (binding.tvPrepTime.text.toString().trim().equals("0 min",true)) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.prepTime, false)
            return false
        }else if (binding.tvcookTime.text.toString().trim().equals("0 min",true)) {
            commonWorkUtils.alertDialog(requireActivity(), ErrorMessage.cookTime, false)
            return false
        }
        return true
    }
    private fun createRecipeApi() {
        lifecycleScope.launch {
            try {
                val jsonObject = JsonObject()
                Log.d("fdfdf", "ffd:--0" + ingredientList.size)
                // Create a JsonArray for ingredients
                val ingArray = JsonArray()
                val ingHeaderArray = JsonArray()
                val prepArray = JsonArray()
                val repHeaderArray = JsonArray()
                val cookArray = JsonArray()

                ingredientList.forEach { item ->
                    val quantity = item.quantity
                    val unit = item.unit ?: "null"
                    val name = item.name
                    val ingredientString = if (unit.equals("<unit>", true) || unit.equals("null", true) || unit.isBlank()) {
                        "$quantity $name"
                    } else {
                        "$quantity $unit $name"
                    }
                    ingArray.add(ingredientString)
                    ingHeaderArray.add(item.header)
                }

                // Prepare prep steps
                recipeList.forEach { items ->
                    prepArray.add(items.text)
                    repHeaderArray.add(items.header)
                }
                // Prepare prep steps
                cookwareList.forEach { items ->
                    cookArray.add(items.name)
                }

                if (!binding.tvToolbar.text.toString().equals("Create Recipe",true)){
                    jsonObject.addProperty("uri", uri)
                }

                if (apiCall.equals("import", ignoreCase = true)) {
                    jsonObject.addProperty("createdType", "import")
                    jsonObject.addProperty("source_url", source_url)
                } else {
                    jsonObject.addProperty("createdType", "create")
                }

                jsonObject.addProperty("recipe_key", recipeStatus.toString())
                jsonObject.addProperty("cook_book", cookBookId)
                jsonObject.addProperty("title", binding.etRecipeName.text.toString().trim())
                jsonObject.add("ingr", ingArray)
                jsonObject.addProperty("summary", binding.edtSummary.text.toString().trim())
                jsonObject.addProperty("yield", binding.tvServing.text.toString().replace("Servings","").trim())
                jsonObject.addProperty("prep_time", binding.tvPrepTime.text.toString().replace("min","").trim())
                jsonObject.addProperty("cook_time", binding.tvcookTime.text.toString().replace("min","").trim())
                jsonObject.addProperty("is_public", recipeStatus.toString())
                jsonObject.add("prep", prepArray)
                jsonObject.add("headers", ingHeaderArray)
                jsonObject.add("steps_headers", repHeaderArray)
                if (cookwareList.size>0){
                    jsonObject.add("cookware", cookArray)
                }
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
            ingredientList.forEach { item ->
                val quantity = item.quantity
                val unit = item.unit.toString()
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
                createRecipeViewModel.convertUnitRequestApi({
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
        ingredientList = ingredientList.mapIndexed { index, ingredient ->
            val data = dataList?.getOrNull(index)
            if (data != null) {
                ingredient.copy(
                    quantity = data.converted,
                    unit = data.target_unit
                )
            } else {
                ingredient
            }
        }.toMutableList()
        adapterIngredientsCreateRecipeAdapter.updateList(ingredientList)
        dialogWeight.dismiss()
    }



    private fun addCookWare(){
        Log.d("loadCookWareImage", "*****$loadCookWareImage")
        cookwareList.add(cookwareSelectPostion?:cookwareList.size,CookWare("1",binding.edCookWare.text.toString(),loadCookWareImage))
        adapterCookWareCreateRecipeAdapter.updateList(cookwareList)
        cookwareSelectPostion=null
        Glide.with(requireContext())
            .load(R.drawable.cookwareicon)
            .into(binding.imgCookWare)
        binding.edCookWare.text.clear()
        binding.edCookWare.clearFocus()
        binding.edCookWare.setBackgroundResource(0)
        loadCookWareImage=""
        cookWareStatus="Yes"
    }

    @SuppressLint("SetTextI18n")
    private fun addIngredient(){
        val headerText = binding.ingredientHeader.text.toString().trim().ifEmpty {
            "Ingredients"
        }
        if (binding.edIngredient.text.trim().isNotEmpty() && binding.edAmount.text.trim().isNotEmpty() && !binding.unitName.text.toString().trim().equals("Select",true)){
            ingredientList.add(ingredientSelectPostion?:ingredientList.size,Ingredient(headerText,loadImage,loadImage,binding.edIngredient.text.toString(),binding.edAmount.text.toString(), binding.edIngredient.text.toString(), binding.unitName.text.toString(),ingredientList.size))
            adapterIngredientsCreateRecipeAdapter.updateList(ingredientList)
            binding.edIngredient.text.clear()
            binding.ingredientHeader.text.clear()
            binding.edAmount.text.clear()
            ingredientSelectPostion = null
            binding.edIngredient.setBackgroundResource(0)
            binding.edAmount.setBackgroundResource(0)
            binding.unitName.text = "Select"
            loadImage=""
            ingredientStatus="yes"
            binding.layamount.visibility = View.GONE
            binding.layHrader.visibility = View.GONE
            binding.btnHeader.text="+ Header"
            binding.btnHeader.setTextColor(Color.parseColor("#FE9F45"))
            Glide.with(requireContext())
                .load(R.drawable.ingredienticon)
                .into(binding.imageProfile)
        }
    }

    private fun searchApi(value:String, changeStatus:String){
        if (BaseApplication.isOnline(requireActivity())) {
            searchRecipeApi(value,changeStatus)
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    private fun searchRecipeApi(searchText: String, changeStatus: String) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            // Create a JsonObject for the main JSON structure
            createRecipeViewModel.recipeSearchIngredientApi({
                BaseApplication.dismissMe()
                when (it) {
                    is NetworkResult.Success -> {
                        try {
                            val apiModel = Gson().fromJson(it.data, IngredientModel::class.java)
                            Log.d("@@@ Recipe Details ", "message :- $it.data")
                            if (apiModel.code == 200 && apiModel.success) {
                                apiModel.data?.let { data->
                                    if (data.size>0){
                                        openIngredient(data,changeStatus)
                                    }
                                }
                            } else {
                                if (apiModel.code == ErrorMessage.code) {
                                    showAlert(apiModel.message, true)
                                } else {
                                    Log.d("AddMeal", "message:--" + apiModel.message)
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("AddMeal", "message:--" + e.message)
                        }
                    }
                    is NetworkResult.Error -> {
                        Log.d("AddMeal", "message:--" + it.message)
                    }
                    else -> {
                        Log.d("AddMeal", "message:--" + it.message)
                    }
                }
            }, searchText,selectType)
        }
    }

    @SuppressLint("InflateParams")
    private fun openIngredient(data: MutableList<Data>, changeStatus: String) {

        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.item_show_data_ingredient, null)

        popupWindow = if (changeStatus.equals("Ingredient",true)){
            PopupWindow(popupView, binding.edIngredient.width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        }else{
            PopupWindow(popupView, binding.edCookWare.width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        }

        val rcyActivity = popupView.findViewById<RecyclerView>(R.id.rcyActivity)
        rcyActivity.layoutManager = LinearLayoutManager(requireContext())
        adapterSearchIngredient= SearchIngredientItemAdapter(data,this,changeStatus)
        rcyActivity.adapter = adapterSearchIngredient
        val marginTop = (5 * resources.displayMetrics.density).toInt()

        if (changeStatus.equals("Ingredient",true)){
            popupWindow.showAsDropDown(binding.edIngredient, 0, marginTop)
        }else{
            popupWindow.showAsDropDown(binding.edCookWare, 0, marginTop)
        }

    }


    @SuppressLint("InflateParams")
    private fun openUit(data: MutableList<com.mykaimeal.planner.fragment.mainfragment.addrecipetab.unitList.Data>) {
        val inflater = requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView: View = inflater.inflate(R.layout.item_show_data_ingredient, null)
        popupWindowUnit = PopupWindow(popupView, binding.unitName.width, ViewGroup.LayoutParams.WRAP_CONTENT, true)
        val rcyActivity = popupView.findViewById<RecyclerView>(R.id.rcyActivity)
        rcyActivity.layoutManager = LinearLayoutManager(requireContext())
        val adapterSearchIngredient= SearchUnitItemAdapter(data,this)
        rcyActivity.adapter = adapterSearchIngredient
        val marginTop = (5 * resources.displayMetrics.density).toInt()
        popupWindowUnit.showAsDropDown(binding.unitName, 0, marginTop)

    }


    private fun setupAccessoryClicks() {
        // Example: edAmount me text insert karna hai
        binding.btnHalf.setOnClickListener {
            binding.edAmount.text.clear()
            insertFraction("1/2")
        }
        binding.btnThird.setOnClickListener {
            binding.edAmount.text.clear()
            insertFraction("1/3")
        }
        binding.btnQuarter.setOnClickListener {
            binding.edAmount.text.clear()
            insertFraction("1/4")
        }
        binding.btnEight.setOnClickListener {
            binding.edAmount.text.clear()
            insertFraction("1/8")
        }
        binding.btnTwoFour.setOnClickListener {
            binding.edAmount.text.clear()
            insertFraction("2/4")
        }
        binding.btnThreeFour.setOnClickListener {
            binding.edAmount.text.clear()
            insertFraction("3/4")
        }
    }

    // Helper function jo value EditText me dal de
    @SuppressLint("SetTextI18n")
    private fun insertFraction(value: String) {
       /* val editText = binding.edAmount
        val current = editText.text.toString()
        val cursorPos = editText.selectionStart
        // Cursor position pe insert karte hain
        val newText = StringBuilder(current).insert(cursorPos, value).toString()
        editText.setText(newText)
        editText.setSelection(cursorPos + value.length) // cursor move kare insert ke baad*/
        binding.edAmount.setText(""+fractionToDecimal(value))
        binding.edAmount.setSelection(binding.edAmount.length())
    }

    @SuppressLint("DefaultLocale")
    private fun fractionToDecimal(value: String): Double {
        return try {
            val result = if (value.contains("/")) {
                val parts = value.split("/")
                if (parts.size == 2) {
                    val numerator = parts[0].trim().toDouble()
                    val denominator = parts[1].trim().toDouble()
                    if (denominator != 0.0) numerator / denominator else 0.0
                } else {
                    value.toDoubleOrNull() ?: 0.0
                }
            } else {
                value.toDoubleOrNull() ?: 0.0
            }
            // Round to 2 decimal places
            String.format("%.2f", result).toDouble()
        } catch (e: Exception) {
            0.0
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

        if (binding.tvToolbar.text.toString().equals("Create Recipe",true)){
            tvSuccessDesc.text="Your Recipe has been saved\n successfully."
        }else{
            tvSuccessDesc.text="Your Recipe has been edited\n successfully."
        }

        dialogSuccess.show()
        dialogSuccess.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        rlOkayBtn.setOnClickListener {
            changeStatus="No"
            dialogSuccess.dismiss()
            if (binding.tvToolbar.text.toString().equals("Create Recipe",true)){
                findNavController().navigate(R.id.planFragment)
            }else{
                findNavController().navigateUp()
            }
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

    private fun getUnitList() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            createRecipeViewModel.getUnitRequest {
                BaseApplication.dismissMe()
                handleApiUnitResponse(it)
            }
        }
    }

    private fun handleApiCookBookResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessCookBookResponse(result.data.toString())
            is NetworkResult.Error -> {
                requireActivity().runOnUiThread {
                    binding.spinnerCookBook.setItems(cookbookList.map { it.name })
                    binding.spinnerCookBook.showOrDismiss()
                }
            }
            else -> {
                requireActivity().runOnUiThread {
                    binding.spinnerCookBook.setItems(cookbookList.map { it.name })
                    binding.spinnerCookBook.showOrDismiss()
                }
            }
        }
    }

    private fun handleApiUnitResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuccessUnitResponse(result.data.toString())
            is NetworkResult.Error -> {
            }
            else -> {
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessCookBookResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, CookBookListResponse::class.java)
            Log.d("@@@ cookbook List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    cookbookList.retainAll { it == cookbookList[0] }
                    cookbookList.addAll(apiModel.data)
                }
                requireActivity().runOnUiThread {
                    binding.spinnerCookBook.setItems(cookbookList.map { it.name })
                    binding.spinnerCookBook.showOrDismiss()
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
      private fun handleSuccessUnitResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, UnitModel::class.java)
            Log.d("@@@ cookbook List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                if (apiModel.data != null && apiModel.data.size > 0) {
                    openUit(apiModel.data)
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


    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun openCameraGallery(){
        ImagePicker.with(requireActivity())
            .crop() // Crop image (Optional)
            .compress(1024 * 5) // Compress the image to less than 5 MB
            .maxResultSize(250, 250) // Set max resolution
            .createIntent { intent -> pickImageLauncher.launch(intent) }
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



    @SuppressLint("SetTextI18n")
    private fun openTimeAlert(type:String){
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialog)
        dialog.setContentView(R.layout.alert_dialog_time)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        val tvTitle: TextView? = dialog.findViewById(R.id.tvtitle)
        val btnSave: TextView? = dialog.findViewById(R.id.btnSave)
        val tvDis: TextView? = dialog.findViewById(R.id.tvdis)
        val dataItemSelect : TimePickerView? = dialog.findViewById(R.id.weekday_time_picker_view)
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
        }
        if (type.equals("prepare",true)){
            tvTitle?.text="Prep time"
            if (!binding.tvPrepTime.text.toString().equals("0 min",true)){
                val showData=fromTotalMinutes(binding.tvPrepTime.text.toString().replace("min","").trim().toInt())
                dataItemSelect?.setTime(showData.first,showData.second)
            }
        }else{
            tvTitle?.text="Cook time"
            if (!binding.tvcookTime.text.toString().equals("0 min",true)){
                val showData=fromTotalMinutes(binding.tvPrepTime.text.toString().replace("min","").trim().toInt())
                dataItemSelect?.setTime(showData.first,showData.second)
            }
        }

        tvDis?.text="How long does it take to $type \nthis recipe?"
        btnSave?.setOnClickListener {
            dialog.dismiss()
            val totalMint =  toTotalMinutes(dataItemSelect?.getHours()?:0 , dataItemSelect?.getMinutes()?:0)
            if (type.equals("prepare",true)){
                 binding.tvPrepTime.text= "$totalMint min"
            }else{
                binding.tvcookTime.text= "$totalMint min"
            }
        }

        dialog.show()

    }

    private fun toTotalMinutes(hours: Int, minutes: Int): Int {
        return (hours * 60) + minutes
    }

    private fun fromTotalMinutes(totalMinutes: Int): Pair<Int, Int> {
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return Pair(hours, minutes)
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


    @SuppressLint("SetTextI18n")
    private fun upDateServing(currentValue: Int) {
        serving=currentValue
        binding.tvServing.text= "$serving Servings"
    }


    private fun addRecipeDiscardDialog() {
        if (binding.tvToolbar.text.toString().equals("Create Recipe",true)){
            val dialogDiscard: Dialog = context?.let { Dialog(it) }!!
            dialogDiscard.setContentView(R.layout.alert_dialog_discard_recipe)
            dialogDiscard.window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialogDiscard.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            val tvDialogYesBtn = dialogDiscard.findViewById<TextView>(R.id.tvDialogYesBtn)
            val tvDialogNoBtn = dialogDiscard.findViewById<TextView>(R.id.tvDialogNoBtn)
            dialogDiscard.show()
            dialogDiscard.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
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
        }else{
            changeStatus="No"
            session.setCreateRecipe("")
            findNavController().navigateUp()
        }
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


    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener to avoid leaks
        removeObserver()
    }

    private fun removeObserver(){
        // Remove listener to avoid leaks
        requireActivity().window.decorView.viewTreeObserver.removeOnGlobalLayoutListener(keyboardListener)
    }

    override fun onResume() {
        super.onResume()
        binding.edIngredient.addTextChangedListener(textListener)
        binding.edCookWare.addTextChangedListener(textListenerCookWare)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    override fun onPause() {
        super.onPause()
        binding.edIngredient.removeTextChangedListener(textListener)
        binding.edCookWare.removeTextChangedListener(textListenerCookWare)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun itemRecipeSelect(id: String?, title: String?, image: String?, unitName: String?, type: String?) {

        if (type.equals("Select",true)){
            popupWindowUnit.dismiss()
            binding.unitName.text = unitName
            addIngredient()
        }

        if (type.equals("Ingredient",true)){
            popupWindow.dismiss()
            binding.edIngredient.setBackgroundResource(0)
            binding.layamount.visibility = View.VISIBLE
            binding.edAmount.setBackgroundResource(R.drawable.create_select_header)
            binding.edAmount.requestFocus()
            loadImage = image?:""
            image?.let {
                Glide.with(requireContext())
                    .load(image)
                    .placeholder(R.drawable.ingredienticon)
                    .error(R.drawable.ingredienticon)
                    .into(binding.imageProfile)
            }?:run {
                Glide.with(requireContext())
                    .load(R.drawable.ingredienticon)
                    .placeholder(R.drawable.ingredienticon)
                    .error(R.drawable.ingredienticon)
                    .into(binding.imageProfile)
            }
            if (!unitName.equals("null",true)){
                binding.unitName.text = unitName?:"Select"
            }
            ingredientStatus="No"
            binding.edIngredient.setText(title)
            binding.edIngredient.clearFocus()
        }

        if (type.equals("CookWare",true)){
            popupWindow.dismiss()
            loadCookWareImage = image?:""
            loadCookWareImage.let {
                Glide.with(requireContext())
                    .load(loadCookWareImage)
                    .placeholder(R.drawable.cookwareicon)
                    .error(R.drawable.cookwareicon)
                    .into(binding.imgCookWare)
            }?:run {
                Glide.with(requireContext())
                    .load(R.drawable.cookwareicon)
                    .into(binding.imgCookWare)
            }
            binding.edCookWare.setText(title)
            cookWareStatus="No"
            addCookWare()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun itemSelectType(pos: Int, type: String?) {
         if (type.equals("cookware",true)){
             val cookWare = cookwareList[pos]
             cookwareList.removeAt(pos)
             cookwareSelectPostion = pos
             adapterCookWareCreateRecipeAdapter.updateList(cookwareList)
             binding.edCookWare.setText(cookWare.name)
             cookWareStatus="No"
             binding.edCookWare.setBackgroundResource(R.drawable.create_select_header)
             cookWare.image?.let {
                 loadCookWareImage=it
                 Glide.with(requireContext())
                     .load(it)
                     .placeholder(R.drawable.cookwareicon)
                     .error(R.drawable.cookwareicon)
                     .into(binding.imgCookWare)
             }?:run {
                 Glide.with(requireContext())
                     .load(R.drawable.cookwareicon)
                     .into(binding.imgCookWare)
             }
         }
         if (type.equals("recipe",true)){
             val position = recipeList.indexOfFirst { it.itemId == pos }
             val recipe=recipeList[position]
             recipeSelectPostion = position
             Log.d("recipeselect", " $position")
             recipeList.removeAt(position)
             adapterRecipeAdapter.updateList(recipeList)
             if (recipe.header.equals("Recipe",true)){
                 binding.layHeaderRecipe.visibility = View.GONE
             }else{
                 binding.layHeaderRecipe.visibility = View.VISIBLE
                 binding.recipeHeader.setText(recipe.header)
             }
             binding.edRecipe.setText(recipe.text)
             binding.tvStep.text = "Step -"+(recipeList.size+1)
             binding.edRecipe.setBackgroundResource(R.drawable.create_select_header)

         }
         if (type.equals("ingredient",true)) {
             Log.d("recipeselect", " $pos")
             val position = ingredientList.indexOfFirst { it.itemId == pos }
             val ingredientData = ingredientList[position]
             ingredientSelectPostion = position
             ingredientList.removeAt(position)
             adapterIngredientsCreateRecipeAdapter.updateList(ingredientList)
             ingredientStatus="No"
             binding.edIngredient.setText(ingredientData.text)
             binding.edAmount.setText(ingredientData.quantity)
             binding.layamount.visibility = View.VISIBLE
             binding.unitName.text = ingredientData.unit
             ingredientData.image?.let {
                 loadImage=it
                 Glide.with(requireContext())
                     .load(it)
                     .placeholder(R.drawable.ingredienticon)
                     .error(R.drawable.ingredienticon)
                     .into(binding.imageProfile)
             }?:run {
                 Glide.with(requireContext())
                     .load(R.drawable.ingredienticon)
                     .into(binding.imageProfile)
             }

             if (ingredientData.header.equals("Ingredients",true)){
                 binding.layHrader.visibility = View.GONE
             }else{
                 binding.layHrader.visibility = View.VISIBLE
                 binding.ingredientHeader.setText(ingredientData.header)
             }

        }
    }

    private fun storeDataLocal(){
        if (changeStatus.equals("Yes",true)){
            if (screenName.equals("Create Recipe",true)){
                val recipeStore = Recipe(cookware = cookwareList,
                    description = binding.edtSummary.text.toString().trim(), image = recipeMainImageUri, ingredients = ingredientList,
                    instructions = recipeList, is_public = recipeStatus?.toInt(), label = binding.etRecipeName.text.toString().trim(),
                    meal_type = "Breakfast", origin = "India", prep_time = binding.tvPrepTime.text.toString().replace("min","").trim().toInt(),
                    ratings_avg = 5, servings = serving.toString(), source = "Grandma", source_type = "Book", source_url = source_url, createdType = apiCall.toString(),
                    totalTime = binding.tvPrepTime.text.toString().replace("min","").trim().toInt(),
                    total_time =(binding.tvcookTime.text.toString().replace("min","").trim().toInt() + binding.tvPrepTime.text.toString().replace("min","").trim().toInt()), uri = uri.toString(), url = "",
                    user_id = null, video_url = null, yield = serving.toString(), cookBook=binding.spinnerCookBook.text.toString(),
                    cookBookId=cookBookId, apiCall=apiCall, recipeName=recipeName, lastSelect=lastSelect, recipeStep = binding.edRecipe.text.toString().trim(),
                    recipeHeader = binding.recipeHeader.text.toString().trim(), cookwareData = binding.edCookWare.text.toString().trim(), ingredientHeader = binding.ingredientHeader.text.toString().trim(), ingredientValue = binding.edIngredient.text.toString().trim(),
                    unitValue = binding.unitName.text.toString().trim(), amountValue = binding.edAmount.text.toString().trim(),ingredientImg = loadImage, convertSelect = convertUnitStatus
                )

                val gson = Gson()
                // Recipe object ko JSON string me convert karna
                val recipeJson: String = gson.toJson(recipeStore)
                session.setCreateRecipe(recipeJson)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun selectHeader(){
        if (lastSelect.equals("ingredient",true)){
            selectType="1"
            ingredientStatus ="yes"
            binding.textIngredients.setBackgroundResource(R.drawable.select_bg)
            binding.textCookWare.setBackgroundResource(R.drawable.unselect_bg)
            binding.textRecipe.setBackgroundResource(R.drawable.unselect_bg)
            binding.textIngredients.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textCookWare.setTextColor(Color.parseColor("#3C4541"))
            binding.textRecipe.setTextColor(Color.parseColor("#3C4541"))
            binding.layIngredient.visibility = View.VISIBLE
            binding.layCookWare.visibility = View.GONE
            binding.layRecipe.visibility = View.GONE

            dataRecipe?.let { showView->
                showView.ingredientHeader?.let { data->
                    if (data.isNotEmpty()){
                        binding.ingredientHeader.setText(data)
                        binding.ingredientHeader.setBackgroundResource(R.drawable.create_select_header)
                        binding.layHrader.visibility = View.VISIBLE
                    }else{
                        binding.layHrader.visibility = View.GONE
                        binding.ingredientHeader.setBackgroundResource(0)
                    }
                }?:run {
                    binding.layHrader.visibility = View.GONE
                    binding.ingredientHeader.setBackgroundResource(0)
                }

                showView.ingredientValue?.let { data->
                    if (data.isNotEmpty()){
                        binding.edIngredient.setText(data)
                        binding.edIngredient.setBackgroundResource(R.drawable.create_select_header)
                        binding.layamount.visibility = View.GONE
                    }else{
                        binding.layamount.visibility = View.GONE
                        binding.edIngredient.setBackgroundResource(0)
                    }
                }?:run {
                    binding.layamount.visibility = View.GONE
                    binding.edIngredient.setBackgroundResource(0)
                }

                showView.unitValue?.let { data->
                    if (data.isNotEmpty() || !data.equals("Select",true)){
                        binding.unitName.text = data
                    }else{
                        binding.unitName.text = "Select"
                    }
                }?:run {
                    binding.unitName.text = "Select"
                }
                showView.amountValue?.let { data->
                    if (data.isNotEmpty() ){
                        binding.edAmount.setText(data)
                        binding.layamount.visibility = View.VISIBLE
                    }else{
                        if (binding.edIngredient.text.toString().trim().isNotEmpty()){
                            binding.layamount.visibility = View.VISIBLE
                        }else{
                            binding.layamount.visibility = View.GONE
                        }
                    }
                }?:run {
                    if (binding.edIngredient.text.toString().trim().isNotEmpty()){
                        binding.layamount.visibility = View.VISIBLE
                    }else{
                        binding.layamount.visibility = View.GONE
                    }
                }

                showView.ingredientImg?.let { data->
                    loadImage=data
                    Glide.with(requireContext())
                        .load(loadImage)
                        .placeholder(R.drawable.ingredienticon)
                        .error(R.drawable.ingredienticon)
                        .into(binding.imageProfile)
                }?:run {
                    loadImage=""
                }

            }

        }
        if (lastSelect.equals("cookware",true)){
            cookWareStatus ="yes"
            selectType="2"
            binding.textIngredients.setBackgroundResource(R.drawable.unselect_bg)
            binding.textCookWare.setBackgroundResource(R.drawable.select_bg)
            binding.textRecipe.setBackgroundResource(R.drawable.unselect_bg)
            binding.textIngredients.setTextColor(Color.parseColor("#3C4541"))
            binding.textCookWare.setTextColor(Color.parseColor("#FFFFFF"))
            binding.textRecipe.setTextColor(Color.parseColor("#3C4541"))
            binding.layIngredient.visibility = View.GONE
            binding.layRecipe.visibility = View.GONE
            binding.layCookWare.visibility = View.VISIBLE
            dataRecipe?.let { showView->
                showView.cookwareData?.let { data->
                    if (data.isNotEmpty()){
                        binding.edCookWare.setText(data)
                        binding.edCookWare.setBackgroundResource(R.drawable.create_select_header)
                    }else{
                        binding.edCookWare.setBackgroundResource(0)
                    }
                }?:run {
                    binding.edCookWare.setBackgroundResource(0)
                }
            }
        }
        if (lastSelect.equals("recipe",true)){
            binding.textIngredients.setBackgroundResource(R.drawable.unselect_bg)
            binding.textCookWare.setBackgroundResource(R.drawable.unselect_bg)
            binding.textRecipe.setBackgroundResource(R.drawable.select_bg)
            binding.textIngredients.setTextColor(Color.parseColor("#3C4541"))
            binding.textCookWare.setTextColor(Color.parseColor("#3C4541"))
            binding.textRecipe.setTextColor(Color.parseColor("#FFFFFF"))
            binding.layIngredient.visibility = View.GONE
            binding.layCookWare.visibility = View.GONE
            binding.layRecipe.visibility = View.VISIBLE
            if (!apiCall.equals("import",true)){
                binding.rcyRecipe.visibility = View.VISIBLE
                binding.btnHeaderRecipe.visibility = View.VISIBLE
                binding.tvStep.visibility = View.VISIBLE
                binding.edRecipe.isEnabled = true
                binding.tvStep.text = "Step -"+(recipeList.size+1)
                dataRecipe?.let { showView->
                    showView.recipeStep?.let { data->
                        if (data.isNotEmpty()){
                            binding.edRecipe.setText(data)
                            binding.edRecipe.setBackgroundResource(R.drawable.create_select_header)
                        }else{
                            binding.edRecipe.setBackgroundResource(0)
                        }
                    }?:run {
                        binding.edRecipe.setBackgroundResource(0)
                    }
                    showView.recipeHeader?.let { data->
                        if (data.isNotEmpty()){
                            binding.recipeHeader.setText(data)
                            binding.recipeHeader.setBackgroundResource(R.drawable.create_select_header)
                            binding.layHeaderRecipe.visibility = View.VISIBLE
                        }else{
                            binding.layHeaderRecipe.visibility = View.GONE
                            binding.recipeHeader.setBackgroundResource(0)
                        }
                    }?:run {
                        binding.recipeHeader.setBackgroundResource(0)
                    }
                }
            }else{
                binding.rcyRecipe.visibility = View.GONE
                binding.btnHeaderRecipe.visibility = View.GONE
                binding.layHeaderRecipe.visibility = View.GONE
                binding.tvStep.visibility = View.GONE
                binding.edRecipe.isEnabled = false
                binding.edRecipe.setText("Imported recipe - Recipe not required")
            }
        }
    }

}
