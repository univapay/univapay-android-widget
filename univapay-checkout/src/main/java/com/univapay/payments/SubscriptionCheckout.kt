package com.univapay.payments

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.univapay.models.CheckoutArguments
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.request.transactiontoken.PaymentData
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.TransactionTokenType
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.utils.CallbackIntents.TokenCallbackIntent
import com.univapay.utils.ErrorParser

class SubscriptionCheckout: CheckoutConfiguration<SubscriptionSettings> {

    constructor(): super()

    constructor(settings: SubscriptionSettings): super(settings)

    constructor(settings: SubscriptionSettings, processFullPayment: Boolean): super(settings, processFullPayment)

    override val transactionTokenType: TransactionTokenType
        get() = TransactionTokenType.SUBSCRIPTION

    override fun process(email: String, paymentData: PaymentData, univapay: UnivapaySDK, context: Context, arguments: CheckoutArguments, rememberCard: Boolean) {
        if(rememberCard){
            this.asRecurringTokenCheckout().process(email, paymentData, univapay, context, arguments, rememberCard)
        } else {
            getTokenBuilder(univapay, email, paymentData, transactionTokenType, arguments.metadata)
                    .build()
                    .dispatch(object : UnivapayCallback<TransactionTokenWithData> {
                        override fun getResponse(response: TransactionTokenWithData) {
                            broadcastSuccess(TokenCallbackIntent.TokenSuccessCallback(), TransactionTokenWithDataParcel(response), context)
                            if(processFullPayment && settings?.money != null){
                                settings.processWithToken(response, univapay, context, arguments)
                            }
                        }

                        override fun getFailure(error: Throwable) {
                            doFailed(TokenCallbackIntent.TokenFailureCallback(), null, ErrorParser.parseError(error), context)
                        }
                    })
        }
    }

    private fun asRecurringTokenCheckout(): RecurringTokenCheckout{
        if(settings != null){
            return RecurringTokenCheckout(settings, processFullPayment)
        } else return RecurringTokenCheckout()
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeParcelable(settings, 0)
        parcel.writeByte(if(processFullPayment) 1 else 0)
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<SubscriptionCheckout> = object: Parcelable.Creator<SubscriptionCheckout>{
            override fun createFromParcel(parcel: Parcel): SubscriptionCheckout {

                val settings = parcel.readParcelable<SubscriptionSettings?>(SubscriptionSettings::class.java.classLoader)
                val processFullPayment = parcel.readByte().toInt() == 1

                if(settings != null){
                    return SubscriptionCheckout(settings, processFullPayment)
                } else return SubscriptionCheckout()
            }

            override fun newArray(i: Int): Array<SubscriptionCheckout?> {
                return arrayOfNulls(0)
            }
        }
    }
}
