package com.univapay.models


import com.univapay.sdk.models.response.subscription.FullSubscription

/**
 * Interface for Checkout Subscription Callback
 */
interface CheckoutSubscriptionCallback {
    fun onSuccess(subscription: FullSubscription?)
    fun onError(error: UnivapayError?)
    fun onUndetermined(subscription: FullSubscription?)
}
