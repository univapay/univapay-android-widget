package com.univapay.views

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager

/**
 * Base class for EditText
 */
open class BaseEditText : TextInputEditText {
    /**
     * @return the current error state of the [android.widget.EditText]
     */
    var isError = false
        internal set

    protected val activity: Activity?
        get() {
            var context = context
            while (context is ContextWrapper) {
                if (context is Activity) {
                    return context
                }
                context = context.baseContext
            }
            return null
        }

    /**
     * @return the [TextInputLayout] parent if present, otherwise `null`.
     */
    val textInputLayoutParent: TextInputLayout?
        get() = if (parent != null && parent.parent is TextInputLayout) {
            parent.parent as TextInputLayout
        } else null

    /**
     * Override this method validation logic
     *
     * @return `true`
     */
    open val isValid: Boolean
        get() = true


    /**
     * Override this method to display error messages
     *
     * @return [String] error message to display.
     */
    open val errorMessage: String?
        get() = null

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
        onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validate()
            }
        }
    }

    /**
     * Attempt to close the soft keyboard. Will have no effect if the keyboard is not open.
     */
    fun closeSoftKeyboard() {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(windowToken, 0)
    }

    /**
     * Sets the hint on the [TextInputLayout] if this view is a child of a [TextInputLayout], otherwise
     * sets the hint on this [android.widget.EditText].
     *
     * @param hint The string resource to use as the hint.
     */
    fun setFieldHint(hint: Int) {
        setFieldHint(context.getString(hint))
    }

    /**
     * Sets the hint on the [TextInputLayout] if this view is a child of a [TextInputLayout], otherwise
     * sets the hint on this [android.widget.EditText].
     *
     * @param hint The string value to use as the hint.
     */
    fun setFieldHint(hint: String) {
        if (textInputLayoutParent != null) {
            textInputLayoutParent!!.hint = hint
        } else {
            setHint(hint)
        }
    }

    /**
     * Check if the [BaseEditText] is valid and set the correct error state and visual
     * indication on it.
     */
    fun validate() {
        if (isValid) {
            setError(null)
        } else {
            setError(errorMessage)
        }
    }

    /**
     * Controls the error state of this [BaseEditText] and sets a visual indication that the
     * [BaseEditText] contains an error.
     *
     * @param errorMessage the error message to display to the user. `null` will remove any error message displayed.
     */
    fun setError(errorMessage: String?) {
        isError = !TextUtils.isEmpty(errorMessage)

        val textInputLayout = textInputLayoutParent
        if (textInputLayout != null) {
            //            textInputLayout.setErrorEnabled(!TextUtils.isEmpty(errorMessage));
            textInputLayout.error = errorMessage
        }
    }
}
