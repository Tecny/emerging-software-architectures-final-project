package com.example.dtaquito

import android.app.Application
import network.RetrofitClient

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializar RetrofitClient con el contexto de la aplicación
        RetrofitClient.initialize(this)
    }
}