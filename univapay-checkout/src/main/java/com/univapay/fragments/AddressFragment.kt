package com.univapay.fragments

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.activities.CheckoutActivity
import com.univapay.models.AddressDetails
import com.univapay.models.CheckoutData
import com.univapay.utils.Constants
import com.univapay.utils.FunctionalUtils
import com.univapay.views.ActionButtonStatus
import com.univapay.views.UnivapayViewModel
import kotlinx.android.synthetic.main.fragment_address.*

/**
 * Address Fragment
 */
class AddressFragment : UnivapayFragment.NewPaymentFragment() {

    override val FRAGMENT_TAG: String
        get() = this::class.java.name

    override fun display(checkout: CheckoutActivity, paymentType: PaymentTypeName?, addToBackstack: Boolean) {
        val bnd = checkout.intent.extras
        bnd.putParcelable(Constants.ARG_ADDRESS_DETAILS, checkout.mAddressDetails)
        arguments = bnd

        commitTransaction(checkout, addToBackstack)
    }

    val addressDetails: AddressDetails
        get() = AddressDetails(
                address_edit_text_name.text.toString(),
                address_edit_text_line1.text.toString(),
                address_edit_text_line2.text.toString(),
                address_edit_text_city.text.toString(),
                address_edit_text_state.text.toString(),
                address_edit_text_country.countryCode,
                address_edit_text_post_code.text.toString()
        )

    val isValid: Boolean
        get() = address_edit_text_name.isValid &&
                address_edit_text_line1.isValid &&
                address_edit_text_line2.isValid &&
                address_edit_text_city.isValid &&
                address_edit_text_state.isValid &&
                address_edit_text_country.isValid &&
                address_edit_text_post_code.isValid



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val checkout = activity as CheckoutActivity?

        // Inflate the layout for this fragment
        val view = inflater.inflate(com.univapay.R.layout.fragment_address, container, false)

        // Set Action Button Text
        UnivapayFragment
                .setupPaymentDetailsBar(
                        checkout,
                        ActionButtonStatus.Next
                ){
                    validate()
                    if (isValid) {
                        checkout?.apply {
                            mAddressDetails = addressDetails
                            // Move Next

                            val viewModel = UnivapayViewModel.instance
                            UnivapayViewModel.instance.checkoutData.observe(this, Observer{ data: FunctionalUtils.Either<Throwable, CheckoutData>? ->

                                val checkoutData = data as FunctionalUtils.Either.Right<CheckoutData>

                                val requireCvv = viewModel.shouldRequireCvv(checkoutData.value.checkoutInfo, checkoutData.value.exceedsThreshold)
                                mCardDetails.requireCvv = requireCvv
                                CardDetailFragment().display(checkout)
                            })
                        }
                    }
                }

        // Populate Data
        val addressDetails = arguments?.getParcelable<AddressDetails>(Constants.ARG_ADDRESS_DETAILS)
        addressDetails?.apply{
            address_edit_text_line1?.setText(line1)
            address_edit_text_name?.setText(name)
            address_edit_text_line2?.setText(line2)
            address_edit_text_city?.setText(city)
            address_edit_text_state?.setText(state)
            address_edit_text_country?.setText(country)
            address_edit_text_post_code?.setText(postCode)
        }

        return view
    }

    fun validate() {
        address_edit_text_name.validate()
        address_edit_text_line1.validate()
        address_edit_text_line2.validate()
        address_edit_text_city.validate()
        address_edit_text_state.validate()
        address_edit_text_country.validate()
        address_edit_text_post_code.validate()
    }
}
