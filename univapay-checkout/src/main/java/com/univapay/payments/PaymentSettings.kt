package com.univapay.payments

import android.content.Context
import android.content.res.Resources
import android.os.Parcelable
import com.univapay.models.CheckoutArguments
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import org.joda.money.CurrencyUnit

abstract class PaymentSettings : Parcelable{

    val money: MoneyLike

    constructor(money: MoneyLike){
        if(money.amount.signum() == -1) throw IllegalArgumentException("Amount can't be negative")
        CurrencyUnit.of(money.currency)
        this.money = money
    }

    abstract fun processWithToken(transactionToken: TransactionTokenWithData, univapay: UnivapaySDK, context: Context, arguments: CheckoutArguments)

    abstract fun getDescription(resources: Resources): String

}
