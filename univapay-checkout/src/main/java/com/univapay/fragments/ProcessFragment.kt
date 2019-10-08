package com.univapay.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.univapay.activities.CheckoutActivity
import com.univapay.models.*
import com.univapay.payments.CheckoutConfiguration
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.CreditCard
import com.univapay.sdk.models.common.KonbiniPayment
import com.univapay.sdk.models.response.transactiontoken.PhoneNumber
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.utils.CallbackIntents
import com.univapay.utils.Constants
import com.univapay.utils.bind
import com.univapay.views.ActionButtonStatus
import com.univapay.views.ButtonAction
import com.univapay.views.UnivapayViewModel

/**
 * Process Fragment used for Lollipop and above
 */
class ProcessFragment : UnivapayFragment.ProcessPaymentFragment() {

    override val FRAGMENT_TAG: String
        get() = this::class.java.name

    override fun display(checkout: CheckoutActivity, token: TransactionTokenWithDataParcel?, paymentType: PaymentTypeName?, exceedsThreshold: Boolean?, addToBackstack: Boolean) {
        val bnd = checkout.intent.extras

        bnd?.putParcelable(Constants.ARG_ADDRESS_DETAILS, checkout.mAddressDetails)
        bnd?.putParcelable(Constants.ARG_CARD_DETAILS, checkout.mCardDetails)
        bnd?.putParcelable(Constants.ARG_KONBINI_DETAILS, checkout.mKonbiniDetails)

        token?.let {
            bnd?.putParcelable(Constants.ARG_TRANSACTION_TOKEN_INFO, token)
        }

        exceedsThreshold?.let {
            bnd?.putBoolean(Constants.ARG_EXCEEDS_THRESHOLD, exceedsThreshold)
        }

        paymentType?.let {
            bnd?.putString(Constants.ARG_PAYMENT_TYPE, paymentType.name)
        }

        arguments = bnd

        checkout.supportFragmentManager
                .beginTransaction()
                .replace(com.univapay.R.id.fragment_container, this, FRAGMENT_TAG)
                .addToBackStack(null)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
    }

    override fun display(checkout: CheckoutActivity, paymentType: PaymentTypeName?, addToBackstack: Boolean) =
            display(checkout, null, paymentType, null)

    private var mCheckoutActivity: CheckoutActivity? = null
    private var progressStartTimeMillis: Long = 0
    private var isCompleteAnimationPending: Boolean = false
    private var isDownloading: Boolean = false
    private var processFragmentView: View? = null

