package com.univapay.utils

import android.util.Log
import com.univapay.UnivapayApplication
import com.univapay.models.UnivapayError
import com.univapay.sdk.models.errors.DetailedError
import com.univapay.sdk.models.errors.UnivapayException
import com.univapay.sdk.models.response.PaymentError

/**
 * A helper class for parsing errors coming from the UnivaPay API.
 */
object ErrorParser {

    fun parseError(error: UnivapayException?): UnivapayError {
        val msg = StringBuilder()

        // API Error
        if (error != null && error.body != null) {
            val errorBody = error.body
            val detailedErrorList = errorBody.errors
            if (detailedErrorList != null && Constants.VALIDATION_ERROR.equals(errorBody.code)) {
                for (detailError in detailedErrorList) {
                    msg.append(getErrorMessage(detailError))
                }
            } else {
                msg.append(errorBody.code)
            }
        }

        return UnivapayError(msg.toString())
    }

    fun parseError(paymentError: PaymentError): UnivapayError {
        val msg = StringBuilder()

        val errorCode = paymentError.code.toString()
        val res = UnivapayApplication.context.resources
        val resName = "checkout_payment_error_$errorCode"
        val resId = getResId(resName)

        if (resId == -1) {
            msg.append(res.getString(com.univapay.R.string.checkout_payment_error_unknown))
        } else {
            msg.append(res.getString(resId))
        }

        return UnivapayError(msg.toString())
    }

    fun parseError(throwable: Throwable): UnivapayError {
        return if (UnivapayException::class.java.isInstance(throwable)) {
            parseError(throwable as UnivapayException?)
        } else {
            UnivapayError(throwable)
        }
    }

    private fun getErrorMessage(detailedError: DetailedError): String {
        val res = UnivapayApplication.context.resources

        when (detailedError.reason) {
            Constants.REQUIRED_VALUE -> return res.getString(com.univapay.R.string.checkout_error_required_value)
                    .replace("%s", detailedError.field)
            Constants.INVALID_CARD_NUMBER -> return res.getString(com.univapay.R.string.checkout_error_invalid_card_number)
            Constants.INVALID_FORMAT_EMAIL -> return res.getString(com.univapay.R.string.checkout_error_invalid_email)
            Constants.INVALID_CVV -> return res.getString(com.univapay.R.string.checkout_error_invalid_cvv)
            Constants.INVALID_CARD_BRAND -> return res.getString(com.univapay.R.string.checkout_error_invalid_card_brand)
            Constants.INVALID_FORMAT_CURRENCY -> return res.getString(com.univapay.R.string.checkout_error_invalid_format_currency)
            Constants.INVALID_AMOUNT,
            Constants.INVALID_FORMAT,
            Constants.INVALID_FORMAT_LENGTH,
            Constants.INVALID_FORMAT_COUNTRY,
            Constants.NESTED_JSON_NOT_ALLOWED,
            Constants.INVALID_FORMAT_DATE -> return res.getString(com.univapay.R.string.checkout_error_invalid)
                    .replace("%s", detailedError.field)
            else -> return detailedError.reason
        }
    }

    private fun getResId(stringName: String): Int {
        try {
            val res = com.univapay.R.string::class.java
            val field = res.getField(stringName)
            return field.getInt(null)
        } catch (e: Exception) {
            Log.e("Error Parser", "Failure to get string appId.", e)
        }

        return -1
    }
}
