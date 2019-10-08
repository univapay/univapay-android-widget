package com.univapay.models

import android.os.Parcel
import android.os.Parcelable

import com.univapay.sdk.models.response.charge.Charge

/**
 * Charge Details class can be written to and restored from a [Parcel].
 */
class ChargeParcel : Parcelable {
    // Getters and Setters
    var charge: Charge? = null
        private set

    constructor(charge: Charge) {
        this.charge = charge
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // ChargeParcel
        dest.writeSerializable(charge!!.toString())
    }

    private fun readFromParcel(`in`: Parcel) {
        // ChargeParcel
        charge = `in`.readSerializable() as Charge
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<ChargeParcel> = object : Parcelable.Creator<ChargeParcel> {
            override fun createFromParcel(parcel: Parcel): ChargeParcel {
                return ChargeParcel(parcel)
            }

            override fun newArray(size: Int): Array<ChargeParcel?> {
                return arrayOfNulls(size)
            }
        }
    }

}
