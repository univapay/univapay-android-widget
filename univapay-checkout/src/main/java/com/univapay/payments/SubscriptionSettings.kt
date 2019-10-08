package com.univapay.payments

import android.content.Context
import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import com.univapay.activities.CheckoutActivity
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.models.CheckoutArguments
import com.univapay.models.InstallmentPlan
import com.univapay.payments.CheckoutConfiguration.Companion.pollCardSubscription
import com.univapay.payments.CheckoutConfiguration.Companion.pollKonbiniSubscription
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.IdempotencyKey
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.request.subscription.InstallmentPlanRequest
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.InstallmentPlanType
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.utils.CallbackIntents
import com.univapay.utils.ErrorParser
import com.univapay.utils.formatCurrency
import com.univapay.views.UnivapayViewModel
import org.joda.time.LocalDate
import org.threeten.bp.ZoneId
import java.math.BigInteger
import java.util.*

class SubscriptionSettings(money: MoneyLike, val period: SubscriptionPeriod) : PaymentSettings(money), Descriptor {

    var installmentPlan: InstallmentPlan? = null
    var initialAmount: BigInteger? = null
    var startOn: LocalDate? = null
    var zoneId: ZoneId? = null
    var preserveEndOfMonth: Boolean? = null
    override var descriptorSettings: DescriptorSettings? = null

    fun withInstallmentPlan(installmentPlan: InstallmentPlanRequest?): SubscriptionSettings {
        this.installmentPlan = InstallmentPlan.fromRequest(installmentPlan)
        return this
    }

    fun withInitialAmount(initialAmount: BigInteger?): SubscriptionSettings {
        this.initialAmount = initialAmount
        return this
    }

    fun withStartOn(startOn: LocalDate?): SubscriptionSettings {
        this.startOn = startOn
        return this
    }

    fun withZoneId(zoneId: ZoneId): SubscriptionSettings{
        this.zoneId = zoneId
        return this
    }

    fun withPreserveEndOfMonth(preserveEndOfMonth: Boolean): SubscriptionSettings{
        this.preserveEndOfMonth = preserveEndOfMonth
        return this
    }

    fun withDescriptor(descriptor: String): SubscriptionSettings{
        this.descriptorSettings = DescriptorSettings(descriptor)
        return this
    }

    fun withDescriptor(descriptor: String, ignoreOnError: Boolean): SubscriptionSettings{
        this.descriptorSettings = DescriptorSettings(descriptor, ignoreOnError)
        return this
    }

