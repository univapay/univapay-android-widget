package com.univapay.views

import android.content.Context
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.widget.TextViewCompat
import android.text.TextUtils
import android.util.AttributeSet

/**
 * Custom EditText
 */
open class CustomEditText : BaseEditText {
    private var mIsRequired = false
    private var mIsRequiredMessage: Int = 0

    override val isValid: Boolean
        get() = !mIsRequired || !TextUtils.isEmpty(text.toString())

    override val errorMessage: String?
        get() = if (mIsRequired && TextUtils.isEmpty(text.toString())) {
            context.getString(mIsRequiredMessage)
        } else null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        setSingleLine()

        if (!isInEditMode) {
            setDrawable(context, attrs)
            setValidations(context, attrs)
        }
    }

    private fun setDrawable(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, com.univapay.R.styleable.CustomEditText, 0, 0)
        try {
            val drawableStart = ta.getResourceId(com.univapay.R.styleable.CustomEditText_drawableStart, -1)
            if (drawableStart != -1) {
                val drawableCompat = VectorDrawableCompat.create(activity!!.resources, drawableStart, getContext().theme)
                TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, drawableCompat, null, null, null)
            }
        } finally {
            ta.recycle()
        }
    }

    private fun setValidations(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, com.univapay.R.styleable.CustomEditText, 0, 0)
        try {
            mIsRequired = ta.getBoolean(com.univapay.R.styleable.CustomEditText_isRequired, false)
            mIsRequiredMessage = ta.getResourceId(com.univapay.R.styleable.CustomEditText_isRequiredErrorMessage, -1)
        } finally {
            ta.recycle()
        }
    }
}
