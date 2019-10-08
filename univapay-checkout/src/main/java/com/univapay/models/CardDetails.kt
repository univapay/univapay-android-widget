package com.univapay.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Card Details class can be written to and restored from a [Parcel].
 */
class CardDetails : Parcelable {
    // Getters and Setters
    var email: String? = null
    var cardholderName: String? = null
    var cardNumber: String? = null
    var security: Int? = -1
    var month: Int? = -1
    var year: Int? = -1
    var rememberCard: Boolean = false
    var requireCvv: Boolean = true

    constructor() {}

    constructor(email: String?, cardholderName: String?, cardNumber: String?, security: Int?, month: Int?, year: Int?, rememberCard: Boolean, requireCvv: Boolean) {
        this.email = email
        this.cardholderName = cardholderName
        this.cardNumber = cardNumber
        this.security = security
        this.month = month
        this.year = year
        this.rememberCard = rememberCard
        this.requireCvv = requireCvv
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // CardDetails
        dest.writeString(email)
        dest.writeString(cardholderName)
        dest.writeString(cardNumber)
        dest.writeInt(security!!)
        dest.writeInt(month!!)
        dest.writeInt(year!!)
        dest.writeByte(if(rememberCard) 1 else 0)
        dest.writeByte(if(requireCvv) 1 else 0)
    }

    private fun readFromParcel(`in`: Parcel) {
        // CardDetails
        email = `in`.readString()
        cardholderName = `in`.readString()
        cardNumber = `in`.readString()
        security = `in`.readInt()
        month = `in`.readInt()
        year = `in`.readInt()
        rememberCard = `in`.readByte().toInt() != 0
        requireCvv = `in`.readByte().toInt() != 0
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<CardDetails> = object : Parcelable.Creator<CardDetails> {
            override fun createFromParcel(parcel: Parcel): CardDetails {
                return CardDetails(parcel)
            }

            override fun newArray(size: Int): Array<CardDetails?> {
                return arrayOfNulls(size)
            }
        }
    }
}
