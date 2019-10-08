package com.univapay.payments

import android.content.Context
import com.univapay.models.CheckoutArguments
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.request.transactiontoken.PaymentData

interface Checkout {

    fun process(email: String, paymentData: PaymentData, univapay: UnivapaySDK, context: Context, arguments: CheckoutArguments, rememberCard: Boolean)

}
