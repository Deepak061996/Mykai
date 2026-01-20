package com.mykaimeal.planner.fragment.mainfragment.commonscreen.supermarktesnearbyscreen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.mykaimeal.planner.OnItemSelectUnSelectListener
import com.mykaimeal.planner.R
import com.mykaimeal.planner.activity.MainActivity
import com.mykaimeal.planner.adapter.AdapterSuperMarket
import com.mykaimeal.planner.basedata.BaseApplication
import com.mykaimeal.planner.basedata.BaseApplication.isOnline
import com.mykaimeal.planner.basedata.NetworkResult
import com.mykaimeal.planner.databinding.FragmentSuperMarketsNearByBinding
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketdetailssupermarket.viewmodel.BasketDetailsSuperMarketViewModel
import com.mykaimeal.planner.fragment.mainfragment.commonscreen.basketscreen.model.Store
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.homeviewmodel.apiresponse.SuperMarketModel
import com.mykaimeal.planner.fragment.mainfragment.viewmodel.walletviewmodel.apiresponse.SuccessResponseModel
import com.mykaimeal.planner.messageclass.ErrorMessage
import dagger.hilt.android.AndroidEntryPoint
import dev.eren.removebg.RemoveBg
import kotlinx.coroutines.launch


@AndroidEntryPoint
class SuperMarketsNearByFragment : Fragment(), OnItemSelectUnSelectListener, OnMapReadyCallback {

    private lateinit var binding: FragmentSuperMarketsNearByBinding
    private var adapter: AdapterSuperMarket? = null
    private lateinit var basketDetailsSuperMarketViewModel: BasketDetailsSuperMarketViewModel
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var latitude = "0.0"
    private var longitude = "0.0"
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    private var storeUid: String? = ""
    private var storeName: String? = ""
    private var currentPage:Int=1
    var isUserScrolling = false
    var isLoading = false
    private var hasMoreData = true
    private var stores: MutableList<Store> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSuperMarketsNearByBinding.inflate(layoutInflater, container, false)

