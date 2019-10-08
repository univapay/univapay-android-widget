package com.univapay.payments

/**
 * Checkout Type: whether the transaction is token-only or full-payment
 */
enum class CheckoutType {
    TOKEN, PAYMENT;

    fun isPayment(): Boolean {
        return this == CheckoutType.PAYMENT
    }

}
