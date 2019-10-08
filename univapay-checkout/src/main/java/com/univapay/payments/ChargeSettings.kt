package com.univapay.payments

import android.content.Context
import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import com.univapay.activities.CheckoutActivity
import com.univapay.models.CaptureSettings
import com.univapay.models.CheckoutArguments
import com.univapay.payments.CheckoutConfiguration.Companion.pollCardCharge
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.IdempotencyKey
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.utils.CallbackIntents
import com.univapay.utils.formatCurrency
import com.univapay.views.UnivapayViewModel
import java.math.BigInteger
import java.util.*

class ChargeSettings(
        money: MoneyLike,
        override val captureSettings: CaptureSettings?,
        override val descriptorSettings: DescriptorSettings?) : PaymentSettings(money), Descriptor, Capture{

    constructor(money: MoneyLike): this(money, null, null)

    constructor(money: MoneyLike, captureSettings: CaptureSettings): this(money, captureSettings, null)

    constructor(money: MoneyLike, descriptorSettings: DescriptorSettings): this(money, null, descriptorSettings)


    fun getCaptureSettings(paymentType: PaymentTypeName): CaptureSettings?{

        return(
                if(paymentType == PaymentTypeName.CARD)
                    captureSettings
                else
                    null
                )
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(money.amount.toLong())
        dest.writeString(money.currency)
        dest.writeParcelable(captureSettings, 0)
        dest.writeParcelable(descriptorSettings, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    private fun requestWithCVVRetry(transactionToken: TransactionTokenWithData, money: MoneyLike, context: Context, univapay: UnivapaySDK, arguments: CheckoutArguments){
        val captureSettings = getCaptureSettings(transactionToken.paymentTypeName)
        val descriptorSettings = descriptorSettings
        val builder = captureSettings?.let{
            univapay.createCharge(transactionToken.id, money, captureSettings.capture)
                    .withCaptureAt(captureSettings.captureAt)
        } ?: univapay.createCharge(transactionToken.id, money, null)

        descriptorSettings?.let{ds->
            builder.withDescriptor(
                    ds.descriptor,
                    ds.ignoreDescriptorOnError
            )
        }

        builder.withMetadata(arguments.metadata)
                .withIdempotencyKey(IdempotencyKey(UUID.randomUUID().toString()))
                .build()
                .dispatch(object : UnivapayCallback<Charge> {
                    override fun getResponse(charge: Charge) {
                        pollCardCharge(univapay, charge, context)
                    }
                    override fun getFailure(error: Throwable) {
                        CheckoutConfiguration.handleTransactionFailure(transactionToken, error, context, CallbackIntents.ChargeCallbackIntent.ChargeFailureCallback()) {
                            requestWithCVVRetry(transactionToken, money, context, univapay, arguments)
                        }
                    }
                })
    }

    override fun processWithToken(transactionToken: TransactionTokenWithData, univapay: UnivapaySDK, context: Context, arguments: CheckoutArguments) {

        if(UnivapayViewModel.instance.config.processFullPayment){
            val builder = captureSettings?.let{
                univapay.createCharge(transactionToken.id, money, captureSettings.capture)
                        .withCaptureAt(captureSettings.captureAt)
            } ?: univapay.createCharge(transactionToken.id, money, null)

            descriptorSettings?.let{ds->
                builder.withDescriptor(
                        ds.descriptor,
                        ds.ignoreDescriptorOnError
                )
            }

            builder.withMetadata(arguments.metadata)
                    .withIdempotencyKey(IdempotencyKey(UUID.randomUUID().toString()))
                    .build()
                    .dispatch(object : UnivapayCallback<Charge> {
                        override fun getResponse(charge: Charge) {
                            pollCardCharge(univapay, charge, context)
                        }
                        override fun getFailure(error: Throwable) {
                            CheckoutConfiguration.handleTransactionFailure(transactionToken, error, context, CallbackIntents.ChargeCallbackIntent.ChargeFailureCallback()) {
                                requestWithCVVRetry(transactionToken, money, context, univapay, arguments)
                            }
                        }
                    })
        } else {
            val activity = context as CheckoutActivity?
            activity?.broadcastFinalize()
            activity?.finish()
        }
    }

    override fun getDescription(resources: Resources): String {
        val formattedAmount = formatCurrency(money, resources)
        return resources.getString(
                com.univapay.R.string.checkout_payment_details_paragraph,
                formattedAmount
        )
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<ChargeSettings> = object : Parcelable.Creator<ChargeSettings> {
            override fun createFromParcel(parcel: Parcel): ChargeSettings {
                return readFromParcel(parcel)
            }

            override fun newArray(size: Int): Array<ChargeSettings?> {
                return arrayOfNulls(size)
            }
        }

        fun readFromParcel(parcel: Parcel): ChargeSettings {
            val amount = BigInteger.valueOf(parcel.readLong())
            val currency = parcel.readString()

            val captureSettings = parcel.readParcelable<CaptureSettings>(CaptureSettings::class.java.classLoader)
            val descriptorSettings = parcel.readParcelable<DescriptorSettings>(DescriptorSettings::class.java.classLoader)

            return ChargeSettings(
                    MoneyLike(
                            amount,
                            currency
                    ),
                    captureSettings,
                    descriptorSettings
            )
        }
    }
}
