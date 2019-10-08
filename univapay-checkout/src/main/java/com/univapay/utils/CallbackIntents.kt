package com.univapay.utils

import android.os.Parcel
import android.os.Parcelable

object CallbackIntents {

    inline fun <reified T: Parcelable> getCreator() =
            object: Parcelable.ClassLoaderCreator<T> {
                override fun createFromParcel(source: Parcel, loader: ClassLoader): T = source.readParcelable(loader)

                override fun createFromParcel(source: Parcel): T = createFromParcel(source, T::class.java.classLoader)

                override fun newArray(size: Int): Array<T?> = arrayOfNulls(size)
            }

    abstract class CallbackIntentParcelable: Parcelable {
        override fun describeContents(): Int{
            return 0
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            dest?.writeValue(this::class.java.classLoader)
        }
    }

    sealed class TokenCallbackIntent: CallbackIntentParcelable(){
        class TokenSuccessCallback: TokenCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<TokenSuccessCallback>()
            }
        }
        class TokenFailureCallback: TokenCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<TokenFailureCallback>()
            }
        }
    }

    sealed class ChargeCallbackIntent: CallbackIntentParcelable(){
        class ChargeSuccessCallback: ChargeCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<ChargeSuccessCallback>()
            }
        }
        class ChargeUndeterminedCallback: ChargeCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<ChargeUndeterminedCallback>()
            }
        }
        class ChargeFailureCallback: ChargeCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<ChargeFailureCallback>()
            }
        }
    }

    sealed class SubscriptionCallbackIntent: CallbackIntentParcelable(){
        class SubscriptionSuccessCallback: SubscriptionCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<SubscriptionSuccessCallback>()
            }
        }
        class SubscriptionFailureCallback: SubscriptionCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<SubscriptionFailureCallback>()
            }
        }
        class SubscriptionUndeterminedCallback: SubscriptionCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<SubscriptionUndeterminedCallback>()
            }
        }
    }

    sealed class InitializationCallbackIntent: CallbackIntentParcelable(){
        class InitializationSuccessCallback: InitializationCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<InitializationSuccessCallback>()
            }
        }
        class InitializationFailureCallback: InitializationCallbackIntent(){
            companion object {
                @JvmField val CREATOR = getCreator<InitializationFailureCallback>()
            }
        }
    }
}
