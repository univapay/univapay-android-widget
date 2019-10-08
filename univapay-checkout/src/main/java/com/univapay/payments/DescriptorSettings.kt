package com.univapay.payments

import android.os.Parcel
import android.os.Parcelable

class DescriptorSettings(val descriptor: String, val ignoreDescriptorOnError: Boolean = false): Parcelable{

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(descriptor)
        dest.writeByte((if(ignoreDescriptorOnError) 1 else 0).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<DescriptorSettings> {
        override fun createFromParcel(parcel: Parcel): DescriptorSettings {
            val descriptor = parcel.readString()
            val ignoreDescriptorOnError = parcel.readByte().toInt() == 1
            return DescriptorSettings(descriptor, ignoreDescriptorOnError)
        }

        override fun newArray(size: Int): Array<DescriptorSettings?> {
            return arrayOfNulls(size)
        }
    }
}
