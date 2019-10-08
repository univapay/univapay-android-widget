package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import com.univapay.sdk.types.Konbini
import org.joda.time.Period

class KonbiniDetails: Parcelable {

    var name: String? = null
    var email: String? = null
    var phoneNumber:String? = null
    var convenienceStore: Konbini? = null
    var expirationPeriod: Period? = null
    var rememberKonbini: Boolean = false

    constructor()

    constructor(name: String, email: String, phoneNumber: String, convenienceStore: Konbini, expirationPeriod: Period, rememberKonbini: Boolean){
        this.name = name
        this.email = email
        this.phoneNumber = phoneNumber
        this.convenienceStore = convenienceStore
        this.expirationPeriod = expirationPeriod
        this.rememberKonbini = rememberKonbini
    }

    constructor(name: String, email: String, phoneNumber: String, convenienceStore: Konbini, rememberKonbini: Boolean){
        this.name = name
        this.email = email
        this.phoneNumber = phoneNumber
        this.convenienceStore = convenienceStore
        this.rememberKonbini = rememberKonbini
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // AddressDetails
        dest.writeString(name)
        dest.writeString(email)
        dest.writeString(phoneNumber)
        dest.writeString(convenienceStore?.name)
        dest.writeString(expirationPeriod?.toString())
        dest.writeByte((if(rememberKonbini) 1 else 0).toByte())
    }

    private fun readFromParcel(`in`: Parcel) {
        // AddressDetails
        name = `in`.readString()
        email = `in`.readString()
        phoneNumber = `in`.readString()

        val konbiniString: String = `in`.readString()
        convenienceStore = if(konbiniString.isEmpty()) null else Konbini.valueOf(konbiniString)

        val expirationPeriodString = `in`.readString()
        expirationPeriod = if(expirationPeriodString.isNullOrEmpty()) null else Period.parse(expirationPeriodString)

        rememberKonbini = `in`.readByte().toInt() != 0

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<KonbiniDetails> = object : Parcelable.Creator<KonbiniDetails> {
            override fun createFromParcel(parcel: Parcel): KonbiniDetails {
                return KonbiniDetails(parcel)
            }

            override fun newArray(size: Int): Array<KonbiniDetails?> {
                return arrayOfNulls(size)
            }
        }
    }
}