        basketDetailsSuperMarketViewModel =
            ViewModelProvider(requireActivity())[BasketDetailsSuperMarketViewModel::class.java]

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigateUp()
                }
            })

        adapter = AdapterSuperMarket(stores, requireActivity(), this)


        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        initialize()

        return binding.root
    }

    private fun initialize() {

        binding.imageBackIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.textDelivery.setOnClickListener {
            updateButtonStyles(binding.textDelivery, binding.textCollect)
        }

        binding.textCollect.setOnClickListener {
            updateButtonStyles(binding.textCollect, binding.textDelivery)
        }



        // Scroll listener for pagination
        binding.recySuperMarket.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    isUserScrolling = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (!isUserScrolling || isLoading || !hasMoreData) return
                if (!recyclerView.canScrollVertically(1)) {
                    isUserScrolling = false
                    isLoading = true
                    currentPage++
                    loadSuperMarket()
                }
            }
        })
        loadSuperMarket()

    }

    private fun loadSuperMarket(){
        if (isOnline(requireActivity())) {
            getSuperMarketsList(currentPage)
        } else {
            BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
        }
    }


    private fun updateButtonStyles(selected: View, unselected: View) {
        selected.setBackgroundResource(R.drawable.selected_button_bg)
        unselected.setBackgroundResource(R.drawable.unselected_button_bg)
        (selected as TextView).setTextColor(Color.WHITE)
        (unselected as TextView).setTextColor(Color.BLACK)
    }

    private fun getSuperMarketsList(currentPage : Int) {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketDetailsSuperMarketViewModel.getSuperMarketWithPage({
                BaseApplication.dismissMe()
                handleMarketApiResponse(it)
            }, latitude, longitude,currentPage.toString())
        }
    }

    private fun handleMarketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleMarketSuccessResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }


    @SuppressLint("SetTextI18n")
    private fun handleMarketSuccessResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuperMarketModel::class.java)
            Log.d("@@@ Recipe Detailsssss", "message :- $data")
            if (apiModel.code == 200 && apiModel.success == true) {
                apiModel.data?.let {
                    showUIData(apiModel.data)
                }?:run {
                    pageReset()
                }

            } else {
                pageReset()
                handleApiError(apiModel.code,apiModel.message)
            }
        } catch (e: Exception) {
            pageReset()
            showAlert(e.message, false)
        }

    }

    private fun showUIData(data: MutableList<Store>?) {
        try {
            data?.let {
                stores.addAll(it)
            }
            stores.removeIf  {
                it.total == 0.0
            }
            if (stores.size>0){
                stores.sortByDescending { it.is_open?:false }
                // Set adapter
                adapter?.updateList(stores)
                binding.recySuperMarket.adapter = adapter
                mMap?.let { updateMap(it) }
            }
            hasMoreData=true
        } catch (e: Exception) {
            showAlert(e.message, false)
        }finally {
            isLoading = false
        }
    }

    private fun showAlert(message: String?, status: Boolean) {
        BaseApplication.alertError(requireContext(), message, status)
    }


    private fun pageReset(){
        if (currentPage!=1){
            currentPage--
        }
        isLoading = false
        hasMoreData = true
        isUserScrolling = true

    }

    private fun handleApiError(code: Int?, message: String?) {
        if (code == ErrorMessage.code) {
            showAlert(message, true)
        } else {
            showAlert(message, false)
        }
    }

    private fun metersToLongitudeOffset(meters: Double, latitude: Double): Double {
        val earthRadius = 6378137.0 // Earth's radius in meters
        return meters / (earthRadius * Math.cos(Math.toRadians(latitude)) * (Math.PI / 180))
    }

    /*private fun updateMap(map: GoogleMap) {
        map.clear()

        map.uiSettings.apply {
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
        }

        var firstMarkerPosition: LatLng? = null

        map.setOnMapLoadedCallback {
            stores.forEach { store ->
                val lat = store.address?.latitude
                val lng = store.address?.longitude
                val imageUrl = store.image

                if (lat != null && lng != null) {
                    val centerLocation = LatLng(lat, lng)
                    if (firstMarkerPosition == null) {
                        firstMarkerPosition = centerLocation
                    }

                    // Step 1: Create placeholder marker immediately
                    val placeholderBitmap = BitmapFactory.decodeResource(resources, R.drawable.gps)
                    val placeholderView = createCombinedMarkerView(requireContext(), null)
                    val placeholderMarkerBitmap = createBitmapFromView(placeholderView)

                    val markerOptions = MarkerOptions()
                        .position(centerLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(placeholderMarkerBitmap))
                        .anchor(0.5f, 1f)

                    val marker = map.addMarker(markerOptions)
                    marker?.tag = store

                    // Step 2: Start loading actual image and updating the marker icon
                    Glide.with(requireContext())
                        .asBitmap()
                        .load(imageUrl)
                        .override(150, 130)
                        .placeholder(R.drawable.no_image)
                        .error(R.drawable.no_image)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                lifecycleScope.launch {
                                    val remover = RemoveBg(requireContext())
                                    remover.clearBackground(resource).collect { outputBitmap ->
                                        if (outputBitmap != null) {
                                            val markerView = createCombinedMarkerView(requireContext(), outputBitmap)
                                            val markerBitmap = createBitmapFromView(markerView)

                                            // Update the existing marker’s icon
                                            marker?.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                                        }
                                    }
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}
                        })
                }
            }

            firstMarkerPosition?.let {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
            }
        }

    }*/

    private fun updateMap(map: GoogleMap) {
        map.clear()

        map.uiSettings.apply {
            isScrollGesturesEnabled = true
            isZoomGesturesEnabled = true
            isTiltGesturesEnabled = true
            isRotateGesturesEnabled = true
        }

        var firstMarkerPosition: LatLng? = null

        map.setOnMapLoadedCallback {
            stores.forEach { store ->
                val lat = store.address?.latitude
                val lng = store.address?.longitude

                if (lat != null && lng != null) {
                    val centerLocation = LatLng(lat, lng)
                    if (firstMarkerPosition == null) {
                        firstMarkerPosition = centerLocation
                    }

                    // Create a marker view with text (e.g., store name)
                    val markerView = createCombinedMarkerView(requireContext(), store.store_name ?: "No Name")
                    val markerBitmap = createBitmapFromView(markerView)

                    val markerOptions = MarkerOptions()
                        .position(centerLocation)
                        .icon(BitmapDescriptorFactory.fromBitmap(markerBitmap))
                        .anchor(0.5f, 1f)

                    val marker = map.addMarker(markerOptions)
                    marker?.tag = store
                }
            }

            firstMarkerPosition?.let {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
            }
        }
    }



    fun createBitmapFromView(view: View): Bitmap {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


//    private fun createCombinedMarkerView(context: Context, text: String): View {
//        val markerWidth = 70
//        val markerHeight = 70
//        val padding = 8
//
//        val container = LinearLayout(context).apply {
//            layoutParams = ViewGroup.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//            orientation = LinearLayout.HORIZONTAL
//            setPadding(padding, padding, padding, padding)
//            setBackgroundColor(Color.TRANSPARENT)
//            gravity = Gravity.CENTER_VERTICAL
//        }
//
//        val textView = TextView(context).apply {
//            layoutParams = LinearLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT
//            ).apply {
//                setMargins(0, 0, 8, 0)
//            }
//            this.text = text
//            textSize = 14f
//            setTextColor(Color.BLACK)
//            setPadding(16, 8, 16, 8)
//        }
//
//        val iconView = ImageView(context).apply {
//            layoutParams = LinearLayout.LayoutParams(markerWidth, markerHeight)
//            scaleType = ImageView.ScaleType.FIT_CENTER
//            setImageResource(R.drawable.gps)
//        }
//
//        container.addView(textView)
//        container.addView(iconView)
//
//        return container
//    }


    private fun createCombinedMarkerView(context: Context, text: String): View {
        val markerWidth = 70
        val markerHeight = 70
        val padding = 8

        // Generate a random background color
        val random = java.util.Random()
        val randomColor = Color.rgb(
            random.nextInt(256),
            random.nextInt(256),
            random.nextInt(256)
        )

        // Container for the marker
        val container = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.TRANSPARENT)
            gravity = Gravity.CENTER_VERTICAL
        }

        // TextView with random background color
        val textView = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 8, 0)
            }
            this.text = text
            textSize = 20f
            setTextColor(randomColor)
            setPadding(16, 8, 16, 8)

            // Set custom font
            typeface = ResourcesCompat.getFont(context, R.font.montserrat_bold)
        }

        // Icon (e.g., GPS marker)
        val iconView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(markerWidth, markerHeight)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageResource(R.drawable.gps) // Replace with your actual drawable
        }

        // Add views to container
        container.addView(textView)
        container.addView(iconView)

        return container
    }


    /*private fun createCombinedMarkerView(context: Context, value: String): View {
        val textView = TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            textSize = 14f
            setPadding(16, 8, 16, 8)
            setTextColor(Color.BLACK)
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            text = value
        }
        return textView
    }*/




