package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class CaptureSettings private constructor(val capture: Boolean = true, val captureAt: Date? = null): Parcelable{

    constructor(capture: Boolean): this(capture, null)

    constructor(captureAt: Date): this(false, captureAt)

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (capture) 1 else 0).toByte())
        captureAt?.let{
            dest.writeLong(captureAt.time)
        }?: dest.writeLong(0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<CaptureSettings> = object : Parcelable.Creator<CaptureSettings> {
            override fun createFromParcel(parcel: Parcel): CaptureSettings {
                return readFromParcel(parcel)
            }

            override fun newArray(size: Int): Array<CaptureSettings?> {
                return arrayOfNulls(size)
            }
        }

        fun readFromParcel(`in`: Parcel): CaptureSettings {
            val capture = `in`.readByte().toInt() == 1
            val dateLong = `in`.readLong()
            val captureAt = if(dateLong == 0L) null else Date(dateLong)

            return CaptureSettings(capture, captureAt)
        }
    }
}
