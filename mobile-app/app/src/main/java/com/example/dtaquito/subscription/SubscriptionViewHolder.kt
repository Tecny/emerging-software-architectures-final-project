package com.example.dtaquito.subscription

import Beans.suscription.Suscriptions
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.dtaquito.R

class SubscriptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.findViewById(R.id.subscription_title)
    private val price: TextView = itemView.findViewById(R.id.subscription_price)
    private val details: TextView = itemView.findViewById(R.id.subscription_details)
    private val button: Button = itemView.findViewById(R.id.subscribe_button)

    private fun mapPlanType(planType: String): String {
        return when (planType.lowercase()) {
            "bronce" -> "bronze"
            "plata" -> "silver"
            "oro" -> "gold"
            else -> planType
        }
    }

    fun renderSubscription(subscription: Suscriptions, currentPlanType: String, onItemClickListener: ((Suscriptions) -> Unit)?) {
        title.text = subscription.title
        price.text = subscription.price
        details.text = subscription.details
        val cardView = itemView as androidx.cardview.widget.CardView
        cardView.setCardBackgroundColor(subscription.backgroundColor)

        val mappedCurrentPlanType = mapPlanType(currentPlanType)

        if (subscription.planType == mappedCurrentPlanType) {
            button.text = itemView.context.getString(R.string.current_plan)
            button.isEnabled = false
            button.visibility = View.VISIBLE
        } else {
            button.text = itemView.context.getString(R.string.update_plan)
            button.isEnabled = true
            button.setOnClickListener {
                onItemClickListener?.invoke(subscription)
            }

            button.visibility = when (mappedCurrentPlanType) {
                "silver" -> if (subscription.planType == "bronze") View.INVISIBLE else View.VISIBLE
                "gold" -> if (subscription.planType == "bronze" || subscription.planType == "silver") View.INVISIBLE else View.VISIBLE
                else -> View.VISIBLE
            }
        }
    }
}