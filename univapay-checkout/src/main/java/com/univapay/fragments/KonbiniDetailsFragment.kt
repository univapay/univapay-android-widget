package com.univapay.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.Spinner
import com.univapay.sdk.types.Konbini
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.activities.CheckoutActivity
import com.univapay.adapters.KonbiniAdapter
import com.univapay.adapters.KonbiniSpinnerAdapter
import com.univapay.models.KonbiniDetails
import com.univapay.utils.Constants
import com.univapay.utils.bindNotNullable
import com.univapay.views.*
import kotlinx.android.synthetic.main.fragment_konbini_form.*

class KonbiniDetailsFragment: UnivapayFragment.NewPaymentFragment(){

    override val FRAGMENT_TAG: String
        get() = this::class.java.name
    private lateinit var mEmailEditText: EmailEditText
    private lateinit var mNameEditText: CustomEditText
    private lateinit var mPhoneEditText: PhoneEditText
    private lateinit var mKonbiniSpinner: Spinner
    private lateinit var rememberKonbiniCheckBox: CheckBox

    val konbiniDetails: KonbiniDetails
        get() = KonbiniDetails(
                mNameEditText.text.toString(),
                mEmailEditText.text.toString(),
                mPhoneEditText.text.toString(),
                (mKonbiniSpinner.selectedItem as KonbiniAdapter).konbini,
                UnivapayViewModel.instance.args.konbiniExpirationPeriod,
                rememberKonbiniCheckBox.isChecked
        )

    val isValid: Boolean
        get() = mNameEditText.isValid &&
                mEmailEditText.isValid &&
                mPhoneEditText.isValid &&
                konbiniDetails.convenienceStore != null

    override fun display(checkout: CheckoutActivity, paymentType: PaymentTypeName?, addToBackstack: Boolean) {

        val bnd = checkout.intent.extras
        bnd.putParcelable(Constants.ARG_KONBINI_DETAILS, checkout.mKonbiniDetails)
        arguments = bnd

        commitTransaction(checkout, addToBackstack)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val checkout = activity as CheckoutActivity?

        // Inflate the layout for this fragment
        val view = inflater.inflate(com.univapay.R.layout.fragment_konbini_form, container, false)

        val args = UnivapayViewModel.instance.args

        // Set Action Button Text
        UnivapayFragment
                .setupPaymentDetailsBar(
                        checkout,
                        ActionButtonStatus.Pay){
                    validate()
                    if(isValid){
                        checkout?.apply {
                            mKonbiniDetails = konbiniDetails
                            ProcessFragment().display(checkout, PaymentTypeName.KONBINI)
                        }
                    }
                }

        // Get the views
        mEmailEditText = bindNotNullable(view, com.univapay.R.id.konbini_edit_text_email)
        mNameEditText = bindNotNullable(view, com.univapay.R.id.konbini_edit_text_name)
        mPhoneEditText = bindNotNullable(view, com.univapay.R.id.konbini_edit_phone_local_number)

        // Spinner setup
        mKonbiniSpinner = bindNotNullable(view, com.univapay.R.id.konbini_spinner)
        mKonbiniSpinner.adapter = KonbiniSpinnerAdapter(context, com.univapay.R.layout.view_konbini_spinner_element)
        mKonbiniSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                konbiniDetails.convenienceStore = null
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                konbiniDetails.convenienceStore = KonbiniAdapter.values()[position].konbini
            }
        }

        rememberKonbiniCheckBox = bindNotNullable(view, com.univapay.R.id.check_can_remember_konbini_data)

        // Hide the `Remember my Card` checkbox if remembering cards is not enabled or the transaction is a subscription.
        if(!args.canRememberCards()){
            rememberKonbiniCheckBox.visibility = View.GONE
        }

        // Populate data
        val konbiniDetails = arguments?.getParcelable<KonbiniDetails>(Constants.ARG_KONBINI_DETAILS)
        konbiniDetails?.apply{
            mNameEditText.setText(name)
            mEmailEditText.setText(email)
            mPhoneEditText.setText(phoneNumber)
            convenienceStore?.let{store->
                Konbini.values().indexOf(store)
            }
        }

        return view
    }

    fun validate() {
        konbini_edit_text_name.validate()
        konbini_edit_text_email.validate()
        konbini_edit_phone_local_number.validate()
    }
}
