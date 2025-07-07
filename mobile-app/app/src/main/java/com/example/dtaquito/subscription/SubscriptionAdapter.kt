package com.example.dtaquito.subscription

import Beans.suscription.Suscriptions
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class SubscriptionAdapter(
    private val subscriptions: List<Suscriptions>,
    private val currentPlanType: String
) : RecyclerView.Adapter<SubscriptionViewHolder>() {

    private var onItemClickListener: ((Suscriptions) -> Unit)? = null

    fun setOnItemClickListener(listener: (Suscriptions) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubscriptionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suscription, parent, false)
        return SubscriptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubscriptionViewHolder, position: Int) {
        holder.renderSubscription(subscriptions[position], currentPlanType, onItemClickListener)
    }

    override fun getItemCount() = subscriptions.size
}