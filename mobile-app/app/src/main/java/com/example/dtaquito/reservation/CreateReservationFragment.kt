package com.example.dtaquito.reservation

import Beans.availability.AvailabilityResponse
import Beans.reservations.Reservation
import Beans.sportspaces.SportSpace
import Interface.PlaceHolder
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.dtaquito.BuildConfig
import com.example.dtaquito.R
import network.RetrofitClient
import okhttp3.ResponseBody
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.annotations.MarkerOptions
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateReservationFragment : Fragment() {

    private lateinit var service: PlaceHolder
    private lateinit var roomNameInput: EditText
    private lateinit var dateInput: EditText
    private lateinit var timeInput: EditText
    private lateinit var endTimeInput: EditText
    private lateinit var typeReservationSpinner: Spinner
    private lateinit var detailsContainer: View

    private var selectedStartTime: String? = null
    private var selectedEndTime: String? = null
    private lateinit var hoursContainer: GridLayout
    private lateinit var selectedStartTimeText: TextView
    private lateinit var selectedEndTimeText: TextView

    private lateinit var detailMapView: MapView
    private var mapInitialized = false

    private var availableDates: List<String> = emptyList()
    private var dateAvailabilityMap: Map<String, List<String>> = emptyMap()
    private var selectedDate: String = ""
    private var availableHours: List<String> = emptyList()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inicializar MapLibre antes de inflar la vista
        MapLibre.getInstance(requireContext(), BuildConfig.LOCATIONIQ_API_KEY, WellKnownTileServer.MapTiler)

        // Inflar la vista después de inicializar MapLibre
        return inflater.inflate(R.layout.activity_create_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        service = RetrofitClient.instance.create(PlaceHolder::class.java)

        // Inicializar vistas para reserva
        roomNameInput = view.findViewById(R.id.room_name_input)
        dateInput = view.findViewById(R.id.date_input)
        timeInput = view.findViewById(R.id.time_input)
        endTimeInput = view.findViewById(R.id.endTime_input)

        // Inicializar iconos y textos seleccionados
        val dateIcon = view.findViewById<ImageView>(R.id.date_icon)
        val typeIcon = view.findViewById<ImageView>(R.id.type_icon)
        val dateSelectedText = view.findViewById<TextView>(R.id.date_selected_text)
        val typeSelectedText = view.findViewById<TextView>(R.id.type_selected_text)


        // Inicializar MapLibre
        MapLibre.getInstance(requireContext(), BuildConfig.LOCATIONIQ_API_KEY, WellKnownTileServer.MapTiler)

        // Inicializar nuevas vistas
        hoursContainer = view.findViewById(R.id.hours_container)
        selectedStartTimeText = view.findViewById(R.id.selected_start_time)
        selectedEndTimeText = view.findViewById(R.id.selected_end_time)

        // Establecer valores predeterminados
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dateInput.setText(currentDate)
        dateSelectedText.text = currentDate

        val defaultType = "Personal"
        typeSelectedText.text = defaultType

        // Inicializar contenedores
        val reservationContainer = view.findViewById<View>(R.id.reservation_container)
        detailsContainer = view.findViewById(R.id.details_container)

        // Inicializar botones de navegación
        val detailsBtn = view.findViewById<Button>(R.id.details_btn)
        val reserveBtn = view.findViewById<Button>(R.id.reserve_btn)

        // Inicializar el MapView
        detailMapView = view.findViewById(R.id.detail_mapView)
        detailMapView.onCreate(savedInstanceState)

        // Configurar botones de zoom
        val zoomInButton = view.findViewById<Button>(R.id.detail_zoom_in_button)
        val zoomOutButton = view.findViewById<Button>(R.id.detail_zoom_out_button)

        zoomInButton?.setOnClickListener {
            detailMapView.getMapAsync { map ->
                map.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.zoomIn()
                )
            }
        }

        zoomOutButton?.setOnClickListener {
            detailMapView.getMapAsync { map ->
                map.animateCamera(
                    org.maplibre.android.camera.CameraUpdateFactory.zoomOut()
                )
            }
        }

        // Obtener el ID del espacio deportivo
        val sportSpaceId = arguments?.getInt("sportSpaceId", -1) ?: -1
        Log.d("sportSpaceId", "Received sportSpaceId: $sportSpaceId")

        // Configurar listeners para los botones

        dateIcon.setOnClickListener {
            showDatePickerDialog()
            // La fecha seleccionada se actualizará en el método showDatePickerDialog()
        }

        typeIcon.setOnClickListener {
            // Mostrar el selector de tipo de reserva
            val typeOptions = listOf("Personal", "Comunidad")
            AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar tipo de reserva")
                .setItems(typeOptions.toTypedArray()) { _, which ->
                    val selectedType = typeOptions[which]
                    // Actualizar el spinner aunque esté oculto
                    val adapter = typeReservationSpinner.adapter as ArrayAdapter<String>
                    val position = adapter.getPosition(selectedType)
                    typeReservationSpinner.setSelection(position)

                    // Actualizar el texto seleccionado
                    typeSelectedText.text = selectedType
                }
                .show()
        }

        detailsBtn.setOnClickListener {
            detailsContainer.visibility = View.VISIBLE
            reservationContainer.visibility = View.GONE
            view.findViewById<View>(R.id.icons_container).visibility = View.GONE // Ocultar iconos

            // Cambiar apariencia de botones
            detailsBtn.setBackgroundResource(R.color.selected_tab_color)
            reserveBtn.setBackgroundResource(R.color.unselected_tab_color)
        }

        reserveBtn.setOnClickListener {
            detailsContainer.visibility = View.GONE
            reservationContainer.visibility = View.VISIBLE
            view.findViewById<View>(R.id.icons_container).visibility = View.VISIBLE // Mostrar iconos

            // Cambiar apariencia de botones
            detailsBtn.setBackgroundResource(R.color.unselected_tab_color)
            reserveBtn.setBackgroundResource(R.color.selected_tab_color)
        }

        // Por defecto, mostrar la pestaña de reserva
// Por defecto, mostrar la pestaña de reserva
        detailsContainer.visibility = View.VISIBLE
        reservationContainer.visibility = View.GONE
        view.findViewById<View>(R.id.icons_container).visibility = View.GONE// Asegurar que los iconos estén visibles

        // Configurar spinner
        typeReservationSpinner = view.findViewById(R.id.typeReservation_spinner)
        val typeOptions = listOf("Personal", "Comunidad")
        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_items2, typeOptions)
        adapter.setDropDownViewResource(R.layout.spinner_items2)
        typeReservationSpinner.adapter = adapter

        // Cargar datos del espacio deportivo y disponibilidad
        if (sportSpaceId != -1) {
            loadSportSpaceDetails(sportSpaceId)
            loadAvailability(sportSpaceId)
        } else {
            Toast.makeText(requireContext(), "ID de espacio deportivo no válido", Toast.LENGTH_SHORT).show()
        }

        // Resto de la configuración...
        dateInput.setOnClickListener { showDatePickerDialog() }
        timeInput.setOnClickListener { showStartTimeDialog() }
        endTimeInput.setOnClickListener { showEndTimeDialog() }

        val createBtn = view.findViewById<Button>(R.id.create_btn)
        createBtn.setOnClickListener {
            createReservation(sportSpaceId, typeReservationSpinner.selectedItem.toString())
        }
    }

    private fun loadSportSpaceDetails(sportSpaceId: Int) {
        service.getSportSpaceById(sportSpaceId).enqueue(object : Callback<SportSpace> {
            override fun onResponse(call: Call<SportSpace>, response: Response<SportSpace>) {
                if (response.isSuccessful && response.body() != null) {
                    val sportSpace = response.body()!!

                    // Cargar imagen
                    val imageView = view?.findViewById<ImageView>(R.id.imageSportSpace)
                    imageView?.let {
                        Glide.with(requireContext())
                            .load(sportSpace.imageUrl)
                            .into(it)
                    }

                    // Actualizar información en la pestaña de detalles
                    view?.findViewById<TextView>(R.id.sport_space_name)?.text = sportSpace.name
                    view?.findViewById<TextView>(R.id.sport_space_description)?.text = sportSpace.description
                    view?.findViewById<TextView>(R.id.sport_space_price_value)?.text = "S/${sportSpace.price}"
                    view?.findViewById<TextView>(R.id.sport_space_address_value)?.text = sportSpace.address
                    view?.findViewById<TextView>(R.id.sport_space_hours_value)?.text = "${sportSpace.openTime} - ${sportSpace.closeTime}"
                    view?.findViewById<TextView>(R.id.sport_space_gamemode_value)?.text = formatGameMode(sportSpace.gamemodeType)

                    // Inicializar mapa con ubicación
                    if (sportSpace.latitude != null && sportSpace.longitude != null) {
                        initializeLocationMap(sportSpace.latitude, sportSpace.longitude)
                    } else {
                        Toast.makeText(requireContext(), "No hay coordenadas disponibles", Toast.LENGTH_SHORT).show()
                    }

                    // Cargar disponibilidad
                    loadAvailability(sportSpaceId)
                } else {
                    Toast.makeText(requireContext(), "Error al cargar detalles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<SportSpace>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initializeLocationMap(latitude: Double, longitude: Double) {
        detailMapView.getMapAsync { mapLibreMap ->
            // Configurar estilo del mapa
            mapLibreMap.setStyle("https://tiles.locationiq.com/v3/streets/vector.json?key=${BuildConfig.LOCATIONIQ_API_KEY}") {
                val location = LatLng(latitude, longitude)

                // Log para depuración
                Log.d("MapDebug", "Inicializando mapa en: $latitude, $longitude")

                // Posicionar cámara en la ubicación
                mapLibreMap.cameraPosition = CameraPosition.Builder()
                    .target(location)
                    .zoom(15.0)
                    .build()

                // Agregar marcador
                val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.place)
                val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                val icon = bitmap?.let {
                    org.maplibre.android.annotations.IconFactory.getInstance(requireContext()).fromBitmap(it)
                }

                // Añadir marcador
                mapLibreMap.addMarker(
                    MarkerOptions()
                        .position(location)
                        .title("Ubicación")
                        .icon(icon)
                )

                mapInitialized = true
            }

            // Configuración de UI
            mapLibreMap.uiSettings.isZoomGesturesEnabled = true
            mapLibreMap.uiSettings.isRotateGesturesEnabled = true
            mapLibreMap.uiSettings.isScrollGesturesEnabled = true
        }
    }

    private fun createReservation(sportSpaceId: Int, reservationType: String) {
        // Validar que todos los campos estén completos
        val roomName = roomNameInput.text.toString()
        val gameDay = dateInput.text.toString()
        val startTime = timeInput.text.toString()
        val endTime = endTimeInput.text.toString()

        if (roomName.isEmpty() || gameDay.isEmpty() || startTime.isEmpty() || endTime.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Convertir el tipo de reserva al formato esperado por el API
        val type = when(reservationType.lowercase()) {
            "comunidad" -> "COMMUNITY"
            "personal" -> "PERSONAL"
            else -> reservationType.uppercase()
        }

        // Crear objeto de solicitud
        val reservationRequest = Reservation(
            gameDay = gameDay,
            startTime = startTime,
            endTime = endTime,
            sportSpacesId = sportSpaceId,
            type = type,
            reservationName = roomName
        )

        // Enviar solicitud al servidor
        service.createReservation(reservationRequest).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Reserva creada con éxito", Toast.LENGTH_SHORT).show()
                    // Regresar al fragmento anterior
                    requireActivity().supportFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Error al crear la reserva: ${response.code()}", Toast.LENGTH_SHORT).show()
                    Log.e("CreateReservation", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(requireContext(), "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("CreateReservation", "Error: ${t.message}")
            }
        })
    }

    private fun loadAvailability(sportSpaceId: Int) {
        service.getSportSpaceAvailability(sportSpaceId).enqueue(object :
            Callback<AvailabilityResponse> {
            override fun onResponse(call: Call<AvailabilityResponse>, response: Response<AvailabilityResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val availability = response.body()!!
                    dateAvailabilityMap = availability.weeklyAvailability
                    availableDates = dateAvailabilityMap.keys.toList()

                    // Habilitar selección de fecha
                    dateInput.isEnabled = true

                    // Establecer fecha predeterminada si hay fechas disponibles
                    if (availableDates.isNotEmpty()) {
                        // Intentar usar la fecha actual si está disponible
                        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        if (availableDates.contains(currentDate)) {
                            selectedDate = currentDate
                        } else {
                            // Si la fecha actual no está disponible, usar la primera fecha disponible
                            selectedDate = availableDates[0]
                        }

                        // Actualizar UI
                        dateInput.setText(selectedDate)
                        view?.findViewById<TextView>(R.id.date_selected_text)?.text = selectedDate

                        // Cargar horas disponibles para esta fecha
                        availableHours = dateAvailabilityMap[selectedDate] ?: emptyList()
                        timeInput.isEnabled = true

                        // Actualizar botones de horas - AÑADIR ESTA LÍNEA
                        updateHoursButtons()
                    }

                    // Establecer tipo predeterminado
                    val typeOptions = listOf("Personal", "Comunidad")
                    val adapter = typeReservationSpinner.adapter as ArrayAdapter<String>
                    val position = adapter.getPosition("Personal")
                    typeReservationSpinner.setSelection(position)
                } else {
                    Toast.makeText(requireContext(), "Error al cargar disponibilidad", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<AvailabilityResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateHoursButtons() {
        // Limpiar el contenedor
        hoursContainer.removeAllViews()

        // Resetear selecciones
        selectedStartTime = null
        selectedEndTime = null
        selectedStartTimeText.text = ""
        selectedEndTimeText.text = ""

        // Crear botones para cada hora disponible
        for (hour in availableHours) {
            val button = Button(requireContext()).apply {
                text = hour
                setBackgroundResource(R.drawable.input_text)
                setTextColor(resources.getColor(R.color.white, null))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    onHourButtonClicked(hour)
                }
            }
            hoursContainer.addView(button)
        }
    }

    private fun updateButtonAppearance(hour: String, isSelected: Boolean) {
        for (i in 0 until hoursContainer.childCount) {
            val button = hoursContainer.getChildAt(i) as Button
            if (button.text == hour) {
                button.setBackgroundResource(
                    if (isSelected) R.drawable.button_rounded else R.drawable.input_text
                )
            }
        }
    }

    private fun onHourButtonClicked(hour: String) {
        when {
            selectedStartTime == null -> {
                // Primera selección - hora de inicio
                selectedStartTime = hour
                selectedStartTimeText.text = hour
                timeInput.setText(hour)

                // Actualizar apariencia de botones
                updateButtonAppearance(hour, true)
            }
            selectedEndTime == null && hour > selectedStartTime!! -> {
                // Segunda selección - hora de fin
                selectedEndTime = hour
                selectedEndTimeText.text = hour
                endTimeInput.setText(hour)

                // Actualizar apariencia de botones
                updateButtonAppearance(hour, true)
            }
            else -> {
                // Reiniciar selecciones
                if (selectedEndTime != null) {
                    // Limpiar apariencia de todos los botones
                    for (i in 0 until hoursContainer.childCount) {
                        val button = hoursContainer.getChildAt(i) as Button
                        button.setBackgroundResource(R.drawable.input_text)
                    }

                    selectedStartTime = hour
                    selectedStartTimeText.text = hour
                    timeInput.setText(hour)
                    selectedEndTime = null
                    selectedEndTimeText.text = ""
                    endTimeInput.setText("")

                    // Actualizar apariencia
                    updateButtonAppearance(hour, true)
                }
            }
        }
    }
    private fun showDatePickerDialog() {
        if (availableDates.isEmpty()) {
            Toast.makeText(requireContext(), "No hay fechas disponibles", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener la fecha actual en formato "yyyy-MM-dd"
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Filtrar solo fechas iguales o posteriores a la fecha actual
        val futureAvailableDates = availableDates.filter { it >= currentDate }

        if (futureAvailableDates.isEmpty()) {
            Toast.makeText(requireContext(), "No hay fechas disponibles a partir de hoy", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar fecha disponible")
            .setItems(futureAvailableDates.toTypedArray()) { _, which ->
                selectedDate = futureAvailableDates[which]
                dateInput.setText(selectedDate)

                // Actualizar el texto seleccionado
                view?.findViewById<TextView>(R.id.date_selected_text)?.text = selectedDate

                // Actualizar horas disponibles para esta fecha
                availableHours = dateAvailabilityMap[selectedDate] ?: emptyList()
                updateHoursButtons()
            }
            .show()
    }

    private fun showStartTimeDialog() {
        if (availableHours.isEmpty()) {
            Toast.makeText(requireContext(), "No hay horarios disponibles para esta fecha", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar hora de inicio")
            .setItems(availableHours.toTypedArray()) { _, which ->
                val selectedTime = availableHours[which]
                timeInput.setText(selectedTime)
                endTimeInput.isEnabled = true
                endTimeInput.setText("")
            }
            .show()
    }

    private fun showEndTimeDialog() {
        val startTime = timeInput.text.toString()
        if (startTime.isEmpty()) {
            Toast.makeText(requireContext(), "Primero selecciona la hora de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        // Filtrar solo horas posteriores a la hora de inicio
        val availableEndHours = availableHours.filter { it > startTime }

        if (availableEndHours.isEmpty()) {
            Toast.makeText(requireContext(), "No hay horas disponibles después de $startTime", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar hora de fin")
            .setItems(availableEndHours.toTypedArray()) { _, which ->
                val selectedEndTime = availableEndHours[which]
                endTimeInput.setText(selectedEndTime)
            }
            .show()
    }

    private fun formatGameMode(gameMode: String): String {
        return when (gameMode) {
            "FUTBOL_11" -> "Futbol 11"
            "FUTBOL_7" -> "Futbol 7"
            "FUTBOL_8" -> "Futbol 8"
            "FUTBOL_5" -> "Futbol 5"
            "BILLAR_3" -> "Billar 3"
            else -> gameMode // En caso de un valor no reconocido
        }
    }
    override fun onStart() {
        super.onStart()
        if (::detailMapView.isInitialized) detailMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (::detailMapView.isInitialized) detailMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (::detailMapView.isInitialized) detailMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        if (::detailMapView.isInitialized) detailMapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        if (::detailMapView.isInitialized) detailMapView.onLowMemory()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::detailMapView.isInitialized) detailMapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::detailMapView.isInitialized) detailMapView.onSaveInstanceState(outState)
    }
}