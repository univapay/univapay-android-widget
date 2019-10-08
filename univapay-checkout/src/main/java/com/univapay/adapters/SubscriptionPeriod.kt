package com.univapay.adapters

import android.support.annotation.StringRes
import com.univapay.UnivapayApplication

/**
 * Subscription Period
 */
enum class SubscriptionPeriod constructor(val value: String, @param:StringRes private val mResId: Int, private val mSubscriptionPeriod: com.univapay.sdk.types.SubscriptionPeriod, @param:StringRes private val mUnitId: Int) {
    Daily("1d", com.univapay.R.string.checkout_subscription_1d, com.univapay.sdk.types.SubscriptionPeriod.DAILY, com.univapay.R.string.checkout_subscription_unit_1d),
    Weekly("1w", com.univapay.R.string.checkout_subscription_1w, com.univapay.sdk.types.SubscriptionPeriod.WEEKLY, com.univapay.R.string.checkout_subscription_unit_1w),
    Biweekly("2w", com.univapay.R.string.checkout_subscription_2w, com.univapay.sdk.types.SubscriptionPeriod.BIWEEKLY, com.univapay.R.string.checkout_subscription_unit_2w),
    Monthly("1m", com.univapay.R.string.checkout_subscription_1m, com.univapay.sdk.types.SubscriptionPeriod.MONTHLY, com.univapay.R.string.checkout_subscription_unit_1m),
    Quarterly("3m", com.univapay.R.string.checkout_subscription_3m, com.univapay.sdk.types.SubscriptionPeriod.QUARTERLY, com.univapay.R.string.checkout_subscription_unit_3m),
    Semiannually("6m", com.univapay.R.string.checkout_subscription_6m, com.univapay.sdk.types.SubscriptionPeriod.SEMIANNUALLY, com.univapay.R.string.checkout_subscription_unit_6m),
    Annually("1y", com.univapay.R.string.checkout_subscription_1y, com.univapay.sdk.types.SubscriptionPeriod.ANNUALLY, com.univapay.R.string.checkout_subscription_unit_1y);

    val localizedString: String
        get() {
            val resources = UnivapayApplication.context.resources
            return resources.getString(mResId)
        }

    val localizedStringFixedCycles: String
        get() {
            val resources = UnivapayApplication.context.resources
            return "(" + resources.getString(mResId) + ")"
        }

    val localizedStringUnit: String
        get() {
            val resources = UnivapayApplication.context.resources
            return resources.getString(mUnitId)
        }

    fun toSubscriptionPeriod(): com.univapay.sdk.types.SubscriptionPeriod {
        return this.mSubscriptionPeriod
    }
}
