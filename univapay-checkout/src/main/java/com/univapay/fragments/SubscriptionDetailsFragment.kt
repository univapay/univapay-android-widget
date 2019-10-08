package com.univapay.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.univapay.activities.CheckoutActivity
import com.univapay.adapters.PaymentsPlanAdapter
import com.univapay.models.CheckoutData
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.payments.SubscriptionSettings
import com.univapay.sdk.models.response.PaymentsPlan
import com.univapay.sdk.types.InstallmentPlanType
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.utils.Constants.ARG_PAYMENT_TYPE
import com.univapay.utils.FunctionalUtils
import com.univapay.utils.bindNotNullable
import com.univapay.views.ActionButtonStatus
import com.univapay.views.UnivapayViewModel

class SubscriptionDetailsFragment : UnivapayFragment.NewPaymentFragment() {

    override val FRAGMENT_TAG: String
        get() = this::class.java.name

    override fun display(checkout: CheckoutActivity, paymentType: PaymentTypeName?, addToBackstack: Boolean) {
        val bnd = checkout.intent.extras
        arguments = bnd

        paymentType?.let {
            bnd.putString(ARG_PAYMENT_TYPE, paymentType.name)
        }

        val transaction = checkout.supportFragmentManager
                .beginTransaction()
                .replace(com.univapay.R.id.fragment_container, this, tag)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

                if(addToBackstack){
                    transaction.addToBackStack(null)
                }

                transaction.commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val checkout = activity as CheckoutActivity?

        // Inflate the layout for this fragment
        val view = inflater.inflate(com.univapay.R.layout.fragment_subscription_details, container, false)

        val config = UnivapayViewModel.instance.config

        if(config.settings is SubscriptionSettings && config.settings.installmentPlan != null){

            //      TODO: handle this type of errors gracefully
            val paymentType: PaymentTypeName = PaymentTypeName.valueOf(arguments?.getString(ARG_PAYMENT_TYPE)!!)

            val message = view.findViewById<TextView>(com.univapay.R.id.simulated_payments_messages)

            if(config.settings.installmentPlan?.planType == InstallmentPlanType.REVOLVING){
                message.text = resources.getString(com.univapay.R.string.subscription_details_revolving)
                message.height = resources.getDimension(com.univapay.R.dimen.checkout_simulated_payment_message_height).toInt()
            } else {
                config.settings.apply {
                    UnivapayViewModel
                            .instance
                            .getPaymentsPlan(paymentType)
                            .observe(this@SubscriptionDetailsFragment, Observer{data: FunctionalUtils.Either<Throwable, PaymentsPlan>? ->

                                when(data){
                                    is FunctionalUtils.Either.Right<PaymentsPlan> -> {
                                        context?.let {c->
                                            val paymentsPlan = data.value
                                            val paymentsPlanAdapter = PaymentsPlanAdapter(
                                                    paymentsPlan,
                                                    c
                                            )

                                            val recyclerView: RecyclerView = bindNotNullable(view, com.univapay.R.id.simulated_payments_list)
                                            recyclerView.apply {
                                                setHasFixedSize(true)
                                                layoutManager = LinearLayoutManager(view.context)
                                                adapter = paymentsPlanAdapter
                                            }
                                        }

                                    }
                                    is FunctionalUtils.Either.Left<Throwable> -> {
                                        message.text = resources.getString(com.univapay.R.string.subscription_payment_schedule_error)
                                        message.height = resources.getDimension(com.univapay.R.dimen.checkout_simulated_payment_message_height).toInt()
                                    }
                                }
                    })
                }
            }
        }

        UnivapayFragment
                .setupPaymentDetailsBar(checkout,
                        ActionButtonStatus.Next
                ){
                    checkout?.apply {
                        val viewModel = UnivapayViewModel.instance

                        val paymentType = PaymentTypeName.valueOf(arguments?.getString(ARG_PAYMENT_TYPE)!!)


                        viewModel.checkoutData.observe(this, Observer{ data: FunctionalUtils.Either<Throwable, CheckoutData>? ->

                            val checkoutData = data as FunctionalUtils.Either.Right<CheckoutData>

                            checkoutData.value.apply {


                                if (paymentType == PaymentTypeName.CARD) {
                                    val cardTokens = tokens?.filter { t -> t.paymentTypeName == PaymentTypeName.CARD }

                                    if (cardTokens == null || cardTokens.isEmpty()) {
                                        if (viewModel.args.isAddress) {
                                            AddressFragment().display(checkout)
                                        } else {
                                            CardDetailFragment().display(checkout)
                                        }
                                    } else {
                                        val tokenParcel = ArrayList(cardTokens.map { t -> TransactionTokenWithDataParcel(t) })
                                        StoredDataFragment().display(checkout, tokenParcel, PaymentTypeName.CARD, exceedsThreshold)
                                    }
                                } else {

                                    val konbiniTokens = tokens?.filter { t -> t.paymentTypeName == PaymentTypeName.KONBINI }
                                    if (konbiniTokens == null || konbiniTokens.isEmpty()) {
                                        KonbiniDetailsFragment().display(checkout)
                                    } else {
                                        val tokenParcel = ArrayList(konbiniTokens.map { t -> TransactionTokenWithDataParcel(t) })
                                        StoredDataFragment().display(checkout, tokenParcel, PaymentTypeName.KONBINI, exceedsThreshold)
                                    }
                                }
                            }
                        })
                    }
                }

        return view
    }
}
