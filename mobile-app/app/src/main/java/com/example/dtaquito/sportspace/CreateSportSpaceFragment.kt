package com.example.dtaquito.sportspace

import Interface.PlaceHolder
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import com.example.dtaquito.BuildConfig
import com.example.dtaquito.R
import com.example.dtaquito.time.TimePickerFragment
import com.example.dtaquito.utils.showToast
import com.google.android.material.textfield.TextInputLayout
import network.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException

class CreateSportSpaceFragment : Fragment() {

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1001
        private const val LOCATION_PERMISSION_CODE = 2001
    }

    private lateinit var imageView: ImageView
    private lateinit var imgUrlEditText: TextView
    private lateinit var mapView: MapView
    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        MapLibre.getInstance(requireContext(), BuildConfig.LOCATIONIQ_API_KEY, WellKnownTileServer.MapTiler)
        return inflater.inflate(R.layout.activity_create_sport_space, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkLocationPermission()
        imageView = view.findViewById(R.id.image_view)
        imgUrlEditText = view.findViewById(R.id.img_url)
        val imgUrlLayout = view.findViewById<TextInputLayout>(R.id.img_url_layout)
        imgUrlLayout.setEndIconOnClickListener { checkStoragePermission() }
        val spinnerSport = view.findViewById<Spinner>(R.id.sport_input)
        val spinnerFormat = view.findViewById<Spinner>(R.id.format_input)
        val nameInput = view.findViewById<EditText>(R.id.name_input)
        val descriptionInput = view.findViewById<EditText>(R.id.description_input)
        val startTimeInput = view.findViewById<EditText>(R.id.start_time_input)
        val endTimeInput = view.findViewById<EditText>(R.id.end_time_input)
        val addressInput = view.findViewById<TextView>(R.id.address_input)
        val priceInput = view.findViewById<EditText>(R.id.price_input)
        val createSportSpaceButton = view.findViewById<Button>(R.id.create_space_btn)
        mapView = view.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        val zoomInButton = view.findViewById<Button>(R.id.zoom_in_button)
        val zoomOutButton = view.findViewById<Button>(R.id.zoom_out_button)
        initializeMap(zoomInButton, zoomOutButton)

        val scrollView = view.findViewById<ViewGroup>(R.id.scrollView)
        setupMapTouchHandling(mapView, scrollView)

        setupSpinners(spinnerSport, spinnerFormat)
        setupTimePickers(startTimeInput, endTimeInput)

        createSportSpaceButton.setOnClickListener {
            val price = priceInput.text.toString().toDoubleOrNull() ?: 0.0
            createSportSpace(
                nameInput.text.toString(),
                descriptionInput.text.toString(),
                startTimeInput.text.toString(),
                endTimeInput.text.toString(),
                spinnerSport.selectedItem.toString(),
                addressInput.text.toString(),
                price,
                spinnerFormat.selectedItem.toString(),
                selectedLatitude,
                selectedLongitude
            )
        }
    }

    private fun checkLocationPermission() {
        val fineLocation = android.Manifest.permission.ACCESS_FINE_LOCATION
        val coarseLocation = android.Manifest.permission.ACCESS_COARSE_LOCATION
        if (ContextCompat.checkSelfPermission(requireContext(), fineLocation) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(requireContext(), coarseLocation) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(fineLocation, coarseLocation), LOCATION_PERMISSION_CODE)
        }
    }

    private fun setupMapTouchHandling(mapView: MapView, scrollView: ViewGroup) {
        mapView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> scrollView.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> scrollView.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    private fun initializeMap(zoomInButton: Button, zoomOutButton: Button) {
        mapView.getMapAsync { mapLibreMap ->
            mapLibreMap.setStyle("https://tiles.locationiq.com/v3/streets/vector.json?key=${BuildConfig.LOCATIONIQ_API_KEY}") {
                val limaLatLng = LatLng(-12.0464, -77.0428)
                mapLibreMap.cameraPosition = CameraPosition.Builder()
                    .target(limaLatLng)
                    .zoom(20.0)
                    .build()
                setupMapClickListener(mapLibreMap)
            }
            mapLibreMap.uiSettings.isZoomGesturesEnabled = true
            mapLibreMap.uiSettings.isRotateGesturesEnabled = true
            mapLibreMap.uiSettings.isScrollGesturesEnabled = true

            // Listeners para los botones de zoom
            zoomInButton.setOnClickListener {
                mapLibreMap.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.zoomIn()
                )
            }
            zoomOutButton.setOnClickListener {
                mapLibreMap.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.zoomOut()
                )
            }
        }
    }

    private fun setupMapClickListener(mapLibreMap: MapLibreMap) {
        mapLibreMap.addOnMapClickListener { point ->
            selectedLatitude = point.latitude
            selectedLongitude = point.longitude
            mapLibreMap.clear()
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.place)
            val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            val icon = bitmap?.let {
                org.maplibre.android.annotations.IconFactory.getInstance(requireContext()).fromBitmap(it)
            }
            mapLibreMap.addMarker(
                MarkerOptions()
                    .position(LatLng(selectedLatitude, selectedLongitude))
                    .title("Selected Location")
                    .icon(icon)
            )
            fetchAddressFromLocationIQ(selectedLatitude, selectedLongitude)
            true
        }
    }

    private fun fetchAddressFromLocationIQ(latitude: Double, longitude: Double) {
        val apiKey = BuildConfig.LOCATIONIQ_API_KEY
        val url = "https://us1.locationiq.com/v1/reverse.php?key=$apiKey&lat=$latitude&lon=$longitude&format=json"
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                requireActivity().runOnUiThread { requireContext().showToast("Error al obtener la dirección") }
            }
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (response.isSuccessful) {
                    response.body?.string()?.let { responseBody ->
                        val jsonObject = org.json.JSONObject(responseBody)
                        val address = jsonObject.getString("display_name")
                        requireActivity().runOnUiThread {
                            view?.findViewById<TextView>(R.id.address_input)?.text = address
                        }
                    }
                } else {
                    requireActivity().runOnUiThread { requireContext().showToast("No se pudo obtener la dirección") }
                }
            }
        })
    }

    private fun setupSpinners(spinnerSport: Spinner, spinnerFormat: Spinner) {
        val sports = listOf(
            getString(R.string.select_sport),
            getString(R.string.soccer),
            getString(R.string.pool)
        )
        val formatsSoccer = listOf(
            getString(R.string.select_format),
            getString(R.string.soccer_5),
            getString(R.string.soccer_7),
            getString(R.string.soccer_8),
            getString(R.string.soccer_11)
        )
        val formatsPool = listOf(
            getString(R.string.select_format),
            getString(R.string.pool_3)
        )
        setupSpinner(spinnerSport, sports) { selectedSport ->
            val formats = if (selectedSport == getString(R.string.pool)) formatsPool else formatsSoccer
            setupSpinner(spinnerFormat, formats) {}
        }
    }

    private fun setupSpinner(spinner: Spinner, items: List<String>, onItemSelected: (String) -> Unit) {
        val adapter = object : ArrayAdapter<String>(requireContext(), R.layout.spinner_items, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(
                    if (position == 0) "#4D4D4D".toColorInt()
                    else "#FFFFFF".toColorInt()
                )
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view as TextView
                textView.setTextColor(
                    if (position == 0) "#4D4D4D".toColorInt()
                    else "#FFFFFF".toColorInt()
                )
                return view
            }
        }
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected(items[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupTimePickers(startTimeInput: EditText, endTimeInput: EditText) {
        startTimeInput.setOnClickListener { showTimePickerDialog(startTimeInput) }
        endTimeInput.setOnClickListener { showTimePickerDialog(endTimeInput) }
    }

    private fun showTimePickerDialog(editText: EditText) {
        val timePicker = TimePickerFragment { time -> editText.setText(time) }
        timePicker.show(parentFragmentManager, "timePicker")
    }

   private fun checkStoragePermission() {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
           val permission = android.Manifest.permission.READ_MEDIA_IMAGES
           if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
               if (shouldShowRequestPermissionRationale(permission)) {
                   requireContext().showToast("Por favor, concede el permiso de fotos desde Ajustes")
               } else {
                   requestPermissions(arrayOf(permission), STORAGE_PERMISSION_CODE)
               }
           } else {
               openGallery()
           }
       } else {
           if (ContextCompat.checkSelfPermission(requireContext(), READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
               if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
                   requireContext().showToast("Por favor, concede el permiso desde Ajustes")
               } else {
                   requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
               }
           } else {
               openGallery()
           }
       }
   }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                val deniedPermission = permissions.getOrNull(0)
                if (deniedPermission != null && !shouldShowRequestPermissionRationale(deniedPermission)) {
                    requireContext().showToast("Permiso denegado permanentemente. Ve a Ajustes para habilitarlo.")
                } else {
                    requireContext().showToast("Permiso denegado")
                }
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                selectedImageUri = uri
                imageView.setImageURI(uri)
                val fileName = getFileName(uri)
                if (fileName != null) {
                    imgUrlEditText.text = fileName
                    imgUrlEditText.hint = ""
                    val heightInPixels = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        300f,
                        resources.displayMetrics
                    ).toInt()
                    imageView.layoutParams.height = heightInPixels
                    imageView.visibility = View.VISIBLE
                    imageView.requestLayout()
                } else {
                    requireContext().showToast("No se pudo obtener la ruta del archivo")
                }
            } else {
                requireContext().showToast("No se pudo cargar la imagen")
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                cursor.moveToFirst()
                fileName = cursor.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun createSportSpace(
        name: String,
        description: String,
        openTime: String,
        closeTime: String,
        sportType: String,
        address: String,
        price: Double,
        gamemode: String,
        latitude: Double,
        longitude: Double
    ) {
        val sportId = if (sportType == getString(R.string.soccer)) 1 else 2
        val gamemodeId = when (gamemode) {
            getString(R.string.soccer_7) -> 2
            getString(R.string.soccer_8) -> 3
            getString(R.string.soccer_5) -> 4
            getString(R.string.pool_3)   -> 5
            getString(R.string.soccer_11) -> 1
            else -> 0
        }

        val namePart = name.toRequestBody("text/plain".toMediaTypeOrNull())
        val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
        val openTimePart = openTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val closeTimePart = closeTime.toRequestBody("text/plain".toMediaTypeOrNull())
        val sportIdPart = sportId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val addressPart = address.toRequestBody("text/plain".toMediaTypeOrNull())
        val pricePart = price.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val gamemodeIdPart = gamemodeId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val latitudePart = latitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val longitudePart = longitude.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val tempFile = selectedImageUri?.let { uri ->
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File.createTempFile("upload", ".jpg", requireContext().cacheDir)
            inputStream?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        }

        if (tempFile == null) {
            requireContext().showToast("No se pudo procesar la imagen")
            return
        }

        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)

        service.createSportSpace(
            namePart, sportIdPart, imagePart, pricePart, addressPart,
            descriptionPart, openTimePart, closeTimePart, gamemodeIdPart, latitudePart, longitudePart
        ).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    requireContext().showToast("Sport space created successfully")
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                } else {
                    requireContext().showToast("Error: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                requireContext().showToast("Network error: ${t.message}")
            }
        })
    }

    // Métodos del ciclo de vida del MapView
    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroyView() { super.onDestroyView(); mapView.onDestroy() }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }
}