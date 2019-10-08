package com.univapay.models


import com.univapay.sdk.models.response.charge.Charge

/**
 * Interface for Checkout Charge Callback
 */
interface CheckoutChargeCallback {
    fun onSuccess(charge: Charge?)
    fun onError(error: UnivapayError?)
    fun onUndetermined(charge: Charge?)
}