    private val mSuccessReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Success Feedback
            onTransactionSuccess()
        }
    }

    private val mUndeterminedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Undetermined Feedback
            onTransactionUndetermined()
        }
    }

    private val mFailureReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Failure Feedback
            onTransactionFailure()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bm = LocalBroadcastManager.getInstance(context!!)
        bm.registerReceiver(mSuccessReceiver,
                IntentFilter(Constants.INTENT_PROCEED_SUCCESS))
        bm.registerReceiver(mUndeterminedReceiver,
                IntentFilter(Constants.INTENT_PROCEED_UNDETERMINED))
        bm.registerReceiver(mFailureReceiver,
                IntentFilter(Constants.INTENT_PROCEED_FAILURE))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(com.univapay.R.layout.fragment_process, container, false)

        val checkout = activity as CheckoutActivity?

        setupPaymentDetailsBar(checkout, ActionButtonStatus.Finish){
            checkout?.apply {
                broadcastFinalize()
                finish()
            }
        }

        // Set Action Button Text
        mCheckoutActivity = activity as CheckoutActivity?

        // Begin the Transaction
        processFragmentView = view

        processTransaction()

        return view
    }

    fun onTransactionBegin() {
        if (isCompleteAnimationPending) {
            return
        }

        processFragmentView?.let{fragmentView->
            // Update Message
            bind<AppCompatTextView>(fragmentView, com.univapay.R.id.text_view_progress_msg)?.text = getString(com.univapay.R.string.checkout_process_wait)

            // Disable UI
            setLockUI(true)

            if (!isDownloading) {
                swapAnimation(com.univapay.R.drawable.avd_progress)
                progressStartTimeMillis = System.currentTimeMillis()
            }
            isDownloading = !isDownloading
        }

    }

    fun onTransactionSuccess() = setProcessViewTransition(
            com.univapay.R.drawable.avd_success,
            com.univapay.R.string.checkout_process_success
    )

    fun onTransactionFailure() = setProcessViewTransition(
            com.univapay.R.drawable.avd_failed,
            com.univapay.R.string.checkout_process_failure
    )

    fun onTransactionUndetermined() = setProcessViewTransition(
            com.univapay.R.drawable.avd_undetermined,
            com.univapay.R.string.checkout_process_undetermined
    )

    private fun setProcessViewTransition(@DrawableRes animationRes: Int, @StringRes stringRes: Int){
        if (isCompleteAnimationPending) {
            return
        }

        if (isDownloading) {
            val delayMillis = 2666 - (System.currentTimeMillis() - progressStartTimeMillis) % 2666
            bind<AppCompatImageView>(processFragmentView!!, com.univapay.R.id.progress_animation)?.postDelayed({
                swapAnimation(animationRes)
                isCompleteAnimationPending = false

                view?.let {v->
                    val textProgressMessage: AppCompatTextView? = bind(v, com.univapay.R.id.text_view_progress_msg)

                    // Update Message
                    textProgressMessage?.text = getString(stringRes)
                    // Enable UI
                    (activity as CheckoutActivity?)?.setFinished()
                    setLockUI(false)
                }

            }, delayMillis)

            setLockUI(false)
            isCompleteAnimationPending = true
        }
    }

    private fun setLockUI(lock: Boolean) {

        val checkout = activity as CheckoutActivity?

        checkout?.findViewById<ButtonAction>(com.univapay.R.id.button_action)?.let { actionButton->

            checkout.mIsBackButtonOn = !lock
            actionButton.isEnabled = !lock
            checkout.supportActionBar?.setHomeButtonEnabled(!lock)
        }
    }

    private fun swapAnimation(@DrawableRes drawableResId: Int) {
        activity?.let{act->
            val drawable = AnimatedVectorDrawableCompat.create(act, drawableResId)
            val mProcessAnimation = bind<AppCompatImageView>(processFragmentView!!, com.univapay.R.id.progress_animation)
            mProcessAnimation?.setImageDrawable(drawable)
            val avd = drawable as Animatable?
            avd?.start()
        }
    }

    protected open fun processCardTransaction(config: CheckoutConfiguration<*>, univapay: UnivapaySDK, args: CheckoutArguments){
        // Get Arguments
        val cardDetails: CardDetails? = arguments?.getParcelable(Constants.ARG_CARD_DETAILS)
        val addressDetails: AddressDetails? = arguments?.getParcelable(Constants.ARG_ADDRESS_DETAILS)

        if(cardDetails == null){
            return
        } else {
            onTransactionBegin()

            val creditCard = CreditCard(
                    cardDetails.cardholderName,
                    cardDetails.cardNumber,
                    cardDetails.month!!, cardDetails.year!!,
                    cardDetails.security
            )

            if (args.isAddress) {
                creditCard.addAddress(
                        addressDetails?.country,
                        addressDetails?.state,
                        addressDetails?.city,
                        addressDetails?.line1,
                        addressDetails?.line2,
                        addressDetails?.postCode)
            }
            config.process(cardDetails.email ?: "", creditCard, univapay, context!!, args, cardDetails.rememberCard)
        }
    }

    protected open fun processKonbiniTransaction(config: CheckoutConfiguration<*>, univapay: UnivapaySDK, args: CheckoutArguments){
        val konbiniDetails: KonbiniDetails? = arguments?.getParcelable(Constants.ARG_KONBINI_DETAILS)

        konbiniDetails?.let {
            onTransactionBegin()

            val konbiniPayment = KonbiniPayment(
                    konbiniDetails.name,
                    konbiniDetails.convenienceStore,
                    konbiniDetails.expirationPeriod,
                    PhoneNumber(81, konbiniDetails.phoneNumber)
            )

            config.process(konbiniDetails.email ?: "", konbiniPayment, univapay, context!!, args, konbiniDetails.rememberKonbini)
        }
    }

    protected open fun processWithToken(transactionTokenParcel: TransactionTokenWithDataParcel, config: CheckoutConfiguration<*>, univapay: UnivapaySDK, args: CheckoutArguments) {
        config.settings?.let{settings->
            if(config.processFullPayment){
                onTransactionBegin()
                settings.processWithToken(transactionTokenParcel.transactionTokenInfo, univapay, context!!,  args)
            } else {
                exitWithoutAnimation(transactionTokenParcel)
            }
        }?: exitWithoutAnimation(transactionTokenParcel)
    }

    protected fun processTransaction() {
        val transactionTokenParcel: TransactionTokenWithDataParcel? = arguments?.getParcelable(Constants.ARG_TRANSACTION_TOKEN_INFO)
        arguments?.getString(Constants.ARG_PAYMENT_TYPE)?.let {pt->

            val config: CheckoutConfiguration<*> = UnivapayViewModel.instance.config
            val args: CheckoutArguments = UnivapayViewModel.instance.args
            val univapay: UnivapaySDK = UnivapayViewModel.instance.univapay

            if(transactionTokenParcel != null){
                processWithToken(transactionTokenParcel, config, univapay, args)
            } else {
                val paymentType: PaymentTypeName = PaymentTypeName.valueOf(pt)

                when(paymentType){
                    PaymentTypeName.CARD -> processCardTransaction(config, univapay, args)
                    PaymentTypeName.KONBINI -> processKonbiniTransaction(config, univapay, args)
                    else -> throw Exception("Payment type not supported")
                }
            }
        }
    }

    protected fun exitWithoutAnimation(transactionTokenParcel: TransactionTokenWithDataParcel){
        val checkout = activity as CheckoutActivity?
        CheckoutConfiguration.broadcastSuccess(CallbackIntents.TokenCallbackIntent.TokenSuccessCallback(), transactionTokenParcel, context!!)
        checkout?.broadcastFinalize()
        checkout?.finish()
    }
}
