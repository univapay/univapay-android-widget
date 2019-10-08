package com.univapay.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.support.graphics.drawable.AnimatedVectorDrawableCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatButton
import android.support.v7.widget.AppCompatImageView
import android.view.MenuItem
import com.univapay.fragments.UnivapayFragment
import com.univapay.fragments.PaymentTypesFragment
import com.univapay.models.*
import com.univapay.payments.CheckoutConfiguration
import com.univapay.utils.CallbackIntents.InitializationCallbackIntent.InitializationFailureCallback
import com.univapay.utils.CallbackIntents.InitializationCallbackIntent.InitializationSuccessCallback
import com.univapay.utils.Constants
import com.univapay.utils.ErrorParser
import com.univapay.utils.FunctionalUtils.Either
import com.univapay.utils.FunctionalUtils.Either.Right
import com.univapay.views.UnivapayViewModel
import com.univapay.views.UnivapayViewModelFactory
import kotlinx.android.synthetic.main.activity_checkout.*

class CheckoutActivity : AppCompatActivity() {

    var mAddressDetails = AddressDetails()
    var mCardDetails = CardDetails()
    var mKonbiniDetails = KonbiniDetails()
    var customerTokens: ArrayList<TransactionTokenWithDataParcel>? = null
    var exceedsCardThreshold: Boolean? = null
    var mIsBackButtonOn = true
    var mFinished = false
    private lateinit var viewModel: UnivapayViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(resources.getBoolean(com.univapay.R.bool.portrait_only)){
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        val args = intent.extras.getParcelable<CheckoutArguments>(Constants.ARG_CHECKOUT_ARGUMENTS)
        val config = intent.extras.getParcelable<CheckoutConfiguration<*>>(Constants.ARG_CHECKOUT_CONFIGURATION)

        setContentView(com.univapay.R.layout.loading_screen)
        val avd = startLoadingAnimation()

        viewModel = ViewModelProviders.of(this, UnivapayViewModelFactory(args, config, packageName)).get(UnivapayViewModel::class.java)

        viewModel.checkoutData.observe(this, Observer{data: Either<Throwable, CheckoutData>?->

        when(data){
            is Right<CheckoutData> -> {

                exceedsCardThreshold = data.value.exceedsThreshold

                customerTokens = data.value.tokens?.let {tx->
                    ArrayList(tx.map { t-> TransactionTokenWithDataParcel(t) })
                }

                val initializer = data.value.let {checkoutData->

                    val paymentTypes = checkoutData.paymentMethods()

//                  The merchant has all payment types disabled.
                    if(paymentTypes.isEmpty()){
                        Either.Left(UnivapayError("No payment methods enabled."))
                    }
//                  There's exactly one payment type. Display the UI for that particular one.
                    else if(paymentTypes.size == 1){
                            data.value.getInitializerForPaymentMethod(this, paymentTypes.first())
                    }
//                  There's more than one payment type. Show payment type selection screen.
                    else
                        Right({PaymentTypesFragment().display(this)})
                }

                when(initializer){
                    is Right -> {
                        initializeFragments()
                        initializer.value()
                        if(savedInstanceState == null){
                            sendInitializationSuccess()
                        }
                    }
                    is Either.Left -> {
                        displayServiceError()
                        if(savedInstanceState == null){
                            sendInitializationFailure(initializer.value)
                        }
                    }
                }
            }
            is Either.Left<Throwable> -> {
                displayServiceError()
                if(savedInstanceState == null){
                    sendInitializationFailure(ErrorParser.parseError(data.value))
                }
            }
        }
            avd?.stop()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastFinalize()
    }

    fun initializeFragments(){
        setContentView(com.univapay.R.layout.activity_checkout)
        UnivapayFragment.setupTopFragment(this)
    }

    fun sendInitializationSuccess(){
        val intent = Intent(Constants.INTENT_CHECKOUT_INITIALIZATION_CALLBACK)
        intent.putExtra(Constants.ARG_CHECKOUT_CALLBACK_TYPE, InitializationSuccessCallback())
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    fun sendInitializationFailure(t: UnivapayError){
        val intent = Intent(Constants.INTENT_CHECKOUT_INITIALIZATION_CALLBACK)
        intent.putExtra(Constants.ARG_CHECKOUT_CALLBACK_ERROR, t)
        intent.putExtra(Constants.ARG_CHECKOUT_CALLBACK_TYPE, InitializationFailureCallback())
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    fun broadcastFinalize(){
        val intent = Intent(Constants.INTENT_CHECKOUT_FINALIZE)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun displayServiceError(){
        setContentView(com.univapay.R.layout.checkout_error_message)
        findViewById<AppCompatButton>(com.univapay.R.id.checkout_service_error_button).setOnClickListener {finish() }
    }

    private fun startLoadingAnimation(): Animatable?{
        val drawable = AnimatedVectorDrawableCompat.create(this, com.univapay.R.drawable.avd_loading)
        val loadingAnimation = findViewById<AppCompatImageView>(com.univapay.R.id.loading_animation)
        loadingAnimation?.setImageDrawable(drawable)
        val avd = drawable as Animatable?
        avd?.start()
        return avd
    }

    override fun onBackPressed() {
        // Disable Back Button while we are processing the Transaction
        if (mIsBackButtonOn) {
            if (mFinished) {
                finish()
                return
            }
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home && button_action.isEnabled) {
            // handle close button
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun setFinished() {
        mFinished = true
    }
}
