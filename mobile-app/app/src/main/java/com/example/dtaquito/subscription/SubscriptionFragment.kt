package com.example.dtaquito.subscription

import Beans.suscription.Suscriptions
import Interface.PlaceHolder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R
import network.RetrofitClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SubscriptionFragment : Fragment() {

    private val service by lazy { RetrofitClient.instance.create(PlaceHolder::class.java) }
    private lateinit var currentSubscriptionView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_suscription, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentSubscriptionView = view.findViewById(R.id.currentSubscription)
        val subscriptions = createSubscriptionList()
        fetchCurrentSubscription(view, subscriptions)
    }

    private fun createSubscriptionList(): List<Suscriptions> {
        if(!isAdded) return emptyList()
        return listOf(
            Suscriptions(
                getString(R.string.bronze_plan_title),
                getString(R.string.bronze_plan_price),
                getString(R.string.bronze_plan_details),
                ContextCompat.getColor(requireContext(), R.color.bronze),
                "bronze"
            ),
            Suscriptions(
                getString(R.string.silver_plan_title),
                getString(R.string.silver_plan_price),
                getString(R.string.silver_plan_details),
                ContextCompat.getColor(requireContext(), R.color.silver),
                "silver"
            ),
            Suscriptions(
                getString(R.string.gold_plan_title),
                getString(R.string.gold_plan_price),
                getString(R.string.gold_plan_details),
                ContextCompat.getColor(requireContext(), R.color.gold),
                "gold"
            )
        )
    }

    private fun fetchCurrentSubscription(view: View, suscriptions: List<Suscriptions>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = service.getCurrentSubscription().execute()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        response.body()?.let { suscription ->
                            displayCurrentSubscription(suscription)
                            setupRecyclerView(view, suscriptions, suscription.planType)
                        }
                    } else {
                        showToast("No se pudo obtener la suscripción actual.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("SuscriptionFragment", "Error: ${e.message}")
                    showToast("Error al obtener la suscripción.")
                }
            }
        }
    }

    private fun setupRecyclerView(view: View, suscriptions: List<Suscriptions>, currentPlanType: String) {
        if(!isAdded) return
        val recyclerView: RecyclerView = view.findViewById(R.id.suscriptionPackages)
        val adapter = SubscriptionAdapter(suscriptions, currentPlanType.lowercase())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.setHasFixedSize(true)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        adapter.setOnItemClickListener { suscription -> upgradeSubscription(suscription) }
        val position = suscriptions.indexOfFirst { it.planType.equals(currentPlanType, ignoreCase = true) }
        if (position != -1) {
            recyclerView.scrollToPosition(position)
        }
    }

    private fun upgradeSubscription(subscription: Suscriptions) {
        service.upgradeSubscription(subscription.planType).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    response.body()?.string()?.let { extractApprovalUrl(it) }?.let { approvalUrl ->
                        startActivity(Intent(Intent.ACTION_VIEW, approvalUrl.toUri()))
                    } ?: showToast("No se encontró la URL de aprobación.")
                } else {
                    showToast("No se pudo actualizar la suscripción.")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("SuscriptionFragment", "Error: ${t.message}")
                showToast("Error al actualizar la suscripción: ${t.message}")
            }
        })
    }

    private fun extractApprovalUrl(responseBody: String): String? {
        return try {
            JSONObject(responseBody).getString("approval_url")
        } catch (e: Exception) {
            Log.e("SuscriptionFragment", "Error al extraer la URL de aprobación: ${e.message}")
            null
        }
    }

    private fun displayCurrentSubscription(subscription: Suscriptions) {
        if (!isAdded) return
        val planType = subscription.planType.uppercase()
        val planNameRes = when (planType) {
            "BRONZE" -> R.string.plan_bronze
            "SILVER" -> R.string.plan_silver
            "GOLD" -> R.string.plan_gold
            else -> null
        }
        val planName = planNameRes?.let { getString(it) } ?: planType
        val text = getString(R.string.current_subscription, planName)
        val spannable = SpannableString(text)

        val color = when (planType) {
            "BRONZE" -> ContextCompat.getColor(requireContext(), R.color.bronze)
            "SILVER" -> ContextCompat.getColor(requireContext(), R.color.silver)
            "GOLD" -> ContextCompat.getColor(requireContext(), R.color.gold)
            else -> "#FFFFFF".toColorInt()
        }

        val startIndex = text.indexOf(planName)
        val endIndex = startIndex + planName.length

        spannable.setSpan(
            ForegroundColorSpan(color),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        currentSubscriptionView.text = spannable
    }

    private fun showToast(message: String) {
        if(isAdded) Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}