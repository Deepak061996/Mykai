package com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemClickListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterFilterCookTimeItem
import com.mykaimeal.planner.adapter.AdapterFilterCuisinesItem
import com.mykaimeal.planner.adapter.AdapterFilterDietItem
import com.mykaimeal.planner.adapter.AdapterFilterMealItem
import com.mykaimeal.planner.adapter.AdapterFilterNutritionItem
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentFilterSearchBinding
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.CookTime
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.Diet
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.DishType
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.FilterSearchModel
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.FilterSearchModelData
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.MealType
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.model.Protein
import com.mykaimeal.planner.fragment.mainfragment.searchtab.filtersearch.viewmodel.FilterSearchViewModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.Locale

@AndroidEntryPoint
class FilterSearchFragment : Fragment(), OnItemClickListener {

    private var _binding: FragmentFilterSearchBinding? = null
    private val binding get() = _binding!!
    private var adapterFilterMealItem: AdapterFilterMealItem? = null
    private var adapterFilterDietItem: AdapterFilterDietItem? = null
    private var adapterFilterCookBookItem: AdapterFilterCookTimeItem? = null
    private var adapterFilterCuisinesItem: AdapterFilterCuisinesItem? = null
    private var adapterFilterNutritionItem: AdapterFilterNutritionItem? = null
    private lateinit var filterSearchViewModel: FilterSearchViewModel
    private var fullListMealType: MutableList<MealType> = mutableListOf()
    private var originalFullList: MutableList<Diet> = mutableListOf()
    private var fullListCookTime: MutableList<CookTime> = mutableListOf()
    private var fullListDishTypes: MutableList<DishType> = mutableListOf()
    private var fullListProtein: MutableList<Protein> = mutableListOf()
    private var showMealType: MutableList<MealType> = mutableListOf()
    private var showFullList: MutableList<Diet> = mutableListOf()
    private var showListCookTime: MutableList<CookTime> = mutableListOf()
    private var showListDishTypes: MutableList<DishType> = mutableListOf()
    private var showListProtein: MutableList<Protein> = mutableListOf()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFilterSearchBinding.inflate(inflater, container, false)

        (activity as? MainActivity)?.binding?.apply {
            llIndicator.visibility = View.GONE
            llBottomNavigation.visibility = View.GONE
        }

        filterSearchViewModel = ViewModelProvider(this)[FilterSearchViewModel::class.java]

        binding.relApplyBtn.isClickable = false

        backButton()

        initialize()


        if (filterSearchViewModel.fullListMealType != null && filterSearchViewModel.originalFullList != null && filterSearchViewModel.fullListCookTime != null &&
            filterSearchViewModel.fullListCuisines != null && filterSearchViewModel.fullListProtein != null
        ) {
            fullListMealType = filterSearchViewModel.fullListMealType!!
            originalFullList = filterSearchViewModel.originalFullList!!
            fullListCookTime = filterSearchViewModel.fullListCookTime!!
            fullListDishTypes = filterSearchViewModel.fullListCuisines!!
            fullListProtein = filterSearchViewModel.fullListProtein!!
            showFullList.clear()
            showMealType.clear()
            showListCookTime.clear()
            showListDishTypes.clear()
            showListProtein.clear()
            upDateUi()
        } else {
            // This Api call when the screen in loaded
            if (BaseApplication.isOnline(requireActivity())) {
                launchApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }

        return binding.root
    }

