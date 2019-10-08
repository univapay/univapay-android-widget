package com.univapay.views

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.text.*
import android.util.AttributeSet
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import com.univapay.fragments.MonthYearPickerDialog
import com.univapay.utils.DateValidator
import java.text.DecimalFormat

/**
 * Expiry EditText
 */
class ExpiryEditText : BaseEditText, TextWatcher, View.OnClickListener {
    private var mChangeWasAddition: Boolean = false
    private var mExpiryEditClickListener: View.OnClickListener? = null
    private var mUseExpirationDateDialog = true
    private var mExpirationDateDialog: MonthYearPickerDialog? = null

    /**
     * @return the 2-digit month, formatted with a leading zero if necessary. If no month has been
     * specified, an empty string is returned.
     */
    val month: String
        get() = if(string.length < 2) "" else string.substring(0, 2)


    /**
     * @return the 2- or 4-digit year depending on user input.
     * If no year has been specified, an empty string is returned.
     */
    val year: String
        get() = if (string.length == 4 || string.length == 6) string.substring(2) else ""

    /**
     * @return whether or not the input is a valid card expiration date.
     */
    override val isValid: Boolean
        get() = DateValidator.isValid(month, year)

    override val errorMessage: String?
        get() = if (TextUtils.isEmpty(text)) {
            context.getString(com.univapay.R.string.checkout_required)
        } else {
            context.getString(com.univapay.R.string.checkout_invalid)
        }

    /**
     * Convenience method to get the input text as a [String].
     */
    private val string: String
        get() = text?.toString() ?: ""

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    private fun init() {
        if (isInEditMode) {
            return
        }

        setIcon()

        inputType = InputType.TYPE_CLASS_NUMBER
        val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
        setFilters(filters)

        addTextChangedListener(this)
        setShowKeyboardOnFocus(!mUseExpirationDateDialog)
        isCursorVisible = !mUseExpirationDateDialog
        super.setOnClickListener(this)

        mExpirationDateDialog = MonthYearPickerDialog()
        mExpirationDateDialog!!.setDateSetListener {_: DatePicker?, year: Int?, month: Int?, _: Int? ->
            year?.let{y->
                month?.let { m->
                    this@ExpiryEditText.setValue(y, m)
                }
            }
        }
    }

    override fun setOnClickListener(clickListener: View.OnClickListener?) {
        mExpiryEditClickListener = clickListener
    }

    override fun onClick(v: View) {
        if (mUseExpirationDateDialog) {
            closeSoftKeyboard()
            showMonthYearDialog()
        }

        mExpiryEditClickListener?.onClick(v)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (mExpirationDateDialog == null) {
            return
        }

        if (focused && mUseExpirationDateDialog) {
            closeSoftKeyboard()
            showMonthYearDialog()
        } else if (mUseExpirationDateDialog) {
            mExpirationDateDialog?.dismiss()
        }
    }

    private fun showMonthYearDialog() {
        val month = month
        val year = year
        val activity = activity as AppCompatActivity?
        mExpirationDateDialog?.setSelectedMonth(if (month.isEmpty()) -1 else Integer.parseInt(month))
        mExpirationDateDialog?.setSelectedYear(if (year.isEmpty()) -1 else Integer.parseInt(year))
        mExpirationDateDialog?.show(activity?.supportFragmentManager, "MonthYearPickerDialog")
    }

    /**
     * Set the Value of the control
     * @param year Expiry year
     * @param month Expiry month
     */
    fun setValue(year: Int, month: Int) {
        var expirationDate: String
        if (month == -1) {
            expirationDate = "  "
        } else {
            val monthFormat = DecimalFormat("00")
            expirationDate = monthFormat.format(month.toLong())
        }

        if (year == -1) {
            expirationDate += "    "
        } else {
            val yearFormat = DecimalFormat("0000")
            expirationDate += yearFormat.format(year.toLong())
        }
        this@ExpiryEditText.setText(expirationDate)
        this@ExpiryEditText.validate()
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        mChangeWasAddition = lengthAfter > lengthBefore
    }

    override fun afterTextChanged(editable: Editable) {
        if (mChangeWasAddition) {
            if (editable.length == 1 && Character.getNumericValue(editable[0]) >= 2) {
                prependLeadingZero(editable)
            }
        }

        val paddingSpans = editable.getSpans(0, editable.length, SlashSpan::class.java)
        for (span in paddingSpans) {
            editable.removeSpan(span)
        }

        addDateSlash(editable)
    }

    private fun setShowKeyboardOnFocus(showKeyboardOnFocus: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showSoftInputOnFocus = showKeyboardOnFocus
        } else {
            try {
                // API 19-21
                val method = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(this, showKeyboardOnFocus)
            } catch (e: Exception) {
                mUseExpirationDateDialog = false

            }

        }
    }

    private fun prependLeadingZero(editable: Editable) {
        val firstChar = editable[0]
        editable.replace(0, 1, "0").append(firstChar)
    }

    private fun addDateSlash(editable: Editable) {
        val index = 2
        val length = editable.length
        if (index <= length) {
            editable.setSpan(SlashSpan(), index - 1, index,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun setIcon() {
        val icon = VectorDrawableCompat.create(activity!!.resources, com.univapay.R.drawable.ic_expiry, context.theme)
        TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, icon, null, null, null)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
}
