package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import com.univapay.sdk.builders.transactiontoken.TransactionTokensBuilders.GetTemporaryTokenAliasAsImageRequestBuilder
import com.univapay.sdk.types.TemporaryTokenAliasQRLogo

/**
* A class containing the configurable settings for QR code display.
* @param logoType: defines the way the store logo is displayed (centered, as background or not displayed at all)
* @param color: The color of the QR code (excluding the logo)
*/
class QROptions(val logoType: TemporaryTokenAliasQRLogo?, val color: String?): Parcelable{

    constructor(logoType: TemporaryTokenAliasQRLogo): this(logoType, null)

    constructor(color: String): this(null, color)

    fun <T: GetTemporaryTokenAliasAsImageRequestBuilder> attachOptionsToBuilder(requestBuilder: T): T{
        logoType?.let {lt->
            requestBuilder.withLogoType(lt)
        }

        color?.let { c->
            requestBuilder.withColor(c)
        }

        return requestBuilder
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(logoType?.name)
        dest?.writeString(color)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<QROptions> = object : Parcelable.Creator<QROptions> {
            override fun createFromParcel(parcel: Parcel): QROptions {
                val logoType = parcel.readString()?.let {lt->
                    TemporaryTokenAliasQRLogo.valueOf(lt)
                }
                val color = parcel.readString()

                return QROptions(logoType, color)
            }

            override fun newArray(size: Int): Array<QROptions?> {
                return arrayOfNulls(size)
            }
        }
    }
}
