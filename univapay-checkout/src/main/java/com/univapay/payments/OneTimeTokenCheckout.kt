package com.univapay.payments

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.univapay.models.CheckoutArguments
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.CreditCard
import com.univapay.sdk.models.request.transactiontoken.PaymentData
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.TransactionTokenType
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.utils.CallbackIntents.TokenCallbackIntent

class OneTimeTokenCheckout : CheckoutConfiguration<ChargeSettings> {

    constructor(): super()

    constructor(settings: ChargeSettings): super(settings)

    constructor(settings: ChargeSettings, processFullPayment: Boolean): super(settings, processFullPayment)

    override val transactionTokenType: TransactionTokenType
        get() = TransactionTokenType.ONE_TIME

    override fun process(email: String, paymentData: PaymentData, univapay: UnivapaySDK, context: Context, arguments: CheckoutArguments, rememberCard: Boolean) {
        if(rememberCard){
            this.asRecurringTokenCheckout().process(email, paymentData, univapay, context, arguments, rememberCard)
        } else {
            fun createWithCVVRetry(paymentData: PaymentData){
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
                                handleTransactionTokenFailure(paymentData, error, context){creditCard: CreditCard ->
                                    createWithCVVRetry(creditCard)
                                }
                            }
                        })
            }
            createWithCVVRetry(paymentData)
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
        @JvmField val CREATOR: Parcelable.Creator<OneTimeTokenCheckout> = object: Parcelable.Creator<OneTimeTokenCheckout>{
            override fun createFromParcel(parcel: Parcel): OneTimeTokenCheckout {
                val settings = parcel.readParcelable<ChargeSettings?>(ChargeSettings::class.java.classLoader)
                val processFullPayment = parcel.readByte().toInt() == 1

                if(settings != null){
                    return OneTimeTokenCheckout(settings, processFullPayment)
                } else return OneTimeTokenCheckout()
            }

            override fun newArray(i: Int): Array<OneTimeTokenCheckout?> {
                return arrayOfNulls(0)
            }
        }
    }

}