//    fun createCombinedMarkerView(requireContext: android.content.Context, logoBitmap: Bitmap?): View {
//        val logoWidth = 150
//        val logoHeight = 130
//        val markerWidth = 70
//        val markerHeight = 70
//        val spacing = 6
//
//        val container = LinearLayout(requireContext).apply {
//            layoutParams = ViewGroup.LayoutParams(logoWidth + markerWidth + spacing, maxOf(logoHeight, markerHeight))
//            orientation = LinearLayout.HORIZONTAL
//            setBackgroundColor(Color.TRANSPARENT)
//            gravity = Gravity.CENTER_VERTICAL
//        }
//
//        val logoImageView = ImageView(context).apply {
//            layoutParams = LinearLayout.LayoutParams(logoWidth, logoHeight)
//            scaleType = ImageView.ScaleType.FIT_XY
//            setImageBitmap(logoBitmap)
//        }
//
//        val markerImageView = ImageView(context).apply {
//            layoutParams = LinearLayout.LayoutParams(markerWidth, markerHeight)
//            scaleType = ImageView.ScaleType.FIT_CENTER
//            setImageResource(R.drawable.gps) // Replace with your GPS marker drawable
//        }
//
//        container.addView(logoImageView)
//        container.addView(markerImageView)
//
//        return container
//    }



