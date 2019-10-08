package com.univapay.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.response.PaymentsPlan
import com.univapay.utils.bindNotNullable
import com.univapay.utils.formatCurrency

class PaymentsPlanAdapter(val payments: PaymentsPlan,
                          val context: Context): RecyclerView.Adapter<PaymentsPlanAdapter.ViewHolder>() {

    class ViewHolder(val simulatedPaymentView: View): RecyclerView.ViewHolder(simulatedPaymentView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val simulatedPaymentView: View = LayoutInflater.from(parent.context).inflate(com.univapay.R.layout.simulated_payment_view, parent, false)
        return ViewHolder(simulatedPaymentView)
    }

    override fun getItemCount(): Int {
        return payments.items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val payment = payments.items[position]

        val paymentText = "${payment.dueDate} - ${formatCurrency(MoneyLike(payment.amount, payment.currency), context.resources)}"

        val paymentTextView: TextView = bindNotNullable(holder.simulatedPaymentView, com.univapay.R.id.simulated_payment)
        paymentTextView.text = paymentText
    }
}
