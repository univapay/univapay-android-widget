package com.univapay.fragments

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.activities.CheckoutActivity
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.views.ActionButtonStatus
import com.univapay.views.ButtonAction
import com.univapay.views.UnivapayViewModel
import kotlinx.android.synthetic.main.layout_checkout_header.*

sealed class UnivapayFragment : Fragment() {

    abstract val FRAGMENT_TAG: String

    fun commitTransaction(checkout: CheckoutActivity, addToBackstack: Boolean){

        val transaction = checkout.supportFragmentManager
                .beginTransaction()
                .replace(com.univapay.R.id.fragment_container, this, FRAGMENT_TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

        if(addToBackstack){
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    abstract class NewPaymentFragment: UnivapayFragment(){
        abstract fun display(checkout: CheckoutActivity, paymentType: PaymentTypeName?, addToBackstack: Boolean = true)
        fun display(checkout: CheckoutActivity, addToBackstack: Boolean = true) = display(checkout, null, addToBackstack)

    }

    abstract class ProcessPaymentFragment: NewPaymentFragment(){
        abstract fun display(checkout: CheckoutActivity,
                             token: TransactionTokenWithDataParcel?,
                             paymentType: PaymentTypeName?,
                             exceedsThreshold: Boolean?,
                             addToBackstack: Boolean = true
        )
    }

    abstract class TokenPaymentFragment: UnivapayFragment(){
        abstract fun display(checkout: CheckoutActivity,
                             tokens: ArrayList<TransactionTokenWithDataParcel>,
                             paymentType: PaymentTypeName,
                             exceedsThreshold: Boolean?,
                             addToBackstack: Boolean = true
        )
    }

    companion object {

        /**
         * Sets the upper fragment containing the store's logo, title and description.
         */
        fun setupTopFragment(activity: CheckoutActivity){

            activity.apply {
                // Setup the Action Bar

                findViewById<Toolbar?>(com.univapay.R.id.toolbar)?.let{ toolbar->
                    setSupportActionBar(toolbar)

                    supportActionBar?.apply {
                        setDisplayHomeAsUpEnabled(true)
                        setHomeButtonEnabled(true)
                        setHomeAsUpIndicator(com.univapay.R.drawable.ic_close_white_24dp)
                    }
                }

                // Logo
                val resourceId = UnivapayViewModel.instance.args.imageResourceId
                if (resourceId > 0) {
                    image_view_checkout_logo?.setImageResource(resourceId)
                }

                // Title
                val title = UnivapayViewModel.instance.args.title
                if (TextUtils.isEmpty(title)) {
                    activity.findViewById<AppCompatTextView?>(com.univapay.R.id.text_view_checkout_title)
                            ?.visibility = View.GONE
                } else {
                    activity.findViewById<AppCompatTextView?>(com.univapay.R.id.text_view_checkout_title)
                            ?.text = title
                }
                // Description
                val description = UnivapayViewModel.instance.args.description
                if (TextUtils.isEmpty(description)) {
                    activity.findViewById<AppCompatTextView?>(com.univapay.R.id.text_view_checkout_description)
                            ?.visibility = View.GONE
                } else {
                    activity.findViewById<AppCompatTextView?>(com.univapay.R.id.text_view_checkout_description)
                            ?.text = description
                }

                val config = UnivapayViewModel.instance.config
                config.settings?.let {settings->
                    activity.findViewById<AppCompatTextView?>(com.univapay.R.id.text_view_checkout_other_data)
                            ?.text = settings.getDescription(resources)
                }
            }
        }

        fun setupPaymentDetailsBar(checkout: CheckoutActivity?,
                                   actionButtonStatus: ActionButtonStatus,
                                   onClickListener: (v: View) -> Unit
        ){
            checkout?.let {
                val buttonAction = checkout.findViewById<ButtonAction?>(com.univapay.R.id.button_action)
                buttonAction?.apply {
                    setActionButtonStatus(actionButtonStatus)
                    isEnabled = true
                    visibility = View.VISIBLE
                    setOnClickListener(onClickListener)
                }
            }
        }
    }
}
