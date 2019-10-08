package com.univapay.adapters

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import com.univapay.UnivapayApplication
import com.univapay.sdk.types.Konbini

enum class KonbiniAdapter constructor(@param:StringRes val nameResId: Int, @param:DrawableRes val imageResId: Int, val konbini: Konbini){

    SEVEN_ELEVEN(com.univapay.R.string.checkout_konbini_seven_eleven, com.univapay.R.drawable.ic_seven_eleven, Konbini.SEVEN_ELEVEN),
    FAMILY_MART(com.univapay.R.string.checkout_konbini_family_mart, com.univapay.R.drawable.ic_family_mart, Konbini.FAMILY_MART),
    LAWSON(com.univapay.R.string.checkout_konbini_lawson, com.univapay.R.drawable.ic_lawson, Konbini.LAWSON),
    MINI_STOP(com.univapay.R.string.checkout_konbini_mini_stop, com.univapay.R.drawable.ic_ministop, Konbini.MINI_STOP),
    SEICO_MART(com.univapay.R.string.checkout_konbini_seico_mart, com.univapay.R.drawable.ic_seicomart, Konbini.SEICO_MART),
    DAILY_YAMAZAKI(com.univapay.R.string.checkout_konbini_daily_yamazaki, com.univapay.R.drawable.ic_daily_yamazaki, Konbini.DAILY_YAMAZAKI);

    override fun toString(): String {
        return  UnivapayApplication.context.getString(nameResId)
    }

    companion object {
        fun fromEnum(konbini: Konbini): KonbiniAdapter{
            val adapted = when(konbini){
                Konbini.SEVEN_ELEVEN -> SEVEN_ELEVEN
                Konbini.FAMILY_MART -> FAMILY_MART
                Konbini.LAWSON -> LAWSON
                Konbini.MINI_STOP -> MINI_STOP
                Konbini.SEICO_MART -> SEICO_MART
                Konbini.DAILY_YAMAZAKI -> DAILY_YAMAZAKI
                else -> throw Exception("Store ${konbini.name} is not supported")
            }
            return adapted
        }
    }
}
