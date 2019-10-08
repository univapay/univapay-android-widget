package com.univapay.models

import com.univapay.activities.CheckoutActivity
import com.univapay.fragments.*
import com.univapay.payments.SubscriptionSettings
import com.univapay.sdk.models.response.store.CheckoutInfo
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.InstallmentPlanType
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.utils.FunctionalUtils.Either
import com.univapay.utils.FunctionalUtils.Either.Left
import com.univapay.utils.FunctionalUtils.Either.Right
import com.univapay.views.UnivapayViewModel

class CheckoutData(val checkoutInfo: CheckoutInfo,
                   val tokens: MutableList<TransactionTokenWithData>?,
                   val exceedsThreshold: Boolean
){

    constructor(checkoutInfo: CheckoutInfo): this(checkoutInfo, null, false)

    constructor(checkoutInfo: CheckoutInfo, tokens: MutableList<TransactionTokenWithData>): this(checkoutInfo, tokens, false)

    constructor(checkoutInfo: CheckoutInfo, exceedsThreshold: Boolean): this(checkoutInfo, null, exceedsThreshold)

    fun paymentMethods(): List<PaymentTypeName> {
        val args = UnivapayViewModel.instance.args

//      If a payment type has been specified, return a singleton list with that payment method.
        return args.paymentMethod?.let { paymentMethod ->
            if(paymentMethod.isSupportedByStore(checkoutInfo)) listOf(paymentMethod.paymentType) else emptyList()
        }
            ?: listOf(
            if(checkoutInfo.cardConfiguration.enabled) PaymentTypeName.CARD else null,
                if(checkoutInfo.konbiniConfiguration.enabled) PaymentTypeName.KONBINI else null
        ).filterNotNull()
    }

    fun getInitializerForPaymentMethod(checkout: CheckoutActivity, paymentTypeName: PaymentTypeName): Either<UnivapayError, ()-> Unit> {
        val initializer: Either<UnivapayError, ()->Unit>

        val settings = UnivapayViewModel.instance.config.settings

        if(settings is SubscriptionSettings &&
                settings.installmentPlan?.let {ip-> ip.planType != InstallmentPlanType.REVOLVING  }?: false
        ) {
            initializer = Right({SubscriptionDetailsFragment().display(checkout, paymentTypeName, false)})
        } else {
            when(paymentTypeName){
                PaymentTypeName.CARD-> {
                    val cardTokens = tokens?.filter { t-> t.paymentTypeName == PaymentTypeName.CARD }
                    if(cardTokens == null || cardTokens.isEmpty()){
                        if (UnivapayViewModel.instance.args.isAddress) {
                            initializer = Right({AddressFragment().display(checkout, false)})
                        } else {
                            initializer = Right({CardDetailFragment().display(checkout, false)})
                        }
                    } else {
                        initializer = Right(
                                {StoredDataFragment()
                                        .display(checkout, ArrayList(cardTokens.map { t-> TransactionTokenWithDataParcel(t) }), PaymentTypeName.CARD, exceedsThreshold, addToBackstack = false)}
                        )
                    }
                }
                PaymentTypeName.KONBINI-> {
                    val konbiniTokens = tokens?.filter { t-> t.paymentTypeName == PaymentTypeName.KONBINI }
                    if(konbiniTokens == null || konbiniTokens.isEmpty()){
                        initializer = Right({KonbiniDetailsFragment().display(checkout, false)})
                    } else {
                        initializer = Right(
                                {StoredDataFragment()
                                        .display(checkout, ArrayList(konbiniTokens.map{t-> TransactionTokenWithDataParcel(t)}), PaymentTypeName.KONBINI, null, addToBackstack = false)}
                        )
                    }
                }
                else -> initializer = Left(UnivapayError("Payment type not supported"))
            }
        }
        return initializer
    }
}
