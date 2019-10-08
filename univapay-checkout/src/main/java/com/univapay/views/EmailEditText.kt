package com.univapay.views

import android.content.Context
import android.text.InputType
import android.text.TextUtils
import android.util.AttributeSet

/**
 * Email EditText
 */

class EmailEditText : CustomEditText {

    override val isValid: Boolean
        get() = super.isValid && isValidEmail(text)

    override val errorMessage: String?
        get() {
            val errorMsg = super.errorMessage
            return if (!TextUtils.isEmpty(errorMsg)) {
                errorMsg
            } else context.getString(com.univapay.R.string.checkout_invalid)
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
    }

    private fun isValidEmail(email: CharSequence?): Boolean {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
