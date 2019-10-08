package com.univapay.models

interface CheckoutInitializationCallback{
    fun onSuccess()
    fun onFailure(error: UnivapayError?)
}
