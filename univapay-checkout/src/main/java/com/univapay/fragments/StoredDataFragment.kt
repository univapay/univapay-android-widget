package com.univapay.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.univapay.activities.CheckoutActivity
import com.univapay.adapters.CreditCardsAdapter
import com.univapay.adapters.CustomRecyclerViewAdapter
import com.univapay.adapters.StoredKonbinisAdapter
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.utils.Constants
import com.univapay.utils.Constants.ARG_CUSTOMER_TOKENS
import com.univapay.utils.Constants.ARG_EXCEEDS_THRESHOLD
import com.univapay.utils.Constants.ARG_PAYMENT_TYPE
import com.univapay.views.ActionButtonStatus
import com.univapay.views.UnivapayViewModel

class StoredDataFragment: UnivapayFragment.TokenPaymentFragment() {

    override val FRAGMENT_TAG: String
        get() = this::class.java.name

    override fun display(checkout: CheckoutActivity,
                         tokens: ArrayList<TransactionTokenWithDataParcel>,
                         paymentType: PaymentTypeName,
                         exceedsThreshold: Boolean?,
                         addToBackstack: Boolean) {

        val bnd = checkout.intent.extras
        bnd.putParcelableArrayList(Constants.ARG_CUSTOMER_TOKENS, tokens)
        exceedsThreshold?.let {e->
            bnd.putBoolean(Constants.ARG_EXCEEDS_THRESHOLD, e)
        }
        bnd.putString(Constants.ARG_PAYMENT_TYPE, paymentType.name)
        arguments = bnd

        commitTransaction(checkout, addToBackstack)
    }

    fun display(activity: CheckoutActivity, tokens: ArrayList<TransactionTokenWithDataParcel>, paymentType: PaymentTypeName) {
        display(activity, tokens, paymentType, null)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(com.univapay.R.layout.stored_data_select, container, false)

        val checkout = activity as CheckoutActivity?

        checkout?.apply {

            val paymentType = arguments?.getString(ARG_PAYMENT_TYPE)?.let {
                PaymentTypeName.valueOf(it)
            }

            val tokens = arguments
                    ?.getParcelableArrayList<TransactionTokenWithDataParcel>(ARG_CUSTOMER_TOKENS)
                    ?.map { t-> t.transactionTokenInfo }?.toMutableList()

            if(tokens != null && paymentType != null) {
                val exceedsThreshold = arguments?.getBoolean(ARG_EXCEEDS_THRESHOLD)

                val viewAdapter = setupTokensView(checkout, view, paymentType, tokens, exceedsThreshold)

                val recyclerView: RecyclerView = view.findViewById(com.univapay.R.id.stored_data_recycler_view)

                recyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(view.context)
                    adapter = viewAdapter
                }
            }
        }
        return view
    }

    private fun setupTokensView(checkout: CheckoutActivity,
                                view: View,
                                paymentType: PaymentTypeName,
                                tokens: MutableList<TransactionTokenWithData>,
                                exceedsThreshold: Boolean?
    ): CustomRecyclerViewAdapter<*> {

        val viewAdapter: CustomRecyclerViewAdapter<*>

        if(paymentType == PaymentTypeName.CARD){
            UnivapayFragment
                    .setupPaymentDetailsBar(checkout,
                            ActionButtonStatus.Add
                    ){
                        if(UnivapayViewModel.instance.args.isAddress) {
                            AddressFragment().display(checkout)
                        } else
                            CardDetailFragment().display(checkout)
                    }
            viewAdapter = CreditCardsAdapter(tokens, view.context, exceedsThreshold = exceedsThreshold?: false)
        } else if(paymentType == PaymentTypeName.KONBINI) {
            UnivapayFragment
                    .setupPaymentDetailsBar(checkout,
                            ActionButtonStatus.Add
                    ){
                        KonbiniDetailsFragment().display(checkout)
                    }
            viewAdapter = StoredKonbinisAdapter(tokens, view.context)
        } else throw Exception("Payment type not supported")

        return viewAdapter
        }
    }
