package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import com.univapay.sdk.models.response.subscription.FullSubscription



/**
 * Subscription Details class can be written to and restored from a [Parcel].
 */
class SubscriptionParcel : Parcelable {
    // Getters and Setters
    var subscription: FullSubscription? = null
        private set

    constructor(subscription: FullSubscription) {
        this.subscription = subscription
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // SubscriptionParcel
        dest.writeSerializable(subscription!!.toString())
    }

    private fun readFromParcel(`in`: Parcel) {
        // SubscriptionParcel
        subscription = `in`.readSerializable() as FullSubscription
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<SubscriptionParcel> = object : Parcelable.Creator<SubscriptionParcel> {
            override fun createFromParcel(parcel: Parcel): SubscriptionParcel {
                return SubscriptionParcel(parcel)
            }

            override fun newArray(size: Int): Array<SubscriptionParcel?> {
                return arrayOfNulls(size)
            }
        }
    }

}
