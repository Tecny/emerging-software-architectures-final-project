package com.example.dtaquito.auth

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import androidx.core.content.edit

class SaveCookieInterceptor(private val context: Context) : Interceptor {

    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val JWT_KEY = "jwt_token"
        private const val COOKIE_NAME = "JWT_TOKEN"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val cookies = response.headers("Set-Cookie")

        if (cookies.isNotEmpty()) {
            Log.d("SaveCookieInterceptor", "Cookies recibidas: $cookies")
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            try {
                for (cookie in cookies) {
                    if (cookie.contains(COOKIE_NAME)) {
                        val token = cookie.substringAfter("$COOKIE_NAME=").substringBefore(";")
                        if (token.isNotEmpty()) {
                            prefs.edit { putString(JWT_KEY, token) }
                            Log.d("SaveCookieInterceptor", "$COOKIE_NAME guardado: $token")
                        } else {
                            Log.w("SaveCookieInterceptor", "Se encontró un $COOKIE_NAME vacío, no se guardará.")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SaveCookieInterceptor", "Error al guardar el token JWT: ${e.message}", e)
            }
        } else {
            if (response.code == 401) { // Código HTTP 401: No autorizado
                Log.d("SaveCookieInterceptor", "La autenticación es requerida, pero no se proporcionaron cookies.")
            } else {
                Log.d("SaveCookieInterceptor", "No se requiere autenticación para esta respuesta.")
            }
        }

        return response
    }
}