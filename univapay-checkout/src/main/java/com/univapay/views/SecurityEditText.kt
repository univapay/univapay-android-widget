package com.univapay.views

import android.content.Context
import android.text.*
import android.util.AttributeSet
import com.univapay.models.CardType

/**
 * Card Security EditText
 */
class SecurityEditText : CustomEditText, TextWatcher {

    private var mCardType: CardType? = null

    private val securityCodeLength: Int
        get() = if (mCardType == null) {
            DEFAULT_MAX_LENGTH
        } else {
            mCardType!!.securityCodeLength
        }

    override val isValid: Boolean
        get() = text.toString().let {cvvText->
            cvvText.length == securityCodeLength &&
                    cvvText.toIntOrNull()?.let{true} ?: false
        }

    fun asInt(): Int{
        return text.toString().toInt()
    }

    override val errorMessage: String?
        get() {
            val securityCodeName: String
            if(mCardType == null) {
                securityCodeName = context.getString(com.univapay.R.string.cvv)
            } else {
                securityCodeName = context.getString(mCardType!!.securityCodeName)
            }

            return if (TextUtils.isEmpty(text)) {
                context.getString(com.univapay.R.string.checkout_cvv_required, securityCodeName)
            } else {
                context.getString(com.univapay.R.string.checkout_cvv_invalid, securityCodeName)
            }
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
        inputType = InputType.TYPE_CLASS_NUMBER
        filters = arrayOf<InputFilter>(InputFilter.LengthFilter(DEFAULT_MAX_LENGTH))

        // Adding the TextWatcher
        addTextChangedListener(this)
    }

    fun setCardType(cardType: CardType) {
        mCardType = cardType

        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(cardType.securityCodeLength))
        setFilters(filters)

        contentDescription = context.getString(cardType.securityCodeName)
        setFieldHint(cardType.securityCodeName)

        invalidate()
    }


    override fun afterTextChanged(editable: Editable) {
        if (securityCodeLength == selectionStart) {
            validate()
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {}

    companion object {
        private val DEFAULT_MAX_LENGTH = 3
    }
}
