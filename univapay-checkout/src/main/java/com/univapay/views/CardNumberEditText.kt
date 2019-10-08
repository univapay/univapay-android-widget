package com.univapay.views

import android.content.Context
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.widget.TextViewCompat
import android.text.*
import android.util.AttributeSet
import com.univapay.models.CardType

/**
 * Card Number EditText
 */
class CardNumberEditText : BaseEditText, TextWatcher {
    /**
     * @return The [CardType] currently entered in
     * the [android.widget.EditText]
     */
    var cardType: CardType? = null
        internal set
    private var onCardTypeChangedListener: ((CardType) -> Unit)? = null

    val cardNumber: String
        get() = text.toString().replace("-", "").trim { it <= ' ' }

    override val isValid: Boolean
        get() = cardType?.validate(text.toString()) ?: false

    override val errorMessage: String?
        get() = if (TextUtils.isEmpty(text)) {
            context.getString(com.univapay.R.string.checkout_required)
        } else {
            context.getString(com.univapay.R.string.checkout_invalid)
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
        if (isInEditMode) {
            return
        }

        inputType = InputType.TYPE_CLASS_NUMBER
        setCardIcon(com.univapay.R.drawable.ic_unknown)

        // Adding the TextWatcher
        addTextChangedListener(this)

        updateCardType()
    }


    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        updateCardType()
        cardType?.let {cardType->
            setCardIcon(cardType.logoResource)
        }
    }

    override fun afterTextChanged(editable: Editable) {
        val paddingSpans = editable.getSpans(0, editable.length, SpaceSpan::class.java)
        for (span in paddingSpans) {
            editable.removeSpan(span)
        }

        updateCardType()
        cardType?.let {cardType->
            setCardIcon(cardType.logoResource)
            addSpans(editable, cardType.spaceIndices)
            if (cardType.maxCardLength == selectionStart) {
                validate()
            }
        }
    }

    private fun updateCardType() {
        val type = CardType.forCardNumber(text.toString())
        if (cardType == null || cardType != type) {
            cardType = type

            cardType?.let{cardType->
                val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(cardType.maxCardLength))
                setFilters(filters)
                invalidate()
                onCardTypeChangedListener?.invoke(cardType)
            }

        }
    }

    private fun addSpans(editable: Editable, spaceIndices: IntArray) {
        val length = editable.length
        for (index in spaceIndices) {
            if (index <= length) {
                editable.setSpan(SpaceSpan(), index - 1, index,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    private fun setCardIcon(iconId: Int) {
        if (isInEditMode) {
            return
        }
        val drawableCompat = VectorDrawableCompat.create(activity!!.resources, com.univapay.R.drawable.ic_card, context.theme)
        if (text.isEmpty()) {
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, drawableCompat, null, null, null)
        } else {
            val icon = ResourcesCompat.getDrawable(resources, iconId, context.theme)
            TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, drawableCompat, null, icon, null)
        }
    }

    /**
     * Receive a callback when the [CardType] changes
     * @param listener to be called when the [CardType] changes
     */
    fun setOnCardTypeChangedListener(listener: (CardType) -> Unit) {
        onCardTypeChangedListener = listener
    }
}
