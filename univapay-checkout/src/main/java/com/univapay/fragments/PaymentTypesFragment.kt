package com.univapay.fragments

import android.os.Bundle
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.univapay.activities.CheckoutActivity
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.payments.SubscriptionSettings
import com.univapay.sdk.types.InstallmentPlanType
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.utils.Constants
import com.univapay.views.ButtonAction
import com.univapay.views.UnivapayViewModel

class PaymentTypesFragment : UnivapayFragment.NewPaymentFragment(){

    override val FRAGMENT_TAG: String
        get() = this::class.java.name

    override fun display(checkout: CheckoutActivity, paymentType: PaymentTypeName?, addToBackstack: Boolean) {

        val bnd = checkout.intent.extras
        bnd.putParcelableArrayList(Constants.ARG_CUSTOMER_TOKENS, checkout.customerTokens)
        bnd.putParcelable(Constants.ARG_ADDRESS_DETAILS, checkout.mAddressDetails)
        arguments = bnd

        val fragmentManager = checkout.supportFragmentManager

        fragmentManager
                .beginTransaction()
                .replace(com.univapay.R.id.fragment_container, this, FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val checkout = activity as CheckoutActivity?

        val view = inflater.inflate(com.univapay.R.layout.fragment_payment_type, container, false)

        val tokens: ArrayList<TransactionTokenWithDataParcel>? =
                arguments?.getParcelableArrayList<TransactionTokenWithDataParcel>(Constants.ARG_CUSTOMER_TOKENS)

        val cardTokens = tokens?.filter { t->
            t.transactionTokenInfo.paymentTypeName == PaymentTypeName.CARD
        }?.let {t-> ArrayList(t) } ?: arrayListOf()

        val konbiniTokens = tokens?.filter { t->
            t.transactionTokenInfo.paymentTypeName == PaymentTypeName.KONBINI
        }?.let{t->ArrayList(t)} ?: arrayListOf()

        checkout?.let {

            checkout.findViewById<ButtonAction>(com.univapay.R.id.button_action)?.let { b->
                b.visibility = View.GONE
            }

//          Set listener for card payment button
            view.findViewById<CardView?>(com.univapay.R.id.card_payment_type)?.setOnClickListener{
                val go = kotlin.run{
                    val settings = UnivapayViewModel.instance.config.settings
                    if(settings is SubscriptionSettings
                            && settings.installmentPlan != null
                            && settings.installmentPlan?.planType != InstallmentPlanType.REVOLVING){
                        {SubscriptionDetailsFragment().display(checkout, PaymentTypeName.CARD)}
                    } else{
                        if(cardTokens.isEmpty()){
                            if (UnivapayViewModel.instance.args.isAddress) {
                                {AddressFragment().display(checkout)}
                            } else {
                                {CardDetailFragment().display(checkout)}
                            }
                        } else {
                            {StoredDataFragment()
                                    .display(checkout,
                                            cardTokens,
                                            PaymentTypeName.CARD,
                                            checkout.exceedsCardThreshold)}
                        }
                    }
                }
                go()
            }

//      Set listener for konbini payment button
            view.findViewById<CardView?>(com.univapay.R.id.konbini_payment_type)?.setOnClickListener{
                val go = kotlin.run{

                    val settings = UnivapayViewModel.instance.config.settings
                    if(settings is SubscriptionSettings
                            && settings.installmentPlan != null
                            && settings.installmentPlan?.planType != InstallmentPlanType.REVOLVING){
                        {SubscriptionDetailsFragment().display(checkout, PaymentTypeName.KONBINI)}
                    } else {
                        if(konbiniTokens.isEmpty()){
                            {KonbiniDetailsFragment().display(checkout)}
                        } else {
                            {StoredDataFragment().display(checkout, konbiniTokens, PaymentTypeName.KONBINI)}
                        }
                    }
                }
                go()
            }
        }

        return view
    }
}
