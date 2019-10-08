package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import com.univapay.sdk.models.request.subscription.FixedCycleAmountInstallmentsPlan
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import com.univapay.sdk.models.request.subscription.InstallmentPlanRequest
import com.univapay.sdk.models.request.subscription.RevolvingInstallmentsPlan
import com.univapay.sdk.types.InstallmentPlanType
import java.math.BigInteger

class InstallmentPlan : Parcelable {

    var planType: InstallmentPlanType? = null
    var fixedCycles: Int? = null
    var fixedCycleAmount: BigInteger? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(planType!!.name)
        if (fixedCycles == null) {
            parcel.writeInt(0)
        } else {
            parcel.writeInt(fixedCycles!!)
        }
        parcel.writeValue(fixedCycleAmount)
    }

    constructor(planType: InstallmentPlanType, fixedCycles: Int?, fixedCycleAmount: BigInteger?) {
        this.planType = planType
        this.fixedCycles = fixedCycles
        this.fixedCycleAmount = fixedCycleAmount
    }

    fun toRequest(): InstallmentPlanRequest? {
        return when (planType) {
            InstallmentPlanType.REVOLVING -> RevolvingInstallmentsPlan()
            InstallmentPlanType.FIXED_CYCLES -> FixedCycleInstallmentsPlan(fixedCycles)
            InstallmentPlanType.FIXED_CYCLE_AMOUNT -> FixedCycleAmountInstallmentsPlan(fixedCycleAmount)
            else -> null
        }
    }

    private constructor(revolvingInstallmentsPlan: RevolvingInstallmentsPlan) {
        this.planType = revolvingInstallmentsPlan.planType
    }

    private constructor(fixedCycleInstallmentsPlan: FixedCycleInstallmentsPlan) {
        this.planType = fixedCycleInstallmentsPlan.planType
        this.fixedCycles = fixedCycleInstallmentsPlan.fixedCycles
    }

    private constructor(fixedCycleAmountInstallmentsPlan: FixedCycleAmountInstallmentsPlan) {
        this.planType = fixedCycleAmountInstallmentsPlan.planType
        this.fixedCycleAmount = fixedCycleAmountInstallmentsPlan.fixedCycleAmount
    }

    companion object {

        fun fromRequest(installmentPlanRequest: InstallmentPlanRequest?): InstallmentPlan? {
            var installmentPlan: InstallmentPlan? = null
            when (installmentPlanRequest?.planType) {
                InstallmentPlanType.FIXED_CYCLES -> installmentPlan = InstallmentPlan(installmentPlanRequest as FixedCycleInstallmentsPlan)
                InstallmentPlanType.FIXED_CYCLE_AMOUNT -> installmentPlan = InstallmentPlan(installmentPlanRequest as FixedCycleAmountInstallmentsPlan)
                InstallmentPlanType.REVOLVING -> installmentPlan = InstallmentPlan(installmentPlanRequest as RevolvingInstallmentsPlan)
                else -> installmentPlan = null
            }
            return installmentPlan
        }


        @JvmField val CREATOR: Parcelable.Creator<InstallmentPlan> = object : Parcelable.Creator<InstallmentPlan> {
            override fun createFromParcel(parcel: Parcel): InstallmentPlan {
                return InstallmentPlan(
                        InstallmentPlanType.valueOf(parcel.readString()),
                        parcel.readInt(),
                        parcel.readValue(BigInteger::class.java.classLoader) as BigInteger?
                )
            }

            override fun newArray(i: Int): Array<InstallmentPlan?> {
                return arrayOfNulls(0)
            }
        }
    }
}
