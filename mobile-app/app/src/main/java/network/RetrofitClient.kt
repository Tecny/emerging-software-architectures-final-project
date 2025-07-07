package network

import Beans.chat.ChatMessage
import MyCookieJar
import android.content.Context
import com.example.dtaquito.auth.CookieInterceptor
import com.example.dtaquito.auth.SaveCookieInterceptor
import com.example.dtaquito.chat.ChatMessageDeserializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = ""
    internal const val CHAT_URL = ""
    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val client: OkHttpClient by lazy {
        val context = appContext ?: throw IllegalStateException("RetrofitClient not initialized.")
        OkHttpClient.Builder()
            .addInterceptor(SaveCookieInterceptor(context))
            .addInterceptor(CookieInterceptor(context))
            .cookieJar(MyCookieJar())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(
                ChatMessage::class.java,
                ChatMessageDeserializer()
            )
            .create()
    }

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                // aquí le indicas que convierta con tu gson
                GsonConverterFactory.create(gson)
            )
            .build()
    }
}