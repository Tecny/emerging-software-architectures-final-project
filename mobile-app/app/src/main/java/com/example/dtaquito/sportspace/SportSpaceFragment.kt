package com.example.dtaquito.sportspace

import Beans.sportspaces.SportSpace
import Interface.PlaceHolder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import com.example.dtaquito.utils.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class SportSpaceFragment : Fragment() {

    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SportSpaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_sport_space, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        setupCreateSportSpaceButton(view)
        fetchUserRoleAndLoadSportSpaces()
    }

    override fun onResume() {
        super.onResume()
        updateTexts()
    }

    fun updateTexts() {
        view?.findViewById<Button>(R.id.create_sport_space_btn)?.text = getString(R.string.create)
    }

    private fun setupCreateSportSpaceButton(view: View) {
        val createSportSpaceBtn = view.findViewById<Button>(R.id.create_sport_space_btn)

        createSportSpaceBtn.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val userRoleType = withContext(Dispatchers.IO) { fillUserProfile() }

                if (userRoleType == "OWNER") {
                    try {
                        val (userPlan, userSportSpaces) = withContext(Dispatchers.IO) {
                            val plan = getUserPlan()
                            val sportSpacesResponse = service.getSportSpacesByUserId().execute()
                            val sportSpaces = if (sportSpacesResponse.isSuccessful) {
                                sportSpacesResponse.body() ?: emptyList()
                            } else if (sportSpacesResponse.code() == 404) {
                                emptyList()
                            } else {
                                throw Exception("Error ${sportSpacesResponse.code()}")
                            }
                            plan to sportSpaces
                        }

                        val maxSpaces = when (userPlan.lowercase()) {
                            "bronze" -> 1
                            "silver" -> 2
                            "gold" -> 3
                            else -> 0
                        }

                        if (userSportSpaces.size < maxSpaces) {
                            createSportSpaceBtn.visibility = View.VISIBLE
                            createSportSpaceBtn.setOnClickListener {
                                if (userSportSpaces.size >= maxSpaces) {
                                    context?.showToast("Mejora tu plan para crear más espacios deportivos")
                                } else {
                                    parentFragmentManager.beginTransaction()
                                        .replace(
                                            R.id.fragment_container,
                                            CreateSportSpaceFragment()
                                        )
                                        .addToBackStack(null)
                                        .commit()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(
                            "SportSpaceFragment",
                            "Error cargando espacios del usuario: ${e.message}"
                        )
                        context?.showToast("Error al cargar tus espacios deportivos.")
                    }
                }
            } catch (e: Exception) {
                Log.e("SportSpaceFragment", "Error al obtener el rol del usuario: ${e.message}")
                context?.showToast("Error al obtener tu rol de usuario.")
            }
        }
    }

    private fun getUserPlan(): String {
        return try {
            val response = service.getCurrentSubscription().execute()
            if (response.isSuccessful) {
                response.body()?.planType?.lowercase() ?: "bronze"
            } else {
                "bronze"
            }
        } catch (_: Exception) {
            "bronze"
        }
    }

    private fun fetchUserRoleAndLoadSportSpaces() {
        lifecycleScope.launch {
            try {
                val role = withContext(Dispatchers.IO) { fillUserProfile() }
                Log.d("SportSpaceFragment", "User role: $role")
                fetchSportSpaces(role)
            } catch (e: Exception) {
                context?.showToast("Error: ${e.message}")
            }
        }
    }

    private fun fetchSportSpaces(userRole: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (userRole == "OWNER") {
                    service.getSportSpacesByUserId().execute()
                } else {
                    service.getAllSportSpaces().execute()
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful || response.code() == 404) {
                        handleSuccessfulResponse(response.body() ?: emptyList())
                    } else {
                        handleErrorResponse(response.code())
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    context?.showToast("Error: ${e.message}")
                }
            }
        }
    }

    private fun handleSuccessfulResponse(sportSpaces: List<SportSpace>?) {
        Log.d("SportSpaceFragment", "sportSpaces: $sportSpaces")
        if (sportSpaces != null && sportSpaces.isNotEmpty()) {
            adapter = SportSpaceAdapter(sportSpaces)
            recyclerView.adapter = adapter
        } else {
            adapter = SportSpaceAdapter(emptyList())
            recyclerView.adapter = adapter
            Log.i("SportSpaceFragment", "No hay espacios deportivos para mostrar aún.")
        }
    }

    private fun handleErrorResponse(responseCode: Int) {
        when (responseCode) {
            403 -> context?.showToast("Acceso denegado: no tienes permiso para acceder a esta funcionalidad")
            404 -> {
                Log.i("SportSpaceFragment", "No hay espacios deportivos aún")
            }
            else -> context?.showToast("Error al obtener los espacios deportivos")
        }
    }

    private fun fillUserProfile(): String {
        val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("role_type", null)
        return if (role == "PLAYER" || role == "OWNER") {
            role
        } else {
            throw IllegalStateException("Rol de usuario inválido o no encontrado")
        }
    }
}