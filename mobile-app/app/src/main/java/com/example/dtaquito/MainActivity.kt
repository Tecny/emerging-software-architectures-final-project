package com.example.dtaquito

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.example.dtaquito.profile.ProfileFragment
import com.example.dtaquito.reservation.ReservationFragment
import com.example.dtaquito.sports.SportFragment
import com.example.dtaquito.sportspace.SportSpaceFragment
import com.example.dtaquito.subscription.SubscriptionFragment
import com.example.dtaquito.tickets.TicketFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    var userRoleType: String = "PLAYER"

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica el idioma guardado antes de setContentView
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "es") ?: "es"
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        super.onCreate(savedInstanceState)
        CoroutineScope(Dispatchers.Main).launch {
            userRoleType = readUserRoleTypeFromPrefs()
            setContentView(R.layout.activity_main)
            bottomNav = findViewById(R.id.bottom_navigation)
            val menuRes = if (userRoleType == "PLAYER") R.menu.menu_player else R.menu.menu_propietario
            bottomNav.menu.clear()
            bottomNav.inflateMenu(menuRes)
            bottomNav.selectedItemId = when(userRoleType){
                "PLAYER" -> R.id.navigation_home
                else -> R.id.navigation_subscriptions
            }
            setupNavigation()
            val goToProfile = prefs.getBoolean("go_to_profile", false)
            if (goToProfile) {
                bottomNav.selectedItemId = R.id.navigation_profile
                loadFragment(ProfileFragment())
                prefs.edit { remove("go_to_profile") }
            } else if (savedInstanceState == null && supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
                if (userRoleType == "PLAYER") {
                    loadFragment(SportFragment())
                } else {
                    loadFragment(SubscriptionFragment())
                }
            }
        }
    }

    private fun setupNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> loadFragment(SportFragment())
                R.id.navigation_sportspaces -> loadFragment(SportSpaceFragment())
                R.id.navigation_reservations -> loadFragment(ReservationFragment())
                R.id.navigation_profile -> loadFragment(ProfileFragment())
                R.id.navigation_sportspaces_prop -> loadFragment(SportSpaceFragment())
                R.id.navigation_subscriptions -> loadFragment(SubscriptionFragment())
                R.id.navigation_tickets -> loadFragment(TicketFragment())
                else -> false
            }
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // Verifica si el fragmento ya está cargado
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null && currentFragment::class.java == fragment::class.java) {
            // Si el fragmento ya está cargado, no hacemos nada
            return
        }
        // Carga el fragmento en el contenedor
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun readUserRoleTypeFromPrefs(): String {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        return prefs.getString("role_type", "PLAYER") ?: "PLAYER"
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    // --- Métodos para actualización instantánea de textos ---
    
    fun updateAllFragmentsTexts() {
        // Actualiza los textos de todos los fragments visibles
        supportFragmentManager.fragments.forEach { fragment ->
            when (fragment) {
                is ProfileFragment -> fragment.updateTexts()
            }
        }
        updateBottomNavTexts()
    }

    fun updateBottomNavTexts() {
        val menu = bottomNav.menu
        when (userRoleType) {
            "PLAYER" -> {
                menu.findItem(R.id.navigation_home)?.title = getString(R.string.communityrooms)
                menu.findItem(R.id.navigation_sportspaces)?.title = getString(R.string.sport_spaces)
                menu.findItem(R.id.navigation_profile)?.title = getString(R.string.profile)
                // Agrega aquí los demás ítems si los tienes
            }
            else -> {
                menu.findItem(R.id.navigation_subscriptions)?.title = getString(R.string.subscriptions)
                menu.findItem(R.id.navigation_sportspaces_prop)?.title = getString(R.string.sport_spaces)
                menu.findItem(R.id.navigation_tickets)?.title = getString(R.string.tickets)
                menu.findItem(R.id.navigation_profile)?.title = getString(R.string.profile)
                // Agrega aquí los demás ítems si los tienes
            }
        }
    }
}