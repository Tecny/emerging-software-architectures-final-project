package com.example.dtaquito.tickets

import Interface.PlaceHolder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import network.RetrofitClient

class TicketFragment : Fragment() {

    private val service = RetrofitClient.instance.create(PlaceHolder::class.java)
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TicketAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_ticket, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val createTicketButton = view.findViewById<Button>(R.id.create_ticket_btn)
        createTicketButton.setOnClickListener {

            val sharedPreferences = requireContext().getSharedPreferences("user_prefs", android.content.Context.MODE_PRIVATE)
            val userCredits = sharedPreferences.getString("credits", "0.0")?.toDouble()
            Log.d("PasoDeCreditos", "TicketFragment - Créditos recibidos: $userCredits")
            val fragment = CreateTicketFragment()
            val args = Bundle()
            args.putString("credits", userCredits.toString())
            fragment.arguments = args
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        fetchTickets()
    }

    private fun fetchTickets() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tickets = service.getBankTransfers()
                val sortedTickets = tickets.sortedByDescending { it.createdAt }
                withContext(Dispatchers.Main) {
                    adapter = TicketAdapter(sortedTickets)
                    recyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("TicketFragment", "Error al obtener tickets: ${e.message}", e)
            }
        }
    }
}