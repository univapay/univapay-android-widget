package com.univapay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager
import com.univapay.UnivapayCheckout.Builder
import com.univapay.activities.CheckoutActivity
import com.univapay.models.*
import com.univapay.payments.CheckoutConfiguration
import com.univapay.payments.RecurringTokenCheckout
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.MetadataMap
import com.univapay.sdk.types.RecurringTokenInterval
import com.univapay.sdk.utils.MetadataAdapter
import com.univapay.utils.CallbackIntents.ChargeCallbackIntent
import com.univapay.utils.CallbackIntents.InitializationCallbackIntent
import com.univapay.utils.CallbackIntents.SubscriptionCallbackIntent
import com.univapay.utils.CallbackIntents.TokenCallbackIntent
import com.univapay.utils.Constants
import com.univapay.utils.unlockUI
import org.joda.time.Period
import java.net.URI
import java.util.*

/**
 * The class of the main client. See the Builder static class to learn how to setup and instantiate
 * the checkout client.
 * @see Builder
 */
class UnivapayCheckout private constructor(private val mCheckoutArguments: CheckoutArguments,
                                           private val mCheckoutTokenCallback: CheckoutTokenCallback?,
                                           private val mCheckoutChargeCallback: CheckoutChargeCallback?,
                                           private val mCheckoutSubscriptionCallback: CheckoutSubscriptionCallback?,
                                           private val mCheckoutConfiguration: CheckoutConfiguration<*>) {


    private fun getInitializationReceiver(initializationCallback: CheckoutInitializationCallback): BroadcastReceiver{
        return object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent) {
                initializationCallback.let{ callback->
                    val initializationCallbackType: InitializationCallbackIntent = intent.getParcelableExtra(Constants.ARG_CHECKOUT_CALLBACK_TYPE)
                    val error: UnivapayError?
                    when(initializationCallbackType){
                        is InitializationCallbackIntent.InitializationSuccessCallback -> {
                            callback.onSuccess()
                        }
                        is InitializationCallbackIntent.InitializationFailureCallback -> {
                            error = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_ERROR)
                            callback.onFailure(error)
                        }
                    }
                }
            }
        }
    }

    private val mCheckoutChargeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val chargeCallbackType: ChargeCallbackIntent = intent.getParcelableExtra(Constants.ARG_CHECKOUT_CALLBACK_TYPE)
            val chargeParcel: ChargeParcel?
            val error: UnivapayError?
            when (chargeCallbackType) {
                is ChargeCallbackIntent.ChargeSuccessCallback -> {
                    chargeParcel = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_CHARGE_DETAILS)
                    unlockUI(Constants.INTENT_PROCEED_SUCCESS)
                    mCheckoutChargeCallback?.onSuccess(chargeParcel?.charge)
                }
                is ChargeCallbackIntent.ChargeFailureCallback -> {
                    error = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_ERROR)
                    unlockUI(Constants.INTENT_PROCEED_FAILURE)
                    mCheckoutChargeCallback?.onError(error)
                }
                is ChargeCallbackIntent.ChargeUndeterminedCallback -> {
                    chargeParcel = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_CHARGE_DETAILS)
                    unlockUI(Constants.INTENT_PROCEED_UNDETERMINED)
                    mCheckoutChargeCallback?.onUndetermined(chargeParcel?.charge)
                }
            }
        }
    }

    private val mCheckoutSubscriptionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val subscriptionCallbackType: SubscriptionCallbackIntent = intent.getParcelableExtra(Constants.ARG_CHECKOUT_CALLBACK_TYPE)
            val subscriptionParcel: SubscriptionParcel?
            val error: UnivapayError?

            when (subscriptionCallbackType) {
                is SubscriptionCallbackIntent.SubscriptionSuccessCallback -> {
                    subscriptionParcel = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_SUBSCRIPTION_DETAILS)
                    unlockUI(Constants.INTENT_PROCEED_SUCCESS)
                    mCheckoutSubscriptionCallback?.onSuccess(subscriptionParcel.subscription)
                }
                is SubscriptionCallbackIntent.SubscriptionFailureCallback -> {
                    error = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_ERROR)
                    unlockUI(Constants.INTENT_PROCEED_FAILURE)
                    mCheckoutSubscriptionCallback?.onError(error)
                }
                is SubscriptionCallbackIntent.SubscriptionUndeterminedCallback -> {
                    subscriptionParcel = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_SUBSCRIPTION_DETAILS)
                    unlockUI(Constants.INTENT_PROCEED_UNDETERMINED)
                    mCheckoutSubscriptionCallback?.onUndetermined(subscriptionParcel?.subscription)
                }
            }
        }
    }

    private val mCheckoutTokenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val tokenCallbackType: TokenCallbackIntent = intent.getParcelableExtra(Constants.ARG_CHECKOUT_CALLBACK_TYPE)
            val tokenParcel: TransactionTokenWithDataParcel?
            val error: UnivapayError?

            when(tokenCallbackType){
                is TokenCallbackIntent.TokenSuccessCallback -> {
                    tokenParcel = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_TOKEN_DETAILS)
                    if(!mCheckoutConfiguration.processFullPayment){
                        unlockUI(Constants.INTENT_PROCEED_SUCCESS)
                    }
                    mCheckoutTokenCallback?.onSuccess(tokenParcel?.transactionTokenInfo)

                }
                is TokenCallbackIntent.TokenFailureCallback -> {
                    error = intent.extras.getParcelable(Constants.ARG_CHECKOUT_CALLBACK_ERROR)
                    unlockUI(Constants.INTENT_PROCEED_FAILURE)
                    mCheckoutTokenCallback?.onError(error)
                }
            }
        }
    }

    private val mCheckoutFinalizeReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finalize()
        }
    }

    /**
     * Displays the [UnivapayCheckout] dialog with the mCheckoutArguments
     * supplied to the builder. The initialization callback after the checkout
     * widget attempts to retrieve the merchant's configuration from the UnivaPay API. The `onError`
     * callback gets called whenever the API cannot be reached due to connection issues or in the case
     * the merchant does not set its credentials properly. Setting up the checkout widget in a way
     * that is inconsistent with the merchant's settings.
     * @see CheckoutInitializationCallback
     */
    fun show(initializationCallback: CheckoutInitializationCallback?) {
        // Launch the Intent to build the ChargeParcel
        val appContext = UnivapayApplication.context

        // Register Local Broadcast Receivers
        val bm = LocalBroadcastManager.getInstance(appContext)
        bm.registerReceiver(mCheckoutTokenReceiver, IntentFilter(Constants.INTENT_CHECKOUT_TOKEN_CALLBACK))
        bm.registerReceiver(mCheckoutChargeReceiver, IntentFilter(Constants.INTENT_CHECKOUT_CHARGE_CALLBACK))
        bm.registerReceiver(mCheckoutSubscriptionReceiver, IntentFilter(Constants.INTENT_CHECKOUT_SUBSCRIPTION_CALLBACK))
        bm.registerReceiver(mCheckoutFinalizeReceiver, IntentFilter(Constants.INTENT_CHECKOUT_FINALIZE))

        initializationCallback?.let {callback->
            bm.registerReceiver(
                    getInitializationReceiver(callback),
                    IntentFilter(Constants.INTENT_CHECKOUT_INITIALIZATION_CALLBACK)
            )
        }

        // Create Charge Intent
        val intent = Intent(appContext, CheckoutActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        // Add Data
        intent.putExtra(Constants.ARG_CHECKOUT_ARGUMENTS, mCheckoutArguments)
        intent.putExtra(Constants.ARG_CHECKOUT_CONFIGURATION, mCheckoutConfiguration)

        appContext.startActivity(intent)
    }

    /**
     * Displays the [UnivapayCheckout] dialog with the mCheckoutArguments
     * supplied to the builder. The initialization callback after the checkout
     * widget attempts to retrieve the merchant's configuration from the UnivaPay API. The `onError`
     * callback gets called whenever the API cannot be reacheds due to connection issues or in the case
     * the merchant does not set its credentials properly. Setting up the checkout widget in a way
     * that is inconsistent with the merchant's settings. The `onSuccess` callback is executed
     * whenever the widget gets initialized properly.
     * @see CheckoutInitializationCallback
     */
    fun show(onSuccessCallback: () -> Unit, onErrorCallback: (error: UnivapayError?)-> Unit){
        val callback = object: CheckoutInitializationCallback{
            override fun onSuccess() = onSuccessCallback()
            override fun onFailure(error: UnivapayError?) = onErrorCallback(error)
        }
        show(callback)
    }

    /**
     * Displays the [UnivapayCheckout] dialog and triggers the code in the `onSuccessCallback` function.
     */
    fun show(onSuccessCallback: () -> Unit){
        show(onSuccessCallback, {})
    }

    /**
     * Display the [UnivapayCheckout] without passing an initialization callback.
     */
    fun show(){
        show(null)
    }

    /**
     * Unregisters the receivers associated with the callbacks passed to the widget. This function
     * is called when the widget's main activity is finalized.
     * @see CheckoutActivity
     *
     */
    @Throws(Throwable::class)
    internal fun finalize() {
        val appContext = UnivapayApplication.context
        // Unregister Receivers
        val bm = LocalBroadcastManager.getInstance(appContext)
        bm.unregisterReceiver(mCheckoutTokenReceiver)
        bm.unregisterReceiver(mCheckoutChargeReceiver)
        bm.unregisterReceiver(mCheckoutSubscriptionReceiver)
    }

    /**
     * A builder class used to setup and instantiate the checkout widget.
     */
    class Builder
    /**
     * Creates a builder
     * @param appCredentials: set here your store's　Application Json Web Token (App JWT).
     * Only the public application ID is necessary.
     * @param money: an instance of [MoneyLike] with the
     * @param checkoutConfiguration
     * @return an instance of [UnivapayCheckout]
     * @see AppCredentials
     * @see MoneyLike
     * @see CheckoutConfiguration
     */
    (
            private val appCredentials: AppCredentials,
            private val checkoutConfiguration: CheckoutConfiguration<*>
    ) {
        private val mCheckoutArguments: CheckoutArguments = CheckoutArguments(appCredentials)
        private var mCheckoutChargeCallback: CheckoutChargeCallback? = null
        private var mCheckoutSubscriptionCallback: CheckoutSubscriptionCallback? = null
        private var mCheckoutTokenCallback: CheckoutTokenCallback? = null

        init{
//          Perform constructor parameters validation here
            if(appCredentials.appId.isEmpty()) throw Exception("Your App ID cannot be empty and must be a valid Application JWT")
        }

        /**
         * Set whether the billing address is required.
         * @param requireAddress
         * @return a [Builder] instance
         */
        fun setAddress(requireAddress: Boolean): Builder {
            mCheckoutArguments.isAddress = requireAddress
            return this
        }


        /**
         * Set the Image resource ID to be used as a logo.
         * @param resId: your logo's resource ID
         * @return a [Builder] instance
         */
        fun setImageResource(resId: Int): Builder {
            mCheckoutArguments.imageResourceId = resId
            return this
        }

        /**
         * Set the title displayed in the [UnivapayCheckout].
         * @param title: a title to display in the main checkout view
         * @return a [Builder] instance
         */
        fun setTitle(title: String): Builder {
            mCheckoutArguments.title = title
            return this
        }

        /**
         * Set the description in the [UnivapayCheckout]
         * @param description
         * @return a [Builder] instance
         */
        fun setDescription(description: String): Builder {
            mCheckoutArguments.description = description
            return this
        }

        /**
         * Pass some custom Metadata to be attached to token and payment requests.
         * @param metadata: an instance of [MetadataMap].
         * @return a [Builder] instance
         * @see MetadataAdapter
         */
        fun setMetadata(metadata: MetadataMap): Builder {
            mCheckoutArguments.metadata = metadata
            return this
        }

        /**
        * Pass some custom object along with a [MetadataAdapter] to attach it as metdata.
        * @param metadata: an instance of any class you wish to serialize
        * @param adapter: a [MetadataAdapter] capable of converting your type to a [MetadataMap]
        * @return a [Builder] instance
        * @see MetadataAdapter
        */
        fun <T> setMetadata(metadata: T, adapter: MetadataAdapter<T>): Builder{
            mCheckoutArguments.metadata = adapter.serialize(metadata)
            return this
        }


        /**
         * By passing a customer ID, the checkout widget includes a checkbox that allows the customer
         * to decide whether his payment (card, convenience store details, etc.) data can be turned
         * into a recurring token (regardless of the checkout configuration specified by the merchant).
         * If the customer agrees, and the token creation is successful, the tokenized card will be
         * presented to the user in the future for easy payments without the need to introduce again
         * the stored payment data.
         * @param customerId: this can come from your database. In case your service does not manage
         * customer IDs as UUIDs, the UnivaPay API allows you to map your own customer ID to a unique
         * UUID you can use. Be sure to check the out the docs at https://docs.univapay.com/
         * @return a [Builder] instance
         */
        fun rememberCardsForUser(customerId: UUID): Builder {
            mCheckoutArguments.customerId = customerId
            return this
        }

        /**
         * @param customerId: this can come from your database. In case your service does not manage
         * customer IDs as UUIDs, the UnivaPay API allows you to map your own customer ID to a unique
         * UUID you can use. Be sure to check the out the docs at https://docs.univapay.com/
         * @param useDisplayQRMode: if this is true, a QR code is displayed directly with choosing
         * a card or a convenience store.
         * @return a [Builder] instance
         */
        fun rememberCardsForUser(customerId: UUID, useDisplayQRMode: Boolean): Builder {
            mCheckoutArguments.customerId = customerId
            mCheckoutArguments.useDisplayQrMode = useDisplayQRMode
            return this
        }

        /**
         * Sets the usage limit for recurring tokens created by the checkout widget. This includes
         * tokens created by [RecurringTokenCheckout] and [RecurringTokenPaymentCheckout]
         * configurations, as well as recurring tokens created whenever the user chooses to store
         * their payment data when a customerId is set.
         * @param recurringTokenUsageLimit: sets the usage limit for recurring tokens created by the widget
         * @return a [Builder] instance
         */
        fun setRecurringTokenUsageLimit(recurringTokenUsageLimit: RecurringTokenInterval): Builder {
            mCheckoutArguments.recurringTokenUsageLimit = recurringTokenUsageLimit
            return this
        }

        /**
         * Forces the use of a specific payment method. If used, the payment type selection screen is
         * not displayed. If the selected payment method is not enabled in the Store's configuration,
         * a service error screen is displayed.
         * @param paymentMethod: any of the payment methods supported by the widget.
         * @return a [Builder] instance
         * @see SupportedPaymentMethod
         */
        fun forcePaymentMethod(paymentMethod: SupportedPaymentMethod): Builder{
            mCheckoutArguments.paymentMethod = paymentMethod
            return this
        }

        /**
        * Configure the way QR codes are displayed, such as the color and the display mode of the store's logo
        * @param qrOptions
        * @return a [Builder] instance
        * @see QROptions
        */
        fun setQROptions(qrOptions: QROptions): Builder{
            mCheckoutArguments.qrOptions = qrOptions
            return this
        }

        /**
         * Set the Token Callback
         * @param checkoutTokenCallback a set of callbacks to handle token creation events.
         * This set of callbacks get executed whether your checkout configuration is full payment or
         * token-only.
         * @return a [Builder] instance
         */
        fun setTokenCallback(checkoutTokenCallback: CheckoutTokenCallback): Builder {
            this.mCheckoutTokenCallback = checkoutTokenCallback
            return this
        }

        /**
         * Set the `onSuccess` and `onError` callback
         * @param onSuccessCallback: callback to be triggered whenever the checkout widget successfully
         * creates a transaction token.
         * @param onErrorCallback: callback that gets triggered in case of some error when attempting to
         * create a transaction token.
         * @return a [Builder] instance
         */
        fun setTokenCallback(onSuccessCallback: (transactionTokenInfo: TransactionTokenWithData?)-> Unit,
                             onErrorCallback: (error: UnivapayError?) -> Unit): Builder {
            this.mCheckoutTokenCallback = object: CheckoutTokenCallback{
                override fun onSuccess(transactionTokenWithData: TransactionTokenWithData?) = onSuccessCallback(transactionTokenWithData)
                override fun onError(error: UnivapayError?) = onErrorCallback(error)
            }
            return this
        }

        /**
         * Set Checkout Charge Callback.
         * @param checkoutChargeCallback a set of callbacks to handle charge creation events.
         * This set of callbacks gets executed independently of whether a recurring token is
         * reused to process the payment, or whether a new token gets created. Its execution is
         * independent from whether a Token Callback has been set.
         *
         * @return a [Builder] instance
         */
        fun setChargeCallback(checkoutChargeCallback: CheckoutChargeCallback): Builder {
            this.mCheckoutChargeCallback = checkoutChargeCallback
            return this
        }

        /**
         * Set the `onSuccess` and `onError` callback
         * @param onSuccessCallback: callback to be triggered whenever the checkout widget successfully
         * creates a charge.
         * @param onErrorCallback: callback that gets triggered in case of some error when attempting to
         * create a charge.
         * @param onUndeterminedCallback: callback to handle cases where the status of the created
         * charge could not be determined.
         *
         * @return a [Builder] instance
         */
        fun setChargeCallback(onSuccessCallback: (charge: Charge?)-> Unit,
                              onErrorCallback: (error: UnivapayError?)-> Unit,
                              onUndeterminedCallback: (charge: Charge?)-> Unit): Builder {

            this.mCheckoutChargeCallback = object: CheckoutChargeCallback{
                override fun onSuccess(charge: Charge?) = onSuccessCallback(charge)
                override fun onError(error: UnivapayError?) = onErrorCallback(error)
                override fun onUndetermined(charge: Charge?) = onUndeterminedCallback(charge)
            }
            return this
        }

        /**
         * Set Checkout Charge Callback.
         * @param checkoutSubscriptionCallback a set of callbacks to handle subscription creation events.
         * This set of callbacks gets executed independently of whether a recurring token is
         * reused to process the payment, or whether a new token gets created. Its execution is
         * independent from whether a Token Callback has been set.
         *
         * @return a [Builder] instance
         */
        fun setSubscriptionCallback(checkoutSubscriptionCallback: CheckoutSubscriptionCallback): Builder {
            this.mCheckoutSubscriptionCallback = checkoutSubscriptionCallback
            return this
        }


        /**
         * Set the `onSuccess` and `onError` callback
         * @param onSuccessCallback: callback to be triggered whenever the checkout widget successfully
         * creates a subscription.
         * @param onErrorCallback: callback that gets triggered in case of some error when attempting to
         * create a subscription.
         * @param onUndeterminedCallback: callback to handle cases where the status of the created
         * subscription could not be determined.
         *
         * @return a [Builder] instance
         */
        fun setSubscriptionCallback(onSuccessCallback: (subscription: FullSubscription?) -> Unit,
                                    onErrorCallback: (error: UnivapayError?) -> Unit,
                                    onUndeterminedCallback: (subscription: FullSubscription?) -> Unit
        ): Builder {
            this.mCheckoutSubscriptionCallback = object: CheckoutSubscriptionCallback{
                override fun onSuccess(subscription: FullSubscription?) = onSuccessCallback(subscription)
                override fun onError(error: UnivapayError?) = onErrorCallback(error)
                override fun onUndetermined(subscription: FullSubscription?) = onUndeterminedCallback(subscription)
            }
            return this
        }

        fun setConvenienceStorePaymentExpirationDate(expirationPeriod: Period): Builder{
            this.mCheckoutArguments.konbiniExpirationPeriod = expirationPeriod
            return this
        }

        fun setCvvRequired(cvvRequired: Boolean): Builder{
            mCheckoutArguments.cvvRequired = cvvRequired
            return this
        }

        fun setEndpoint(endpoint: URI): Builder{
            mCheckoutArguments.endpoint = endpoint
            return this
        }

        fun setTimeout(seconds: Long): Builder{
            mCheckoutArguments.timeout = seconds
            return this
        }

        /**
         * Must set an origin to use for a request header.
         * @param origin: an origin URI to be attached to the requests sent by the checkout widget.
         * It defaults to a URI formed with your application's package as defined in your Android manifest.
         * If you set this parameter, be sure to set this origin when creating your store's Application
         * JWT.
         *
         * @return a [Builder] instance
         */
        fun setOrigin(origin: URI): Builder{
            mCheckoutArguments.origin = origin
            return this
        }

        /**
         * Creates an [UnivapayCheckout] with the mCheckoutArguments supplied to this
         * builder.
         *
         * Calling this method does not display the dialog. If no additional
         * processing is needed, [.show] may be called instead to both
         * build and display the checkout dialog.
         *
         * @return a [UnivapayCheckout] instance
         */
        fun build(): UnivapayCheckout {
            return UnivapayCheckout(
                    mCheckoutArguments,
                    mCheckoutTokenCallback,
                    mCheckoutChargeCallback,
                    mCheckoutSubscriptionCallback,
                    checkoutConfiguration)
        }

        /**
         * Creates an [UnivapayCheckout] with the mCheckoutArguments supplied to this
         * builder and immediately displays the dialog.
         *
         * Calling this method is functionally identical to:
         * <pre>
         * UnivapayCheckout checkout = builder.build();
         * checkout.show();
        </pre> *
         */
        fun show(): UnivapayCheckout {
            val dialog = build()
            dialog.show()
            return dialog
        }
    }

}
