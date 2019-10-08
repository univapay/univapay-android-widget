package com.univapay.payments

import android.content.Context

import com.univapay.models.CheckoutArguments
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData

interface CheckoutCallback {

    fun onSuccess(transactionToken: TransactionTokenWithData, context: Context, arguments: CheckoutArguments)
}
