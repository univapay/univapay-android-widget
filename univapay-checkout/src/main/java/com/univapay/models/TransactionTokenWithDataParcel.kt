package com.univapay.models

import android.os.Parcel
import android.os.Parcelable

import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData


/**
 * TransactionTokenWithDataParcel class can be written to and restored from a [Parcel].
 */
class TransactionTokenWithDataParcel(val transactionTokenInfo: TransactionTokenWithData) : Parcelable {

    private constructor(`in`: Parcel): this(`in`.readSerializable() as TransactionTokenWithData)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // TransactionTokenWithDataParcel
        dest.writeSerializable(transactionTokenInfo.toString())
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<TransactionTokenWithDataParcel> = object : Parcelable.Creator<TransactionTokenWithDataParcel> {
            override fun createFromParcel(parcel: Parcel): TransactionTokenWithDataParcel {
                return TransactionTokenWithDataParcel(parcel)
            }

            override fun newArray(size: Int): Array<TransactionTokenWithDataParcel?> {
                return arrayOfNulls(size)
            }
        }
    }

}
