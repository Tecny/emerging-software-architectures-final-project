package com.example.dtaquito.qr

import Interface.PlaceHolder
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.dtaquito.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QrFragment : Fragment() {
    private var reservationId: Int? = null
    private lateinit var imgQr: ImageView
    private lateinit var tvMessage: TextView
    private val apiService = RetrofitClient.instance.create(PlaceHolder::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            reservationId = it.getInt(ARG_RESERVATION_ID)
            Log.d("QrFragment", "Reservation ID: $reservationId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_qr, container, false)
        imgQr = view.findViewById(R.id.img_qr)

        // Añade un TextView para mostrar mensajes al usuario
        tvMessage = view.findViewById(R.id.textView)

        reservationId?.let { id ->
            loadQrImage(id)
        } ?: run {
            tvMessage.text = "Error: ID de reserva no encontrado"
        }

        return view
    }

    private fun loadQrImage(reservationId: Int) {
        Log.d("QrFragment", "Solicitando token QR para reservationId: $reservationId")

        apiService.generateQrToken(reservationId).enqueue(object : Callback<Map<String, String>> {
            override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                Log.d("QrFragment", "Respuesta token QR: ${response.code()} - ${response.message()}")

                if (response.isSuccessful) {
                    val tokenResponse = response.body()
                    Log.d("QrFragment", "Contenido respuesta: $tokenResponse")

                    val token = tokenResponse?.get("qrToken")
                    Log.d("QrFragment", "Token obtenido: $token")

                    if (token != null) {
                        generateQrImage(token)
                    } else {
                        tvMessage.text = "Token QR no encontrado en la respuesta"
                    }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("QrFragment", "Error al obtener token: ${response.code()} - Error body: $errorBody")

                        // En el metodo onResponse, donde se muestra el ícono de espera:
                        // En la parte donde se muestra el ícono de espera:
                        try {
                            val errorJson = JSONObject(errorBody ?: "{}")

                            tvMessage.text = "El código QR aún no está disponible"
                            tvMessage.textSize = 30f // Texto más grande para mayor visibilidad


                            imgQr.setImageResource(R.drawable.ic_qr_pending)
                            imgQr.scaleType = ImageView.ScaleType.FIT_CENTER
                            imgQr.setBackgroundColor(android.graphics.Color.TRANSPARENT)


                            val layoutParams = imgQr.layoutParams
                            layoutParams.height = (resources.getDimensionPixelSize(R.dimen.qr_image_height) * 0.25).toInt()

                            if (layoutParams is ViewGroup.MarginLayoutParams) {
                                layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.activity_vertical_margin) * 5
                                // Centrar horizontalmente
                                layoutParams.leftMargin = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
                                layoutParams.rightMargin = resources.getDimensionPixelSize(R.dimen.activity_horizontal_margin)
                            }

                            imgQr.layoutParams = layoutParams

                            // Mostrar información adicional
                            Toast.makeText(context, "Los códigos QR suelen estar disponibles 1 hora antes de la reserva",
                                Toast.LENGTH_LONG).show()
                        } catch (e: Exception) {
                            tvMessage.text = "Error al generar QR. Intente más tarde."
                        }
                    } catch (e: Exception) {
                        Log.e("QrFragment", "Error al leer el cuerpo del error: ${e.message}")
                        tvMessage.text = "Error al generar QR. Intente más tarde."
                    }
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("QrFragment", "Error de conexión: ${t.message}", t)
                tvMessage.text = "Error de conexión. Revise su conexión a internet."
            }
        })
    }

    private fun generateQrImage(token: String) {
        Log.d("QrFragment", "Solicitando imagen QR con token: $token")

        apiService.generateQrImage(token).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("QrFragment", "Respuesta imagen QR: ${response.code()} - ${response.message()}")

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val inputStream = body.byteStream()
                                val bitmap = BitmapFactory.decodeStream(inputStream)

                                withContext(Dispatchers.Main) {
                                    if (bitmap != null) {
                                        // Restaurar configuración original del ImageView
                                        val layoutParams = imgQr.layoutParams
                                        layoutParams.height = resources.getDimensionPixelSize(R.dimen.qr_image_height) // Usa una dimensión definida
                                        imgQr.layoutParams = layoutParams
                                        imgQr.scaleType = ImageView.ScaleType.FIT_CENTER // Restaurar escala original

                                        // Mostrar la imagen QR
                                        imgQr.setImageBitmap(bitmap)
                                        tvMessage.text = "Escanear QR"
                                        Log.d("QrFragment", "Imagen QR mostrada exitosamente")
                                    } else {
                                        Log.e("QrFragment", "Bitmap nulo después de decodificar")
                                        tvMessage.text = "Error al mostrar la imagen QR"
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("QrFragment", "Error procesando la imagen: ${e.message}", e)
                                withContext(Dispatchers.Main) {
                                    tvMessage.text = "Error: ${e.message}"
                                }
                            }
                        }
                    }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        Log.e("QrFragment", "Error al obtener imagen: ${response.code()} - Error body: $errorBody")
                        tvMessage.text = "Error: No se pudo obtener la imagen QR"
                    } catch (e: Exception) {
                        Log.e("QrFragment", "Error al leer el cuerpo del error: ${e.message}")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("QrFragment", "Error de conexión: ${t.message}", t)
                tvMessage.text = "Error de conexión. Revise su conexión a internet."
            }
        })
    }

    companion object {
        private const val ARG_RESERVATION_ID = "reservation_id"

        @JvmStatic
        fun newInstance(reservationId: Int) =
            QrFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_RESERVATION_ID, reservationId)
                }
            }
    }
}