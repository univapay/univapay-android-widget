package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import com.univapay.sdk.models.response.transactiontoken.TransactionToken

class TransactionTokenParcel: Parcelable{

    var transactionTokenInfo: TransactionToken? = null
        private set

    constructor(transactionTokenInfo: TransactionToken) {
        this.transactionTokenInfo = transactionTokenInfo
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // TransactionTokenWithDataParcel
        dest.writeSerializable(transactionTokenInfo!!.toString())
    }

    private fun readFromParcel(`in`: Parcel) {
        // TransactionTokenWithDataParcel
        transactionTokenInfo = `in`.readSerializable() as TransactionToken
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<TransactionTokenParcel> = object : Parcelable.Creator<TransactionTokenParcel> {
            override fun createFromParcel(parcel: Parcel): TransactionTokenParcel {
                return TransactionTokenParcel(parcel)
            }

            override fun newArray(size: Int): Array<TransactionTokenParcel?> {
                return arrayOfNulls(size)
            }
        }
    }

}
