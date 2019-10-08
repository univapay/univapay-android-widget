package com.univapay.payments

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.widget.Toast
import com.univapay.models.ChargeParcel
import com.univapay.models.SubscriptionParcel
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.models.UnivapayError
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.builders.transactiontoken.TransactionTokensBuilders
import com.univapay.sdk.models.common.CreditCard
import com.univapay.sdk.models.common.IdempotencyKey
import com.univapay.sdk.models.errors.UnivapayException
import com.univapay.sdk.models.request.transactiontoken.PaymentData
import com.univapay.sdk.models.response.PaginatedList
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.subscription.ScheduledPayment
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.ChargeStatus
import com.univapay.sdk.types.MetadataMap
import com.univapay.sdk.types.SubscriptionStatus
import com.univapay.sdk.types.TransactionTokenType
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.utils.CallbackIntents
import com.univapay.utils.Constants
import com.univapay.utils.ErrorParser
import com.univapay.utils.unlockUI
import com.univapay.views.SecurityEditText
import com.univapay.views.UnivapayViewModel
import java.util.*

/**
 * Container for the transaction's settings. Extend or implement this class to extend the library
 * with a new checkout type.
 *
 * @see OneTimeTokenCheckout
 * @see RecurringTokenCheckout
 * @see SubscriptionCheckout
 */
abstract class CheckoutConfiguration<S: PaymentSettings>: Checkout, Parcelable {

    val settings: S?

    val processFullPayment: Boolean

    /**
     * Using this constructor allows the customer to manage its token and select one for processing.
     * Payment details are not displayed.
     * The token that was selected or created is returned to the merchant as the argument for the <code>onToken</code> callback.
     * The merchant is in charge of processing the transaction in its backend.
     */
    constructor() {
        this.settings = null
        this.processFullPayment = false
    }

    /**
     * This constructor tells the widget to display the payment settings to the user, and to behave in the same
     * way as if the empty constructor was being used.
     */
    constructor(settings: S): this(settings, false)

    /**
     * Use this constructor to set the checkout type to something different from TOKEN.
     * In this case the payment settings are displayed. If the <code>checkoutType</code> equals TOKEN,
     * the widget behaves as in the case when only settings are passed, where the merchant is in charge
     * of processing the transaction on its own.
     * If <code>processFullPayment == true</code>, the full transaction processing is performed
     * by the widget.
     */
    constructor(settings: S, processFullPayment: Boolean){
        this.settings = settings
        this.processFullPayment = processFullPayment
    }

    var callback: CheckoutCallback? = null

    abstract val transactionTokenType: TransactionTokenType

    override fun describeContents(): Int {
        return 0
    }

    fun getTokenBuilder(univapay: UnivapaySDK,
                        email: String,
                        paymentData: PaymentData,
                        transactionTokenType: TransactionTokenType,
                        metadata: MetadataMap?): TransactionTokensBuilders.CreateTransactionTokenRequestBuilder {
        return univapay
                .createTransactionToken(email, paymentData, transactionTokenType)
                .withMetadata(metadata)
                .withIdempotencyKey(IdempotencyKey(UUID.randomUUID().toString()))
    }