    private fun backButton() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })
    }

    private fun launchApi() {
        if (BaseApplication.isOnline(requireActivity())) {
            BaseApplication.showMe(requireContext())
            lifecycleScope.launch {
                filterSearchViewModel.getFilterList {
                    BaseApplication.dismissMe()
                    when (it) {
                        is NetworkResult.Success -> handleSuccessResponse(it.data.toString())
                        is NetworkResult.Error -> showAlert(it.message, false)
                        else -> showAlert(it.message, false)
                    }
                }
            }
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, FilterSearchModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                if (apiModel.data != null) {
                    showDataInUi(apiModel.data)
                }
            } else {
                handleError(apiModel.code, apiModel.message)
            }
        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun handleError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun showDataInUi(data: FilterSearchModelData) {
        try {

            fullListMealType.clear()
            originalFullList.clear()
            fullListCookTime.clear()
            fullListDishTypes.clear()
            fullListProtein.clear()

            showFullList.clear()
            showMealType.clear()
            showListCookTime.clear()
            showListDishTypes.clear()
            showListProtein.clear()

            data.mealType?.let {
                fullListMealType.addAll(it)
            }
            data.Diet?.let {
                originalFullList.addAll(it)
            }
            data.cook_time?.let {
                fullListCookTime.addAll(it)
            }
            data.dishType?.let {
                fullListDishTypes.addAll(it)
            }
            data.protein?.let {
                fullListProtein.addAll(it)
            }


            filterSearchViewModel.setOriginalFullList(originalFullList)
            filterSearchViewModel.setFullListCookTime(fullListCookTime)
            filterSearchViewModel.setFullListMealType(fullListMealType)
            filterSearchViewModel.setFullListCuisines(fullListDishTypes)
            filterSearchViewModel.setFullListProtein(fullListProtein)

            upDateUi()

        } catch (e: Exception) {
            showAlert(e.message, false)
        }
    }

    private fun upDateUi() {

        if (fullListMealType.isNotEmpty()) {
            showMealType.addAll(fullListMealType.take(5))

            if (fullListMealType.size > 5) {
                showMealType.add(
                    MealType(id = -1, image = "", name = "More", "", selected = true)
                )
            }

            binding.rcyMealType.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                adapterFilterMealItem = AdapterFilterMealItem(
                    showMealType,
                    requireActivity(),
                    this@FilterSearchFragment
                )
                adapter = adapterFilterMealItem
            }
        }

        if (originalFullList.isNotEmpty()) {
            showFullList.addAll(originalFullList.take(5))

            if (originalFullList.size > 5) {
                showFullList.add(Diet(name = "More", selected = true, ""))
            }

            binding.rcyDiet.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                adapterFilterDietItem = AdapterFilterDietItem(
                    showFullList,
                    requireActivity(),
                    this@FilterSearchFragment
                )
                adapter = adapterFilterDietItem
            }
        }

        if (fullListCookTime.isNotEmpty()) {
            showListCookTime.addAll(fullListCookTime.take(5))
            if (fullListCookTime.size > 5) {
                showListCookTime.add(CookTime(name = "More", value = "", selected = true))
            }
            binding.rcyCookTime.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                adapterFilterCookBookItem = AdapterFilterCookTimeItem(
                    showListCookTime, requireActivity(), this@FilterSearchFragment
                )
                adapter = adapterFilterCookBookItem
            }
        }


        if (fullListDishTypes.isNotEmpty()) {
            showListDishTypes.addAll(fullListDishTypes.take(5))

            if (fullListDishTypes.size > 5) {
                showListDishTypes.add(DishType(created_at = "", deleted_at = null, id = -1, name = "More", updated_at = "", selected = true))
            }
            binding.rcyCuisine.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                // Optional: update your model if needed
                adapterFilterCuisinesItem = AdapterFilterCuisinesItem(showListDishTypes,
                    requireActivity(), this@FilterSearchFragment)
                adapter = adapterFilterCuisinesItem
            }
        }

        if (fullListProtein.isNotEmpty()) {
            showListProtein.addAll(fullListProtein.take(5))

            if (fullListProtein.size > 5) {
                showListProtein.add(Protein(name = "More", value = "", selected = true))
            }
            binding.rcyNutrition.apply {
                layoutManager = FlexboxLayoutManager(requireContext()).apply {
                    flexDirection = FlexDirection.ROW
                    flexWrap = FlexWrap.WRAP
                    justifyContent = JustifyContent.FLEX_START
                }
                // Optional: update your model if needed

                adapterFilterNutritionItem = AdapterFilterNutritionItem(showListProtein, requireActivity(), this@FilterSearchFragment)
                adapter = adapterFilterNutritionItem
            }
        }
        buttonActive()
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }

    private fun initialize() {

        binding.relBackFiltered.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.etIngRecipeSearchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }

            override fun afterTextChanged(editable: Editable) {
                if (editable.toString().isEmpty()) {

                    if (showMealType.size > 0) {
                        adapterFilterMealItem?.updateList(showMealType)
                        binding.llMealType.visibility = View.VISIBLE
                        binding.rcyMealType.visibility = View.VISIBLE
                    } else {
                        binding.llMealType.visibility = View.GONE
                        binding.rcyMealType.visibility = View.GONE
                    }

                    if (showFullList.size > 0) {
                        adapterFilterDietItem?.updateList(showFullList)
                        binding.llDiet.visibility = View.VISIBLE
                        binding.rcyDiet.visibility = View.VISIBLE
                    } else {
                        binding.llDiet.visibility = View.GONE
                        binding.rcyDiet.visibility = View.GONE
                    }

                    if (showListCookTime.size > 0) {
                        adapterFilterCookBookItem?.updateList(showListCookTime)
                        binding.llCookTime.visibility = View.VISIBLE
                        binding.rcyCookTime.visibility = View.VISIBLE
                    } else {
                        binding.llCookTime.visibility = View.GONE
                        binding.rcyCookTime.visibility = View.GONE
                    }


                    if (showListDishTypes.size > 0) {
                        adapterFilterCuisinesItem?.updateList(showListDishTypes)
                        binding.llCuisine.visibility = View.VISIBLE
                        binding.rcyCuisine.visibility = View.VISIBLE
                    } else {
                        binding.llCuisine.visibility = View.GONE
                        binding.rcyCuisine.visibility = View.GONE
                    }

                    if (showListProtein.size > 0) {
                        adapterFilterNutritionItem?.updateList(showListProtein)
                        binding.llNutrition.visibility = View.VISIBLE
                        binding.rcyNutrition.visibility = View.VISIBLE
                    } else {
                        binding.llNutrition.visibility = View.GONE
                        binding.rcyNutrition.visibility = View.GONE
                    }
                } else {
                    filter(editable.toString())
                }
            }
        })

        binding.relApplyBtn.setOnClickListener {
            if (binding.relApplyBtn.isClickable) {
                    (activity as MainActivity?)?.upDateSearchItemClick()
                    val mealType = showMealType.filter { it.selected == true }.map { it.name.toString() }
                    val diet = showFullList.filter { it.selected == true }.map { it.name.toString() }
                    val cookTime = showListCookTime.filter { it.selected == true }.map { it.value.toString() }
                    val cuisine = showListDishTypes.filter { it.selected == true }.map { it.name.toString() }
                    val localList:MutableList<Protein> = mutableListOf()
                    localList.addAll(showListProtein)
                    localList.removeIf { it.name.equals("More",true) }
                    val nutrition = localList.filter { it.selected == true }.map { it.value.toString() }
                    val bundle = Bundle().apply {
                        putString("recipeName", "")
                        putString("mealJsonArray", JSONArray(mealType).toString())
                        putString("dietJsonArray", JSONArray(diet).toString())
                        putString("cookTimeJsonArray", JSONArray(cookTime).toString())
                        putString("cuisineJsonArray", JSONArray(cuisine).toString())
                        putString("nutritionJsonArray", JSONArray(nutrition).toString())
                        putString("screenType", "filter")
                    }
                    findNavController().navigate(R.id.searchedRecipeBreakfastFragment, bundle)
            }
        }

    }


    private fun filter(text: String) {
        val list1: MutableList<MealType> = mutableListOf()
        val list2: MutableList<Diet> = mutableListOf()
        val list3: MutableList<CookTime> = mutableListOf()
        val list4: MutableList<DishType> = mutableListOf()
        val list5: MutableList<Protein> = mutableListOf()
        try {

            if (showMealType.size > 0) {
                for (item in showMealType) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list1.add(item)
                    }
                }
                if (list1.size > 0) {
                    adapterFilterMealItem?.updateList(list1)
                    binding.rcyMealType.visibility = View.VISIBLE
                } else {
                    binding.rcyMealType.visibility = View.GONE
                }
            } else {
                binding.rcyMealType.visibility = View.GONE
            }

            if (showFullList.size > 0) {
                for (item in showFullList) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list2.add(item)
                    }
                }
                if (list2.size > 0) {
                    adapterFilterDietItem?.updateList(list2)
                    binding.rcyDiet.visibility = View.VISIBLE
                } else {
                    binding.rcyDiet.visibility = View.GONE
                }
            } else {
                binding.rcyDiet.visibility = View.GONE
            }

            if (showListCookTime.size > 0) {
                for (item in showListCookTime) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list3.add(item)
                    }
                }
                if (list3.size > 0) {
                    adapterFilterCookBookItem?.updateList(list3)
                    binding.rcyCookTime.visibility = View.VISIBLE
                } else {
                    binding.rcyCookTime.visibility = View.GONE
                }
            } else {
                binding.rcyCookTime.visibility = View.GONE
            }

            if (showListDishTypes.size > 0) {
                for (item in showListDishTypes) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list4.add(item)
                    }
                }
                if (list4.size > 0) {
                    adapterFilterCuisinesItem?.updateList(list4)
                    binding.rcyCuisine.visibility = View.VISIBLE
                } else {
                    binding.rcyCuisine.visibility = View.GONE
                }
            } else {
                binding.rcyCuisine.visibility = View.GONE
            }

            if (showListProtein.size > 0) {
                for (item in showListProtein) {
                    val category = item.name?.lowercase(Locale.getDefault())
                    if (category != null && category.contains(text.lowercase(Locale.getDefault()))) {
                        list5.add(item)
                    }
                }
                if (list5.size > 0) {
                    adapterFilterNutritionItem?.updateList(list5)
                    binding.rcyNutrition.visibility = View.VISIBLE
                } else {
                    binding.rcyNutrition.visibility = View.GONE
                }
            } else {
                binding.rcyNutrition.visibility = View.GONE
            }

        } catch (e: Exception) {
            binding.rcyDiet.visibility = View.GONE
            binding.rcyCookTime.visibility = View.GONE
            binding.rcyMealType.visibility = View.GONE
            binding.rcyCuisine.visibility = View.GONE
            binding.rcyNutrition.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    override fun itemClick(position: Int?, status: String?, type: String?) {
        if (type.equals("MealType", true)) {
            showMealType.clear()
            for (i in 0 until fullListMealType.size) {
                showMealType.add(fullListMealType[i])
            }
            adapterFilterMealItem?.updateList(showMealType)
        }
        if (type.equals("Diet", true)) {
            showFullList.clear()
            for (i in 0 until originalFullList.size) {
                showFullList.add(originalFullList[i])
            }
            adapterFilterDietItem?.updateList(showFullList)
        }
        if (type.equals("CookTime", true)) {
            showListCookTime.clear()
            for (i in 0 until fullListCookTime.size) {
                showListCookTime.add(fullListCookTime[i])
            }
            adapterFilterCookBookItem?.updateList(showListCookTime)
        }

        if (type.equals("Cuisines", true)) {
            showListDishTypes.clear()
            for (i in 0 until fullListDishTypes.size ) {
                showListDishTypes.add(fullListDishTypes[i])
            }
            adapterFilterCuisinesItem?.updateList(showListDishTypes)
        }

        if (type.equals("Nutrition", true)) {
            showListProtein.clear()
            for (i in 0 until fullListProtein.size) {
                showListProtein.add(fullListProtein[i])
            }
            adapterFilterNutritionItem?.updateList(showListProtein)

        }

        buttonActive()
    }

    private fun buttonActive() {
        val count = /*showMealType.count { it.selected == true } + showFullList.count { it.selected == true } +
                    showListCookTime.count { it.selected == true } + showListDishTypes.count { it.selected == true }
        + showListProtein.count { it.selected == true }*/

            fullListMealType.count { it.selected == true } + originalFullList.count { it.selected == true } +
                    fullListCookTime.count { it.selected == true } + fullListDishTypes.count { it.selected == true } + fullListProtein.count { it.selected ==true }

        Log.d("count", "******$count")

        if (count == 0) {
            binding.tvCount.text = "Apply"
            binding.relApplyBtn.isClickable = false
            binding.relApplyBtn.setBackgroundResource(R.drawable.gray_btn_unselect_background)
        } else {
            binding.tvCount.text = "Apply ($count)"
            binding.relApplyBtn.isClickable = true
            binding.relApplyBtn.setBackgroundResource(R.drawable.green_btn_background)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onDestroy() {
        super.onDestroy()
        filterSearchViewModel.setOriginalFullList(null)
        filterSearchViewModel.setFullListCookTime(null)
        filterSearchViewModel.setFullListMealType(null)
    }


}