//    private fun updateMap(map: GoogleMap) {
//        map.clear()
//
//        mMap?.uiSettings?.apply {
//            isScrollGesturesEnabled = true
//            isZoomGesturesEnabled = true
//            isTiltGesturesEnabled = true
//            isRotateGesturesEnabled = true
//        }
//
//        var firstMarkerPosition: LatLng? = null
//        val horizontalOffset = 0.0001 // ~11 meters side-by-side
//
//        stores.forEach { store ->
//            val lat = store.address?.latitude
//            val lng = store.address?.longitude
//            val name = store.store_name
//            val imageUrl = store.image
//
//            if (lat != null && lng != null) {
//                val centerLocation = LatLng(lat, lng)
//
//                // Set first camera position
//                if (firstMarkerPosition == null) {
//                    firstMarkerPosition = centerLocation
//                }
//
//                val leftLocation = LatLng(lat, lng - horizontalOffset)
//                val rightLocation = LatLng(lat, lng + horizontalOffset)
//
//                // 1. Load marker from imageUrl (left)
//                Glide.with(requireContext())
//                    .asBitmap()
//                    .load(imageUrl)
//                    .override(200, 100)
//                    .into(object : CustomTarget<Bitmap>() {
//                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//                            lifecycleScope.launch {
//                                val remover = RemoveBg(requireContext())
//                                remover.clearBackground(resource).collect { output ->
//                                    val markerOptions = MarkerOptions()
//                                        .position(leftLocation)
//                                        .icon(output?.let { BitmapDescriptorFactory.fromBitmap(it) })
//                                    val marker = mMap?.addMarker(markerOptions)
//                                    marker?.tag = store
//                                }
//                            }
//                        }
//
//                        override fun onLoadCleared(placeholder: Drawable?) {}
//                    })
//
//                // 2. Add static marker (right)
//                val rawBitmap = BitmapFactory.decodeResource(resources, R.drawable.gps)
//                val resizedBitmap = Bitmap.createScaledBitmap(rawBitmap, 70, 70, false) // width: 70, height: 70
//                val staticMarkerOptions = MarkerOptions()
//                    .position(rightLocation)
//                    .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap))
//                mMap?.addMarker(staticMarkerOptions)
//            }
//        }
//
//        // Move camera to first marker
//        firstMarkerPosition?.let {
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
//        }
//    }



