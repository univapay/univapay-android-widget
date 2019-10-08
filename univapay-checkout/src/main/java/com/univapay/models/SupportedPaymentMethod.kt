package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import com.univapay.sdk.models.response.store.CheckoutInfo
import com.univapay.sdk.types.PaymentTypeName

/**
 * Enumerates the payment methods that are supported by the UnivaPay Checkout.
 * Note that these are not the only ones supported by the platform, and that
 * some of these may actually be disabled at the Store level.
 */
enum class SupportedPaymentMethod(val paymentType: PaymentTypeName): Parcelable {
    CREDIT_CARD(PaymentTypeName.CARD),
    CONVENIENCE_STORE(PaymentTypeName.KONBINI);

    fun isSupportedByStore(checkoutInfo: CheckoutInfo): Boolean{
        return kotlin.run {
            when(this){
                CREDIT_CARD -> checkoutInfo.cardConfiguration.enabled
                CONVENIENCE_STORE -> checkoutInfo.konbiniConfiguration.enabled
            }
        }
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(paymentType.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        fun fromPaymentType(paymentType: PaymentTypeName): SupportedPaymentMethod{
            return kotlin.run {
                when(paymentType){
                    PaymentTypeName.CARD -> CREDIT_CARD
                    PaymentTypeName.KONBINI -> CONVENIENCE_STORE
                    else -> throw IllegalArgumentException("Payment type not supported")
                }
            }
        }

        @JvmField val CREATOR: Parcelable.Creator<SupportedPaymentMethod> = object : Parcelable.Creator<SupportedPaymentMethod> {
            override fun createFromParcel(parcel: Parcel): SupportedPaymentMethod {
                return SupportedPaymentMethod.fromPaymentType(
                        PaymentTypeName.valueOf(parcel.readString())
                )
            }

            override fun newArray(size: Int): Array<SupportedPaymentMethod?> {
                return arrayOfNulls(size)
            }
        }
    }
}
