package com.univapay.example.models


import com.univapay.example.R

enum class TransactionType(private val resourceId: Int) {
    ONE_TIME(R.string.pref_token_type_one_time),
    RECURRING(R.string.pref_token_type_recurring),
    SUBSCRIPTION(R.string.pref_token_type_subscription);

    fun isCharge(): Boolean{
        return (this == ONE_TIME || this == RECURRING)
    }

    fun isSubscription(): Boolean{
        return this == SUBSCRIPTION
    }

}