    override fun getDescription(resources: Resources): String{

        val initialAmountText = initialAmount?.let {i->
            val initialAmountMoney = MoneyLike(
                    i,
                    money.currency
            )
            resources.getString(
                    com.univapay.R.string.subscription_details_initial_amount_paragraph,
                    formatCurrency(initialAmountMoney, resources)
            )
        }?:""

        return kotlin.run {

            val formattedAmount = formatCurrency(money, resources)

            installmentPlan?.let {plan->
                when(plan.planType){
                    InstallmentPlanType.FIXED_CYCLE_AMOUNT -> {
                        val fixedCycleAmountMoney = MoneyLike(
                                plan.fixedCycleAmount,
                                money.currency
                        )
                        resources
                                .getString(
                                        com.univapay.R.string.subscription_fixed_cycles_amount_paragraph,
                                        formattedAmount,
                                        formatCurrency(fixedCycleAmountMoney, resources),
                                        period.localizedString,
                                        initialAmountText
                                )
                    }
                    InstallmentPlanType.FIXED_CYCLES -> {
                        resources
                                .getString(
                                        com.univapay.R.string.subscription_fixed_cycles_paragraph,
                                        formattedAmount,
                                        plan.fixedCycles,
                                        period.localizedString,
                                        initialAmountText
                                )

                    }
                    else -> {
                        resources.getString(
                                com.univapay.R.string.subscription_revolving_paragraph,
                                formattedAmount,
                                period.localizedString,
                                initialAmountText
                        )
                    }
                }
            }?: resources.getString(
                    com.univapay.R.string.subscription_periodic_paragraph,
                    formattedAmount,
                    period.localizedString,
                    initialAmountText
            )
        }
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeLong(money.amount.toLong())
        parcel.writeString(money.currency)
        parcel.writeString(period.toString())
        parcel.writeValue(installmentPlan)

        initialAmount?.let {value->
            parcel.writeInt(value.toInt())
        } ?: parcel.writeInt(-1)

        startOn?.let {value->
            parcel.writeLong(value.toDate().time)
        } ?: parcel.writeLong(-1)

        parcel.writeString(zoneId?.id)

        preserveEndOfMonth?.let {value->
            parcel.writeInt(if(value) 1 else 0)
        } ?: parcel.writeInt(-1)

        parcel.writeParcelable(descriptorSettings, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun processWithToken(transactionToken: TransactionTokenWithData, univapay: UnivapaySDK, context: Context, arguments: CheckoutArguments) {
        if(UnivapayViewModel.instance.config.processFullPayment){
            val builder = univapay.createSubscription(transactionToken.id, money, com.univapay.sdk.types.SubscriptionPeriod.valueOf(period.name.toUpperCase()))
                    .withInstallmentPlan(installmentPlan?.toRequest())
                    .withInitialAmount(initialAmount)
                    .withStartOn(startOn)
                    .withZoneId(zoneId)
                    .withPreserveEndOfMoth(preserveEndOfMonth)
                    .withIdempotencyKey(IdempotencyKey(UUID.randomUUID().toString()))
                    .withMetadata(arguments.metadata)

            descriptorSettings?.let{ds->
                builder.withDescriptor(
                        ds.descriptor,
                        ds.ignoreDescriptorOnError
                )
            }

            builder
                    .build()
                    .dispatch(object : UnivapayCallback<FullSubscription> {
                        override fun getResponse(subscription: FullSubscription) {
                            if(transactionToken.paymentTypeName == PaymentTypeName.KONBINI)
                                pollKonbiniSubscription(univapay, subscription, context)
                            else
                                pollCardSubscription(univapay, subscription, context)
                        }
                        override fun getFailure(error: Throwable) {
                            CheckoutConfiguration.doFailed(CallbackIntents.SubscriptionCallbackIntent.SubscriptionFailureCallback(), null, ErrorParser.parseError(error), context)
                        }
                    })
        } else {
            val activity = context as CheckoutActivity?
            activity?.broadcastFinalize()
            activity?.finish()
        }

    }

    companion object CREATOR : Parcelable.Creator<SubscriptionSettings> {

        override fun createFromParcel(parcel: Parcel): SubscriptionSettings{
            val amount = BigInteger.valueOf(parcel.readLong())
            val currency = parcel.readString()
            val period = SubscriptionPeriod.valueOf(parcel.readString())

            val settings = SubscriptionSettings(
                    MoneyLike(amount, currency),
                    period
            )
            val installmentPlan = parcel.readValue(InstallmentPlan::class.java.classLoader) as InstallmentPlan?
            val initialAmountInt = parcel.readInt()
            val initialAmount = if ( initialAmountInt == -1) {
                null
            } else {
                initialAmountInt.toBigInteger()
            }

            val startOnLong = parcel.readLong()
            val startOn = if (startOnLong == -1L) {
                null
            } else {
                LocalDate.fromDateFields(Date(startOnLong))
            }

            val zoneId = parcel.readString()
            val preserveEndOfMonth = (parcel.readByte().toInt() == 1)
            val descriptorSettings = parcel.readParcelable<DescriptorSettings>(DescriptorSettings::class.java.classLoader)

            settings
                    .withInitialAmount(initialAmount)
                    .withInstallmentPlan(installmentPlan?.toRequest())
                    .withStartOn(startOn)
                    .withPreserveEndOfMonth(preserveEndOfMonth)

            descriptorSettings?.let {
                settings
                        .withDescriptor(descriptorSettings.descriptor, descriptorSettings.ignoreDescriptorOnError)
            }

            zoneId?.let {
                settings.withZoneId(ZoneId.of(zoneId))
            }

            return settings
        }

        override fun newArray(size: Int): Array<SubscriptionSettings?> {
            return arrayOfNulls(size)
        }
    }

}