    companion object {

        fun handleTransactionTokenFailure(paymentData: PaymentData,
                                          throwable: Throwable,
                                          context: Context,
                                          requestAgain: (updatedCreditCard: CreditCard) -> Unit){
            when(paymentData){
                is CreditCard ->
                    if (UnivapayException::class.java.isInstance(throwable)){
                        val exception = throwable as UnivapayException
                        if(
                                exception.httpStatusMessage == "CVV_REQUIRED" ||
                                exception.body.errors.filter { detailedError ->
                                    detailedError.field == "data.cvv"
                                }.isNotEmpty()
                        ) {
                            retryWithCVV(context, throwable, paymentData, requestAgain)
                        } else
                            doFailed(CallbackIntents.TokenCallbackIntent.TokenFailureCallback(), null, ErrorParser.parseError(exception as Throwable), context)
                    }
                else -> doFailed(CallbackIntents.TokenCallbackIntent.TokenFailureCallback(), null, ErrorParser.parseError(throwable), context)
            }
        }

        fun retryWithCVV(context: Context,
                         throwable: Throwable,
                         creditCard: CreditCard,
                         requestAgain: (updatedCreditCard: CreditCard) -> Unit){

            AlertDialog.Builder(context).apply {

                setTitle(com.univapay.R.string.checkout_recurring_token_cvv_challenge_title)
                setView(com.univapay.R.layout.dialog_cvv)
                setPositiveButton(com.univapay.R.string.checkout_recurring_token_permission_positive_button, null)
                setNegativeButton(com.univapay.R.string.checkout_recurring_token_permission_negative_button){ _, _ -> }
                val cvvDialog = create()
                cvvDialog.setOnShowListener{dialogInterface ->
                    val cvvChallengeInput: SecurityEditText = (dialogInterface as AlertDialog).findViewById(com.univapay.R.id.cvv_challenge)!!

                    dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{ _ ->
                        cvvChallengeInput.validate()
                        if(cvvChallengeInput.isValid){

                            val updatedCreditCard = cloneWithCVV(
                                    creditCard,
                                    cvvChallengeInput.asInt()
                            )
                            dialogInterface.dismiss()
                            requestAgain(updatedCreditCard)
                        }
                    }

                    dialogInterface.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{ _ ->
                        dialogInterface.dismiss()
                        doFailed(CallbackIntents.TokenCallbackIntent.TokenFailureCallback(), null, ErrorParser.parseError(throwable), context)
                    }
                }
                cvvDialog.show()
            }
        }

        fun handleTransactionFailure(transactionToken: TransactionTokenWithData,
                                     throwable: Throwable,
                                     context: Context,
                                     callbackIntentType: CallbackIntents.CallbackIntentParcelable,
                                     requestAgain: (TransactionTokenWithData) -> Unit){

            when(transactionToken.data){
                is CreditCard ->
                    if (UnivapayException::class.java.isInstance(throwable)){
                        val exception = throwable as UnivapayException
                        if(exception.httpStatusMessage == "RECURRING_USAGE_REQUIRES_CVV") {
                            retryWithCVV(context, throwable, transactionToken,callbackIntentType, requestAgain)
                        } else
                            doFailed(callbackIntentType, null, ErrorParser.parseError(exception as Throwable), context)
                    }
                else -> doFailed(callbackIntentType, null, ErrorParser.parseError(throwable), context)
            }

        }

        fun retryWithCVV(context: Context,
                         throwable: Throwable,
                         token: TransactionTokenWithData,
                         callbackIntentType: CallbackIntents.CallbackIntentParcelable,
                         requestAgain: (TransactionTokenWithData) -> Unit){

            AlertDialog.Builder(context).apply {

                setTitle(com.univapay.R.string.checkout_recurring_token_cvv_challenge_title)
                setView(com.univapay.R.layout.dialog_cvv)
                setPositiveButton(com.univapay.R.string.checkout_recurring_token_permission_positive_button, null)
                setNegativeButton(com.univapay.R.string.checkout_recurring_token_permission_negative_button){ _, _ -> }
                val cvvDialog = create()
                cvvDialog.setOnShowListener{dialogInterface ->
                    val cvvChallengeInput: SecurityEditText = (dialogInterface as AlertDialog).findViewById(com.univapay.R.id.cvv_challenge)!!

                    dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{ _ ->
                        cvvChallengeInput.validate()
                        if(cvvChallengeInput.isValid){

                            UnivapayViewModel.instance.univapay.updateTransactionToken(token.storeId, token.id)
                                    .withCvv(cvvChallengeInput.text.toString().toInt())
                                    .build()
                                    .dispatch(object: UnivapayCallback<TransactionTokenWithData> {
                                        override fun getResponse(updatedToken: TransactionTokenWithData) {
                                            requestAgain(updatedToken)
                                            dialogInterface.dismiss()
                                        }

                                        override fun getFailure(updateTokenError: Throwable) {
                                            dialogInterface.dismiss()
                                            doFailed(callbackIntentType, null, ErrorParser.parseError(throwable), context)
                                        }
                                    })
                            dialogInterface.dismiss()
                        }
                    }

                    dialogInterface.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener{ _ ->
                        dialogInterface.dismiss()
                        doFailed(callbackIntentType, null, ErrorParser.parseError(throwable), context)
                    }
                }
                cvvDialog.show()
            }
        }

        fun broadcastParcel(type: CallbackIntents.CallbackIntentParcelable, parcel: Parcelable, error: UnivapayError?, context: Context) {
            val intent: Intent
            val transactionDetails: String

            when(parcel){
                is ChargeParcel -> {
                    intent = Intent(Constants.INTENT_CHECKOUT_CHARGE_CALLBACK)
                    transactionDetails = Constants.ARG_CHECKOUT_CALLBACK_CHARGE_DETAILS
                    completeBroadcast(intent, transactionDetails, type, parcel, error, context)
                }
                is SubscriptionParcel -> {
                    intent = Intent(Constants.INTENT_CHECKOUT_SUBSCRIPTION_CALLBACK)
                    transactionDetails = Constants.ARG_CHECKOUT_CALLBACK_SUBSCRIPTION_DETAILS
                    completeBroadcast(intent, transactionDetails, type, parcel, error, context)
                }
                is TransactionTokenWithDataParcel -> {
                    intent = Intent(Constants.INTENT_CHECKOUT_TOKEN_CALLBACK)
                    transactionDetails = Constants.ARG_CHECKOUT_CALLBACK_TOKEN_DETAILS
                    completeBroadcast(intent, transactionDetails, type, parcel, error, context)
                }
            }
        }

        private fun completeBroadcast(intent: Intent, transactionDetails: String, type: CallbackIntents.CallbackIntentParcelable, parcel: Parcelable, error: UnivapayError?, context: Context){
            intent.putExtra(Constants.ARG_CHECKOUT_CALLBACK_TYPE, type)
            intent.putExtra(transactionDetails, parcel)
            intent.putExtra(Constants.ARG_CHECKOUT_CALLBACK_ERROR, error)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }

        fun broadcastSuccess(type: CallbackIntents.CallbackIntentParcelable, parcel: Parcelable, context: Context) {
            broadcastParcel(type, parcel, null, context)
        }

        fun handleError(error: UnivapayError?, context: Context) {
            if (error == null) {
                return
            }

            val msg = error.message
            // for API Error and Processing Error display toast
            if ("" != msg) {
                val toast = Toast.makeText(context, msg, Toast.LENGTH_LONG)
                toast.show()
            }
        }

        fun doFailed(type: CallbackIntents.CallbackIntentParcelable, parcel: Parcelable?, error: UnivapayError, context: Context) {
            handleError(error, context)
            unlockUI(Constants.INTENT_PROCEED_FAILURE)
            parcel?.let {
                broadcastParcel(type, parcel, error, context)
            }
        }

        fun doUndetermined(type: CallbackIntents.CallbackIntentParcelable, parcel: Parcelable?, error: UnivapayError, context: Context) {
            handleError(error, context)
            unlockUI(Constants.INTENT_PROCEED_UNDETERMINED)
            parcel?.let {
                broadcastParcel(type, parcel, error, context)
            }
        }

        fun cloneWithCVV(creditCard: CreditCard, cvv: Int): CreditCard {
            return CreditCard(
                    creditCard.cardholder,
                    creditCard.cardNumber,
                    creditCard.expMonth,
                    creditCard.expYear,
                    cvv
            )
        }

        /**
         * Poll for the status of a charge
         */
        fun pollCardCharge(univapay: UnivapaySDK, charge: Charge, context: Context){
            univapay.chargeCompletionMonitor(charge.storeId, charge.id).await(object : UnivapayCallback<Charge> {
                override fun getResponse(polledCharge: Charge) {
                    when(polledCharge.status){
                        ChargeStatus.FAILED, ChargeStatus.ERROR ->
                            doFailed(CallbackIntents.ChargeCallbackIntent.ChargeFailureCallback(), ChargeParcel(polledCharge), ErrorParser.parseError(polledCharge.error), context)
                        ChargeStatus.PENDING -> {
                            val error = UnivapayError(Constants.CHARGE_STATUS_PENDING_ERROR)
                            doUndetermined(CallbackIntents.ChargeCallbackIntent.ChargeUndeterminedCallback(), ChargeParcel(polledCharge), error, context)
                        }
                        ChargeStatus.SUCCESSFUL, ChargeStatus.AUTHORIZED, ChargeStatus.AWAITING ->
                            broadcastSuccess(CallbackIntents.ChargeCallbackIntent.ChargeSuccessCallback(), ChargeParcel(polledCharge), context)
                        else -> {
                            val error = UnivapayError(Constants.CHARGE_UNEXPECTED_STATUS_ERROR)
                            doFailed(CallbackIntents.ChargeCallbackIntent.ChargeFailureCallback(), ChargeParcel(polledCharge), error, context)
                        }
                    }
                }

                override fun getFailure(error: Throwable) {
//                                  This case is also undetermined because we failed to get the status of the charge
                    doUndetermined(CallbackIntents.ChargeCallbackIntent.ChargeUndeterminedCallback(), ChargeParcel(charge), ErrorParser.parseError(error), context)
                }
            })
        }


        /**
         * Poll for the status of a subscription
         */
        fun pollCardSubscription(univapay: UnivapaySDK, subscription: FullSubscription, context: Context){
            univapay.subscriptionCompletionMonitor(subscription.storeId, subscription.id).await(object: UnivapayCallback<FullSubscription>{
                override fun getResponse(polledSubscription: FullSubscription) {
                    when(polledSubscription.status){
                        SubscriptionStatus.CURRENT ->
                            broadcastSuccess(CallbackIntents.SubscriptionCallbackIntent.SubscriptionSuccessCallback(), SubscriptionParcel(polledSubscription), context)
                        SubscriptionStatus.UNCONFIRMED -> {
                            val error = UnivapayError(Constants.SUBSCRIPTION_FAILED_TO_CONFIRM_ERROR)
                            doFailed(CallbackIntents.SubscriptionCallbackIntent.SubscriptionFailureCallback(), SubscriptionParcel(polledSubscription), error, context)
                        }
                        SubscriptionStatus.UNVERIFIED -> {
                            val error = UnivapayError(Constants.SUBSCRIPTION_STATUS_UNVERIFIED_ERROR)
                            doUndetermined(CallbackIntents.SubscriptionCallbackIntent.SubscriptionUndeterminedCallback(), SubscriptionParcel(polledSubscription), error, context)
                        }
                        else -> {
                            val error = UnivapayError(Constants.SUBSCRIPTION_UNEXPECTED_STATUS_ERROR)
                            doFailed(CallbackIntents.SubscriptionCallbackIntent.SubscriptionFailureCallback(), SubscriptionParcel(polledSubscription), error, context)
                        }
                    }
                }
                override fun getFailure(error: Throwable) {
//                                  This case is also undetermined because we failed to get the status of the subscription
                    doUndetermined(CallbackIntents.SubscriptionCallbackIntent.SubscriptionUndeterminedCallback(), SubscriptionParcel(subscription), ErrorParser.parseError(error), context)
                }
            })
        }

        /**
         * Poll for the status of a subscription to be paid via a convenience store
         */
        fun pollKonbiniSubscription(univapay: UnivapaySDK, subscription: FullSubscription, context: Context){
            univapay.listScheduledPayments(subscription.storeId, subscription.id)
                    .build()
                    .dispatch(object: UnivapayCallback<PaginatedList<ScheduledPayment>>{
                        override fun getResponse(payments: PaginatedList<ScheduledPayment>) {

                            payments.items.firstOrNull()?.let {payment->

                                univapay.listChargesForPayment(subscription.storeId, subscription.id, payment.id)
                                        .build()
                                        .dispatch(object: UnivapayCallback<PaginatedList<Charge>>{
                                            override fun getResponse(charges: PaginatedList<Charge>) {
                                                charges.items.firstOrNull()?.let {charge->
                                                    univapay.chargeCompletionMonitor(charge.storeId, charge.id).await(object : UnivapayCallback<Charge> {
                                                        override fun getResponse(polledCharge: Charge) {
                                                            when(polledCharge.status){
                                                                ChargeStatus.FAILED, ChargeStatus.ERROR ->
                                                                    doFailed(CallbackIntents.SubscriptionCallbackIntent.SubscriptionFailureCallback(), SubscriptionParcel(subscription), ErrorParser.parseError(polledCharge.error), context)
                                                                ChargeStatus.PENDING -> {
                                                                    val error = UnivapayError(Constants.CHARGE_STATUS_PENDING_ERROR)
                                                                    doUndetermined(CallbackIntents.SubscriptionCallbackIntent.SubscriptionUndeterminedCallback(), SubscriptionParcel(subscription), error, context)
                                                                }
                                                                ChargeStatus.SUCCESSFUL, ChargeStatus.AUTHORIZED, ChargeStatus.AWAITING ->
                                                                    broadcastSuccess(CallbackIntents.SubscriptionCallbackIntent.SubscriptionSuccessCallback(), SubscriptionParcel(subscription), context)
                                                                else -> {
                                                                    val error = UnivapayError(Constants.CHARGE_UNEXPECTED_STATUS_ERROR)
                                                                    doFailed(CallbackIntents.SubscriptionCallbackIntent.SubscriptionFailureCallback(), SubscriptionParcel(subscription), error, context)
                                                                }
                                                            }
                                                        }

                                                        override fun getFailure(error: Throwable) {
//                                  This case is also undetermined because we failed to get the status of the charge
                                                            doUndetermined(CallbackIntents.ChargeCallbackIntent.ChargeUndeterminedCallback(), ChargeParcel(charge), ErrorParser.parseError(error), context)
                                                        }
                                                    })
                                                } ?: kotlin.run {
                                                    val error = UnivapayError(Constants.SUBSCRIPTION_STATUS_UNVERIFIED_ERROR)
                                                    doUndetermined(CallbackIntents.SubscriptionCallbackIntent.SubscriptionUndeterminedCallback(), SubscriptionParcel(subscription), error, context)
                                                }
                                            }

                                            override fun getFailure(error: Throwable) {
                                                doUndetermined(CallbackIntents.SubscriptionCallbackIntent.SubscriptionUndeterminedCallback(), SubscriptionParcel(subscription), ErrorParser.parseError(error), context)
                                            }
                                        })
                            } ?: kotlin.run {
                                val error = UnivapayError(Constants.SUBSCRIPTION_STATUS_UNVERIFIED_ERROR)
                                doUndetermined(CallbackIntents.SubscriptionCallbackIntent.SubscriptionUndeterminedCallback(), SubscriptionParcel(subscription), error, context)
                            }
                        }

                        override fun getFailure(error: Throwable) {
                            doUndetermined(CallbackIntents.SubscriptionCallbackIntent.SubscriptionUndeterminedCallback(), SubscriptionParcel(subscription), ErrorParser.parseError(error), context)
                        }
                    })
        }
    }
}
