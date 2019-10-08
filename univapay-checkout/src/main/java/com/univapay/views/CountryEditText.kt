package com.univapay.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Handler
import android.support.v4.widget.TextViewCompat
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.view.View
import android.widget.EditText

import com.univapay.fragments.CountryPickerDialog
import com.univapay.models.Country

/**
 * Country EditText
 */
class CountryEditText : BaseEditText, View.OnClickListener {
    private var mClickListener: View.OnClickListener? = null
    private var mCountryPickerDialog: CountryPickerDialog? = null
    private var mSelectedCountry: Country? = null

    /**
     * @return the 2 character code. If no country has been
     * specified, an empty string is returned.
     */
    val countryCode: String
        get() = mSelectedCountry?.code ?: ""

    /**
     * @return the country name depending on user input.
     * If no country has been specified, an empty string is returned.
     */
    val countryName: String
        get() = mSelectedCountry?.name ?: ""

    /**
     * @return whether or not the input is a valid card expiration date.
     */
    override val isValid: Boolean
        get() = mSelectedCountry != null

    override val errorMessage: String?
        get() = context.getString(com.univapay.R.string.checkout_required)

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

        if (!isInEditMode) {
            setShowKeyboardOnFocus(false)
            isCursorVisible = false
            super.setOnClickListener(this)

            // Dialog Instance
            mCountryPickerDialog = CountryPickerDialog.newInstance()

            // HACK: HintText and Text will overlap if not set in post delay
            Handler().postDelayed(
                    {
                        // Get default Country from Devices
                        mSelectedCountry = mCountryPickerDialog?.getUserCountryInfo(context)
                        setText(mSelectedCountry?.name)
                        setIcon()
                    }, 1
            )

            mCountryPickerDialog?.setCountrySelectListener { country: Country ->
                mSelectedCountry = mCountryPickerDialog?.getSelectedCountry()
                this@CountryEditText.setText(country.name)
                setIcon()
                mCountryPickerDialog?.dismiss()
            }

        }
    }

    override fun setOnClickListener(clickListener: View.OnClickListener) {
        mClickListener = clickListener
    }

    override fun onClick(v: View) {
        closeSoftKeyboard()
        showDialog()
        mClickListener?.onClick(v)
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)

        if (focused) {
            closeSoftKeyboard()
            showDialog()
        }
    }

    private fun showDialog() {
        val activity = activity as AppCompatActivity?

        mSelectedCountry?.code?.let {code->
            mCountryPickerDialog?.setSelectedCountry(code)
        }

        mCountryPickerDialog?.show(activity?.supportFragmentManager, "CountryPickerDialog")
    }

    private fun setShowKeyboardOnFocus(showKeyboardOnFocus: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            showSoftInputOnFocus = showKeyboardOnFocus
        } else {
            try {
                // API 16-21
                val method = EditText::class.java.getMethod("setShowSoftInputOnFocus", Boolean::class.javaPrimitiveType)
                method.isAccessible = true
                method.invoke(this, showKeyboardOnFocus)
            } catch (e: Exception) {
                // Couldn't set Keyboard on Focus.
            }

        }
    }

    private fun setIcon() {
        mSelectedCountry?.let {country->
            val bMap = BitmapFactory.decodeResource(resources, country.flag)
            val bMapScaled = Bitmap.createScaledBitmap(bMap, 60, 40, true)
            val flag = BitmapDrawable(resources, bMapScaled)
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, flag, null, null, null)
        }
    }
}
