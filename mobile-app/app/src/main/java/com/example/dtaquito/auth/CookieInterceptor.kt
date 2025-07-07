package com.example.dtaquito.auth

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class CookieInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        if (token != null) {
            android.util.Log.d("CookieInterceptor", "Token enviado: Bearer $token")
        } else {
            android.util.Log.e("CookieInterceptor", "No se encontró token para enviar")
        }

        return chain.proceed(request)
    }
}