package com.example.dtaquito.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.dtaquito.MainActivity
import com.example.dtaquito.R
import com.example.dtaquito.login.LoginActivity

class SplashScreen : AppCompatActivity() {
    
    companion object {
        private const val SPLASH_TIMER = 2000L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            FLAG_FULLSCREEN,
            FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_splash_screen)

        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val savedUserId = prefs.getInt("user_id", -1)
        val rememberMe = prefs.getBoolean("remember_me", false)
        val isTemporarySession = prefs.getBoolean("is_temporary_session", false)
        val roleType = prefs.getString("role_type", null)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (savedUserId != -1 && rememberMe && !isTemporarySession && roleType != null) {
                // Solo ir directo a MainActivity si el usuario marcó "Remember me" Y no es sesión temporal
                Intent(this@SplashScreen, MainActivity::class.java).apply {
                    putExtra("ROLE_TYPE", roleType)
                }
            } else {
                // Si no hay "Remember me" o es sesión temporal, ir al Login y limpiar
                if (savedUserId != -1 && (isTemporarySession || !rememberMe)) {
                    clearTemporarySession()
                }
                Intent(this@SplashScreen, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, SPLASH_TIMER)
    }
    
    private fun clearTemporarySession() {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit().apply {
            remove("user_id")
            remove("user_name")
            remove("user_email")
            remove("role_type")
            remove("credits")
            remove("jwt_token")
            remove("is_temporary_session")
            remove("remember_me")
            apply()
        }
    }
}