package com.univapay.fragments

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import com.univapay.sdk.models.common.CreditCard
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.sdk.types.TransactionTokenType
import com.univapay.activities.CheckoutActivity
import com.univapay.models.CardDetails
import com.univapay.models.CardType
import com.univapay.utils.Constants
import com.univapay.utils.bindNotNullable
import com.univapay.views.*
import kotlinx.android.synthetic.main.fragment_card_detail.*

/**
 * Card Detail Fragment
 */
class CardDetailFragment : UnivapayFragment.NewPaymentFragment() {

    override val FRAGMENT_TAG: String
        get() = this::class.java.name

    private lateinit var mEmailEditText: EmailEditText
    private lateinit var mCardholderNameEditText: CustomEditText
    private lateinit var mCardNumberEditText: CardNumberEditText
    private lateinit var mSecurityEditText: SecurityEditText
    private lateinit var mExpiryEditText: ExpiryEditText
    private lateinit var mRememberCardCheckBox: CheckBox
    private var mShouldRequireCvv: Boolean = true

    override fun display(checkout: CheckoutActivity, paymentType: PaymentTypeName?, addToBackstack: Boolean) {

        val bnd = checkout.intent.extras
        bnd.putParcelable(Constants.ARG_CARD_DETAILS, checkout.mCardDetails)
        arguments = bnd

        commitTransaction(checkout, addToBackstack)
    }

    val cardDetails: CardDetails
        get() = CardDetails(
                mEmailEditText.text.toString(),
                mCardholderNameEditText.text.toString(),
                mCardNumberEditText.cardNumber,
                if(mSecurityEditText.visibility == View.VISIBLE) Integer.parseInt(mSecurityEditText.text.toString()) else null,
                Integer.parseInt(mExpiryEditText.month),
                Integer.parseInt(mExpiryEditText.year),
                mRememberCardCheckBox.isChecked,
                mShouldRequireCvv
        )

    val isValid: Boolean
        get() = (mEmailEditText.isValid &&
                mCardholderNameEditText.isValid &&
                mCardNumberEditText.isValid &&
                (if(mSecurityEditText.visibility == View.VISIBLE) mSecurityEditText.isValid else true) &&
                mExpiryEditText.isValid)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val checkout = activity as CheckoutActivity?

        val cardDetailsView = inflater.inflate(com.univapay.R.layout.fragment_card_detail, container, false)

        checkout?.let {
            UnivapayFragment.setupTopFragment(checkout)
        }

        // Set Action Button Text
        UnivapayFragment
                .setupPaymentDetailsBar(checkout,
                        ActionButtonStatus.Pay
                ){
                    validate()
                    if (isValid) {
                        checkout?.apply {
                            mCardDetails = cardDetails

                            mCardDetails.apply{
                                val creditCard = CreditCard(
                                        cardholderName,
                                        cardNumber,
                                        month!!,
                                        year!!,
                                        security
                                )

                                // Check if we need to pass the AddressDetails
                                val viewModel = UnivapayViewModel.instance
                                if (viewModel.args.isAddress) {
                                    creditCard.addAddress(
                                            mAddressDetails.country,
                                            mAddressDetails.state,
                                            mAddressDetails.city,
                                            mAddressDetails.line1,
                                            mAddressDetails.line2,
                                            mAddressDetails.postCode)
                                }
                                // If the token is recurring, and the user has not checked `Remember My Card`, a dialog should request for permission.
                                if(viewModel.config.transactionTokenType == TransactionTokenType.RECURRING && !mCardDetails.rememberCard){
                                    val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(checkout)
                                    val consentMessage = resources.getString(com.univapay.R.string.checkout_recurring_token_consent)

                                    alertDialogBuilder
                                            .setTitle(com.univapay.R.string.checkout_recurring_token_permission_message)
                                            .setMultiChoiceItems(arrayOf(consentMessage), null){dialog, _, isChecked ->
                                                val alertDialog = dialog as AlertDialog
                                                if(isChecked){
                                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true)
                                                } else {
                                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false)
                                                }
                                            }
                                            .setPositiveButton(
                                                    com.univapay.R.string.checkout_recurring_token_permission_positive_button){ _, _->
                                                ProcessFragment().display(checkout, PaymentTypeName.CARD)
                                            }
                                            .setNegativeButton(
                                                    com.univapay.R.string.checkout_recurring_token_permission_negative_button){ _, _->
                                                // Do nothing unless an explicit permission is given
                                            }

                                    val dialog = alertDialogBuilder
                                            .create()

                                    dialog.setOnShowListener{dialogInterface->
                                        (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false)
                                    }

                                    dialog.show()

                                } else {
                                    ProcessFragment().display(checkout, PaymentTypeName.CARD)
                                }
                            }
                        }

                    }
                }

        val args = UnivapayViewModel.instance.args

        // Get the views:
        mEmailEditText = bindNotNullable(cardDetailsView, com.univapay.R.id.card_detail_edit_text_email)
        mCardholderNameEditText = bindNotNullable(cardDetailsView, com.univapay.R.id.card_detail_edit_text_cardholder_name)
        mCardNumberEditText = bindNotNullable(cardDetailsView, com.univapay.R.id.card_detail_edit_text_card_number)
        mSecurityEditText = bindNotNullable(cardDetailsView, com.univapay.R.id.card_detail_edit_text_security)
        mExpiryEditText = bindNotNullable(cardDetailsView, com.univapay.R.id.card_detail_edit_text_expiry)
        mRememberCardCheckBox = bindNotNullable(cardDetailsView, com.univapay.R.id.check_can_remember_card)


        // Connect Card Number with Security
        card_detail_edit_text_card_number?.setOnCardTypeChangedListener{cardType: CardType ->
            card_detail_edit_text_security?.setCardType(cardType)
        }

        // Hide the `Remember my Card` checkbox if remembering cards is not enabled or the transaction is a subscription.
        if(!args.canRememberCards()){
            mRememberCardCheckBox.visibility = View.GONE
        }

        // Populate Data
        val cardDetails = arguments?.getParcelable<CardDetails>(Constants.ARG_CARD_DETAILS)
        cardDetails?.let {
            mShouldRequireCvv = cardDetails.requireCvv
            mEmailEditText.setText(cardDetails.email)
            mCardholderNameEditText.setText(cardDetails.cardholderName)
            mCardNumberEditText.setText(cardDetails.cardNumber)
            if (cardDetails.security != -1) {
                mSecurityEditText.setText(String.format("%s", cardDetails.security))
            }
            if (cardDetails.month != -1 && cardDetails.year != -1) {
                mExpiryEditText.setValue(cardDetails.year!!, cardDetails.month!!)
            }
        }

        // Don't require a CVV unless it's necessary
        if(!mShouldRequireCvv){
            mSecurityEditText.visibility = View.GONE
        }

        return cardDetailsView
    }

    fun validate() {
        mEmailEditText.validate()
        mCardholderNameEditText.validate()
        mCardNumberEditText.validate()
        mExpiryEditText.validate()
        if(mSecurityEditText.visibility == View.VISIBLE) mSecurityEditText.validate()
    }
}
