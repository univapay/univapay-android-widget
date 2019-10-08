package com.univapay.models


import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData

/**
 * Interface for Token Callback
 */
interface CheckoutTokenCallback {
    fun onSuccess(transactionTokenWithData: TransactionTokenWithData?)
    fun onError(error: UnivapayError?)
}
