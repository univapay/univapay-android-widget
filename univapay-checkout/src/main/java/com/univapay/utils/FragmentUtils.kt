package com.univapay.utils

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Build
import android.support.annotation.IdRes
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import com.univapay.UnivapayApplication
import com.univapay.models.CheckoutArguments
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.common.auth.AppJWTStrategy
import com.univapay.sdk.settings.UnivapaySettings
import org.joda.money.format.MoneyFormatterBuilder
import java.net.URI

fun <T: View> lazyBind(view: View, @IdRes res: Int): Lazy<T?>{
    @Suppress("UNCHECKED_CAST")
    return lazy { bind<T>(view, res) }
}

fun <T: View> Activity.lazyBind(@IdRes res: Int): Lazy<T?>{
    @Suppress("UNCHECKED_CAST")
    return lazy {bind<T>(res)}
}

fun <T: View> bind(view: View, @IdRes res: Int): T?{
    return view.findViewById(res)
}

fun <T: View> Activity.bind(@IdRes res: Int): T?{
    return findViewById(res)
}

fun <T: View> bindNotNullable(view: View, @IdRes res: Int): T{
    return view.findViewById(res)
}

fun getUnivapay(args: CheckoutArguments?, packageName: String?): UnivapaySDK{
    val appJWT: String? = args?.appId ?: ""
    val settings = UnivapaySettings()
    args?.let {arguments->
        settings.withEndpoint(arguments.endpoint.toString())
        settings.withTimeoutSeconds(arguments.timeout)
        settings.attachOrigin(
                (
                    if("null" != arguments.origin.toString())
                        arguments.origin
                    else
                        packageName?.let {name->
                            getDefaultOrigin(name)
                        }
                ).toString()
        )
    }
    return UnivapaySDK.create(AppJWTStrategy(appJWT), settings)
}

fun unlockUI(action: String){
    val intent = Intent(action)
    LocalBroadcastManager.getInstance(UnivapayApplication.context).sendBroadcast(intent)
}

fun formatCurrency(money: MoneyLike, resources: Resources): String {
    val locale = if(Build.VERSION.SDK_INT < 24){
        resources.configuration.locale
    } else{
        resources.configuration.locales.get(0)
    }
    val formatterBuilder = MoneyFormatterBuilder().appendCurrencySymbolLocalized().appendAmount()
    return formatterBuilder.toFormatter(locale).print(money.asJodaMoney())
}

fun getDefaultOrigin(packageName: String): URI {

    return URI.create(
            packageName
                    .split(".")
                    .reversed()
                    .joinToString(separator = ".", prefix = "http://") { s-> s }
    )

}
