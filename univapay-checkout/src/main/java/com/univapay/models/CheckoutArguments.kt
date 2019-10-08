package com.univapay.models

import android.os.Parcel
import android.os.Parcelable
import com.univapay.payments.CheckoutType
import com.univapay.sdk.types.MetadataMap
import com.univapay.sdk.types.RecurringTokenInterval
import com.univapay.utils.BundleParser
import com.univapay.utils.Constants
import org.joda.time.Period
import java.net.URI
import java.util.*

/**
 * A Parcelable containing the arguments passed to the [CheckoutBuilder]
 */
class CheckoutArguments(appCredentials: AppCredentials) : Parcelable {

    var appId: String? = appCredentials.appId
    var customerId: UUID? = null
    var checkoutType = CheckoutType.TOKEN
        private set
    var imageResourceId = -1
    var title: String? = null
    var description: String? = null
    var isAddress = false
    var metadata: MetadataMap = MetadataMap()
    var cvvRequired: Boolean = true
    var endpoint: URI = URI.create(Constants.API_ENDPOINT)
    var timeout: Long = 10
    var origin: URI? = null
    var konbiniExpirationPeriod: Period = Period.days(30)
    var recurringTokenUsageLimit: RecurringTokenInterval? = null
    var capture: Boolean = true
    var captureAt: Date? = null
    var paymentMethod: SupportedPaymentMethod? = null
    var qrOptions: QROptions? = null
    var useDisplayQrMode: Boolean = false

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        // CheckoutArguments
        dest.writeString(appId)
        dest.writeString(checkoutType.name)
        dest.writeString(customerId?.toString())
        dest.writeInt(imageResourceId)
        dest.writeString(title)
        dest.writeString(description)
        dest.writeByte((if (isAddress) 1 else 0).toByte())
        dest.writeBundle(BundleParser.fromMap(metadata))
        dest.writeByte((if (cvvRequired) 1 else 0).toByte())
        dest.writeString(endpoint.toString())
        dest.writeLong(timeout)
        dest.writeString(origin?.toString())
        dest.writeString(konbiniExpirationPeriod.toString())
        dest.writeString(recurringTokenUsageLimit?.toString())
        dest.writeByte((if (capture) 1 else 0).toByte())
        dest.writeSerializable(captureAt)
        dest.writeParcelable(paymentMethod, 0)
        dest.writeParcelable(qrOptions, 0)
        dest.writeByte((if (useDisplayQrMode) 1 else 0).toByte())
    }

    fun canRememberCards(): Boolean{
        return customerId != null
    }

    companion object {

        @JvmField val CREATOR: Parcelable.Creator<CheckoutArguments> = object : Parcelable.Creator<CheckoutArguments> {
            override fun createFromParcel(parcel: Parcel): CheckoutArguments {
                return CheckoutArguments.readFromParcel(parcel)
            }

            override fun newArray(size: Int): Array<CheckoutArguments?> {
                return arrayOfNulls(size)
            }
        }

        fun readFromParcel(`in`: Parcel): CheckoutArguments {
            val appId = `in`.readString()
            val checkoutType = CheckoutType.valueOf(`in`.readString())
            val customerId = `in`.readString()
            val imageResourceId = `in`.readInt()
            val title = `in`.readString()
            val description = `in`.readString()
            val isAddress = `in`.readByte().toInt() != 0
            val metadata = BundleParser.toMap(`in`.readBundle()) as HashMap<String, String>
            val cvvRequired = `in`.readByte().toInt() != 0
            val endpointStr = `in`.readString()
            val timeout = `in`.readLong()
            val originStr = `in`.readString()
            val konbiniExpirationString = `in`.readString()
            val recurringTokenIntervalStr = `in`.readString()

            val args = CheckoutArguments(AppCredentials(appId))
            args.appId = appId
            args.checkoutType = checkoutType
            customerId?.let {id->
                args.customerId = UUID.fromString(id)
            }
            args.imageResourceId = imageResourceId
            args.title = title
            args.description = description
            args.isAddress = isAddress
            val metadataMap = MetadataMap()
            metadataMap.putAll(metadata)
            args.metadata = metadataMap
            args.cvvRequired = cvvRequired
            args.endpoint = URI.create(endpointStr)
            args.timeout = timeout
            originStr?.let {origin->
                args.origin = URI.create(origin)
            }
            args.konbiniExpirationPeriod = Period.parse(konbiniExpirationString)
            recurringTokenIntervalStr?.let {usageLimit->
                args.recurringTokenUsageLimit = RecurringTokenInterval.valueOf(usageLimit)
            }

            args.capture = `in`.readInt() == 1
            args.captureAt = `in`.readSerializable() as Date?
            args.paymentMethod = `in`.readParcelable(SupportedPaymentMethod::class.java.classLoader)
            args.qrOptions = `in`.readParcelable(QROptions::class.java.classLoader)
            args.useDisplayQrMode = `in`.readByte().toInt() != 0

            return args
        }
    }
}