//    private fun updateMap(map: GoogleMap) {
//        map.clear()
//
//        mMap?.uiSettings?.apply {
//            isScrollGesturesEnabled = true
//            isZoomGesturesEnabled = true
//            isTiltGesturesEnabled = true
//            isRotateGesturesEnabled = true
//        }
//
//        var firstMarkerPosition: LatLng? = null
//
//        stores.forEach { store ->
//            val lat = store.address?.latitude
//            val lng = store.address?.longitude
//            val name = store.store_name
//            val imageUrl = store.image
//            if (lat != null && lng != null) {
//                val location = LatLng(lat, lng)
//
//                if (firstMarkerPosition == null) {
//                    firstMarkerPosition = location
//                }
//                Glide.with(requireContext())
//                    .asBitmap()
//                    .load(imageUrl) // Or use imageUrl if dynamic
//                    .override(200, 100)
//                    .into(object : CustomTarget<Bitmap>() {
//                        override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {
//                            lifecycleScope.launch {
//                                val remover = RemoveBg(requireContext())
//                                remover.clearBackground(resource).collect { output ->
//                                    val markerOptions = MarkerOptions()
//                                        .position(location)
//                                        .title(name)
//                                        .icon(output?.let { BitmapDescriptorFactory.fromBitmap(it) })
//                                    val marker = mMap?.addMarker(markerOptions)
//                                    marker?.tag = store // Tag the marker with its Store object
//                                }
//                            }
//                        }
//                        override fun onLoadCleared(placeholder: Drawable?) {}
//                    })
//
//            }
//        }
//
//        // Move camera to first marker only
//        firstMarkerPosition?.let {
//            map.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
//        }
//
//    }






    override fun onMapReady(gmap: GoogleMap) {
        mMap = gmap
        // 🔹 Disable map movement
        val lat = latitude?.toDoubleOrNull() ?: 0.0  // Convert String to Double, default to 0.0 if null
        val lng = longitude?.toDoubleOrNull() ?: 0.0
        val newYork = LatLng(lat, lng)

        // 🔴 Disable indoor maps to hide level picker
        mMap?.isIndoorEnabled = false

        val customMarker = bitmapDescriptorFromVector(
            R.drawable.gps,
            70,
            70
        )

        mMap?.addMarker(
            MarkerOptions()
                .position(newYork)
                .icon(customMarker)
        )

        // 🔹 Disable map movement
        mMap?.uiSettings?.apply {
            isScrollGesturesEnabled = false  // ❌ Disable scrolling
            isZoomGesturesEnabled = false    // ❌ Disable zooming
            isTiltGesturesEnabled = false    // ❌ Disable tilt
            isRotateGesturesEnabled = false  // ❌ Disable rotation
        }

        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(newYork, 20f))

    }

    private fun bitmapDescriptorFromVector(
        vectorResId: Int, width: Int, height: Int): BitmapDescriptor? {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(requireContext(), vectorResId)
        if (vectorDrawable == null) {
            return null
        }
        // Create a new bitmap with desired width and height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Set bounds for the drawable
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        vectorDrawable.draw(canvas)

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    // Manage MapView Lifecycle
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        mapView.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        mapView.onDestroy()
        super.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun itemSelectUnSelect(id: Int?, status: String?, type: String?, position: Int?) {
        if (type == "SuperMarket") {
            storeUid = position?.let { stores[it].store_uuid.toString() }
            storeName = position?.let { stores[it].store_name.toString() }
            if (isOnline(requireActivity())) {
                (activity as MainActivity?)?.upBasket()
                selectSuperMarketApi()
            } else {
                BaseApplication.alertError(requireContext(), ErrorMessage.networkError, false)
            }
        }
    }

    private fun selectSuperMarketApi() {
        BaseApplication.showMe(requireContext())
        lifecycleScope.launch {
            basketDetailsSuperMarketViewModel.selectStoreProductUrl({
                BaseApplication.dismissMe()
                handleSelectSupermarketApiResponse(it)
            }, storeName, storeUid)
        }
    }

    private fun handleSelectSupermarketApiResponse(result: NetworkResult<String>) {
        when (result) {
            is NetworkResult.Success -> handleSuperMarketResponse(result.data.toString())
            is NetworkResult.Error -> showAlert(result.message, false)
            else -> showAlert(result.message, false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun handleSuperMarketResponse(data: String) {
        try {
            val apiModel = Gson().fromJson(data, SuccessResponseModel::class.java)
            Log.d("@@@ addMea List ", "message :- $data")
            if (apiModel.code == 200 && apiModel.success) {
                findNavController().navigateUp()
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


}