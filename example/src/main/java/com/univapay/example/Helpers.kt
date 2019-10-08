package com.univapay.example

import android.content.Context
import android.preference.PreferenceManager
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.models.TransactionType
import com.univapay.payments.*
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.request.subscription.FixedCycleAmountInstallmentsPlan
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import com.univapay.sdk.models.request.subscription.RevolvingInstallmentsPlan
import com.univapay.sdk.types.InstallmentPlanType
import java.math.BigInteger

object Helpers {

    class CheckoutDemoSettings(
        val title: String,
        val description: String,
        val money: MoneyLike,
        val checkoutType: CheckoutType,
        val tokenType: TransactionType,
        val requireAddress: Boolean,
        val requireCvv: Boolean,
        val allowRememberCard: Boolean,
        val settings: PaymentSettings?
    )

    /**
     * Some example payments.
     */
    internal fun getSampleConfig(checkoutType: CheckoutType,
                                 transactionType: TransactionType,
                                 settings: PaymentSettings?): CheckoutConfiguration<*> {

        val isFullPayment = checkoutType == CheckoutType.PAYMENT

        when(settings){
            is ChargeSettings -> {
                when(transactionType){
                    TransactionType.ONE_TIME -> return OneTimeTokenCheckout(settings, isFullPayment)
                    TransactionType.RECURRING -> return RecurringTokenCheckout(settings, isFullPayment)
                    else -> throw Exception("Illegal state. Charge settings only available for OneTime and Recurring tokens.")
                }
            }
            is SubscriptionSettings -> return SubscriptionCheckout(settings, isFullPayment)
            else -> {
                when(transactionType){
                    TransactionType.ONE_TIME -> return OneTimeTokenCheckout()
                    TransactionType.SUBSCRIPTION -> return SubscriptionCheckout()
                    TransactionType.RECURRING -> return RecurringTokenCheckout()
                }
            }
        }
    }

    internal fun parsePreferences(context: Context): CheckoutDemoSettings{

        val preferences = PreferenceManager.getDefaultSharedPreferences(context)

        val title = preferences.getString("pref_title", context.resources.getString(R.string.pref_title_default))
        val description = preferences.getString("pref_description", context.resources.getString(R.string.pref_description_default))
        val amount = preferences.getString("pref_amount", "0").toBigInteger()
        val currency = preferences.getString("pref_currency", "").toUpperCase()
        val checkoutType = CheckoutType.valueOf(preferences.getString("pref_checkout_type", CheckoutType.PAYMENT.name).toUpperCase())
        val tokenType = TransactionType.valueOf(preferences.getString("pref_token_type", TransactionType.ONE_TIME.name).toUpperCase().replace("-", "_"))
        val requireAddress = preferences.getBoolean("pref_require_address", false)
        val requireCvv = preferences.getBoolean("pref_require_cvv", false)
        val allowRememberingCard = preferences.getBoolean("pref_allow_remembering_card", false)
        val defaultInstallmentsPlan = context.resources.getString(R.string.pref_installment_plan_type_none)
        val installmentPlanTypeStr = preferences.getString("pref_installment_plan_type", defaultInstallmentsPlan)
                .replace("(", "")
                .replace(")", "")
        var installmentPlanType: InstallmentPlanType? = null
        if (installmentPlanTypeStr != defaultInstallmentsPlan) {
            installmentPlanType = InstallmentPlanType.valueOf(installmentPlanTypeStr.toUpperCase().replace(" ","_"))
        }

        var settings: PaymentSettings? = null

        if(amount != BigInteger.valueOf(0) && !currency.isEmpty()){
            val money = MoneyLike(amount, currency)

            when(tokenType){
                TransactionType.SUBSCRIPTION -> {
                    when (installmentPlanType) {
                        InstallmentPlanType.REVOLVING -> {
                            settings = SubscriptionSettings(money, SubscriptionPeriod.Monthly)
                                    .withInitialAmount(null)
                                    .withInstallmentPlan(RevolvingInstallmentsPlan())
                                    .withStartOn(null)
                        }
                        InstallmentPlanType.FIXED_CYCLES -> {
                            settings = SubscriptionSettings(money, SubscriptionPeriod.Monthly)
                                    .withInitialAmount(BigInteger.valueOf(1000))
                                    .withInstallmentPlan(FixedCycleInstallmentsPlan(12))
                                    .withStartOn(null)
                        }
                        InstallmentPlanType.FIXED_CYCLE_AMOUNT -> {
                            settings = SubscriptionSettings(money, SubscriptionPeriod.Monthly)
                                    .withInitialAmount(BigInteger.valueOf(1000))
                                    .withInstallmentPlan(FixedCycleAmountInstallmentsPlan(BigInteger.valueOf(2000)))
                                    .withStartOn(null)
                        }
                        else -> {
                            settings = SubscriptionSettings(money, SubscriptionPeriod.Monthly)
                        }
                    }
                }
                TransactionType.ONE_TIME, TransactionType.RECURRING -> settings = ChargeSettings(money)
            }
        }

        return CheckoutDemoSettings(
                title,
                description,
                MoneyLike(amount, currency),
                checkoutType,
                tokenType,
                requireAddress,
                requireCvv,
                allowRememberingCard,
                settings
        )

    }
}
