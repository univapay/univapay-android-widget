package com.univapay.views

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import com.univapay.sdk.models.response.transactiontoken.PhoneNumber

class PhoneEditText: CustomEditText{

    override val isValid: Boolean
        get() = super.isValid

    override val errorMessage: String?
        get() = super.errorMessage

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init(){
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_PHONE
    }

    fun getAsPhoneNumber(): PhoneNumber{
        return PhoneNumber(
                81,
                text.toString()
        )
    }
}
