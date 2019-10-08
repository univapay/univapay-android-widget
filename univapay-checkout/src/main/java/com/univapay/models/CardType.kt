package com.univapay.models

import android.text.TextUtils

import java.util.regex.Pattern

/**
 * Card Type enum, formatting and validation rules.
 */
enum class CardType private constructor(regex: String,
                                        /**
                                         * @return The android resource appId for the card logo image.
                                         */
                                        val logoResource: Int,
                                        /**
                                         * @return minimum length of a card for this [CardType]
                                         */
                                        val minCardLength: Int,
                                        /**
                                         * @return max length of a card for this [CardType]
                                         */
                                        val maxCardLength: Int,
                                        /**
                                         * @return The length of the current card's security code.
                                         */
                                        val securityCodeLength: Int,
                                        /**
                                         * @return The android resource appId for the security code name for this card type.
                                         */
                                        val securityCodeName: Int) {

    VISA("^4\\d*",
            com.univapay.R.drawable.ic_visa,
            16, 16,
            3, com.univapay.R.string.cvv),
    MASTERCARD("^(5[1-5]|222[1-9]|22[3-9]|2[3-6]|27[0-1]|2720)\\d*",
            com.univapay.R.drawable.ic_mastercard,
            16, 16,
            3, com.univapay.R.string.cvc),
    DISCOVER("^(6011|65|64[4-9]|622)\\d*",
            com.univapay.R.drawable.ic_discover,
            16, 16,
            3, com.univapay.R.string.cid),
    AMERICAN_EXPRESS("^3[47]\\d*",
            com.univapay.R.drawable.ic_american_express,
            15, 15,
            4, com.univapay.R.string.cid),
    DINERS_CLUB("^(36|38|30[0-5])\\d*",
            com.univapay.R.drawable.ic_diners_club,
            14, 14,
            3, com.univapay.R.string.cvv),
    JCB("^35\\d*",
            com.univapay.R.drawable.ic_jcb,
            16, 16,
            3, com.univapay.R.string.cvv),
    MAESTRO("^(5018|5020|5038|6020|6304|6703|6759|676[1-3])\\d*",
            com.univapay.R.drawable.ic_maestro,
            12, 19,
            3, com.univapay.R.string.cvc),
    UNIONPAY("^62\\d*",
            com.univapay.R.drawable.ic_unionpay,
            16, 19,
            3, com.univapay.R.string.cvn),
    UNKNOWN("\\d+",
            com.univapay.R.drawable.ic_unknown,
            12, 19,
            3, com.univapay.R.string.cvv),
    EMPTY("^$",
            com.univapay.R.drawable.ic_unknown,
            12, 19,
            3, com.univapay.R.string.cvv);

    /**
     * @return The regex matching this card type.
     */
    val pattern: Pattern

    /**
     * @return the locations where spaces should be inserted when formatting the card in a user
     * friendly way. Only for display purposes.
     */
    val spaceIndices: IntArray
        get() = if (this == AMERICAN_EXPRESS) AMERICAN_EXPRESS_SPACE_INDICES else DEFAULT_SPACE_INDICES

    init {
        pattern = Pattern.compile(regex)
    }

    /**
     * @param cardNumber The card number to validate.
     * @return `true` if this card number is locally valid.
     */
    fun validate(cardNumber: String): Boolean {
        if (TextUtils.isEmpty(cardNumber)) {
            return false
        }

        val numberLength = cardNumber.length
        if (numberLength < minCardLength || numberLength > maxCardLength) {
            return false
        } else if (!pattern.matcher(cardNumber).matches()) {
            return false
        }
        return isLuhnValid(cardNumber)
    }

    companion object {

        private val AMERICAN_EXPRESS_SPACE_INDICES = intArrayOf(4, 10)
        private val DEFAULT_SPACE_INDICES = intArrayOf(4, 8, 12)

        /**
         * Returns the card type matching this account, or [CardType.UNKNOWN]
         * for no match.
         *
         *
         * A partial account type may be given, with the caveat that it may not have enough digits to
         * match.
         */
        fun forCardNumber(cardNumber: String): CardType {
            for (cardType in values()) {
                if (cardType.pattern.matcher(cardNumber).matches()) {
                    return cardType
                }
            }
            return EMPTY
        }

        /**
         * Performs the Luhn check on the given card number.
         *
         * @param cardNumber a String consisting of numeric digits (only).
         * @return `true` if the sequence passes the checksum
         * @throws IllegalArgumentException if `cardNumber` contained a non-digit (where [ ][Character.isDefined] is `false`).
         * @see [Luhn Algorithm
        ](http://en.wikipedia.org/wiki/Luhn_algorithm) */
        fun isLuhnValid(cardNumber: String): Boolean {
            val reversed = StringBuffer(cardNumber).reverse().toString()
            val len = reversed.length
            var oddSum = 0
            var evenSum = 0
            for (i in 0 until len) {
                val c = reversed[i]
                if (!Character.isDigit(c)) {
                    throw IllegalArgumentException(String.format("Not a digit: '%s'", c))
                }
                val digit = Character.digit(c, 10)
                if (i % 2 == 0) {
                    oddSum += digit
                } else {
                    evenSum += digit / 5 + 2 * digit % 10
                }
            }
            return (oddSum + evenSum) % 10 == 0
        }
    }
}
