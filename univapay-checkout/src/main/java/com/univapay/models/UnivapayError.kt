package com.univapay.models

import android.os.Parcel
import android.os.Parcelable

/**
 * UnivapayError class can be written to and restored from a [Parcel].
 */
class UnivapayError : Parcelable {

    // Getters and Setters
    var message: String? = null
        private set

    constructor(message: String) {
        this.message = message
    }

    constructor(throwable: Throwable) {
        this.message = throwable.message
    }

    private constructor(`in`: Parcel) {
        readFromParcel(`in`)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // UnivapayError
        dest.writeString(message)
    }

    private fun readFromParcel(`in`: Parcel) {
        // UnivapayError
        message = `in`.readString()
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<UnivapayError> = object : Parcelable.Creator<UnivapayError> {
            override fun createFromParcel(parcel: Parcel): UnivapayError {
                return UnivapayError(parcel)
            }

            override fun newArray(size: Int): Array<UnivapayError?> {
                return arrayOfNulls(size)
            }
        }
    }

}
