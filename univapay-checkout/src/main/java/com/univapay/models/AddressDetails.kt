package com.univapay.models

import android.os.Parcel
import android.os.Parcelable

/**
 * Address Details class can be written to and restored from a [Parcel].
 */
class AddressDetails : Parcelable {
    // Getters and Setters
    var name: String? = null
    var line1: String? = null
    var line2: String? = null
    var city: String? = null
    var state: String? = null
    var postCode: String? = null
    var country: String? = null

    constructor() {}

    constructor(name: String, line1: String, line2: String, city: String, state: String, country: String, postCode: String) {
        this.name = name
        this.line1 = line1
        this.line2 = line2
        this.city = city
        this.state = state
        this.country = country
        this.postCode = postCode
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // AddressDetails
        dest.writeString(name)
        dest.writeString(line1)
        dest.writeString(line2)
        dest.writeString(city)
        dest.writeString(postCode)
        dest.writeString(country)
    }

    private fun readFromParcel(`in`: Parcel) {
        // AddressDetails
        name = `in`.readString()
        line1 = `in`.readString()
        line2 = `in`.readString()
        city = `in`.readString()
        postCode = `in`.readString()
        country = `in`.readString()
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<AddressDetails> = object : Parcelable.Creator<AddressDetails> {
            override fun createFromParcel(parcel: Parcel): AddressDetails {
                return AddressDetails(parcel)
            }

            override fun newArray(size: Int): Array<AddressDetails?> {
                return arrayOfNulls(size)
            }
        }
    }

}
