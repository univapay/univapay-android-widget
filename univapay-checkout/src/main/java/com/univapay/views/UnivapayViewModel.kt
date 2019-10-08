package com.univapay.views

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.univapay.models.CheckoutArguments
import com.univapay.models.CheckoutData
import com.univapay.payments.CheckoutConfiguration
import com.univapay.payments.SubscriptionSettings
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.response.PaymentsPlan
import com.univapay.sdk.models.response.store.CheckoutInfo
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.sdk.types.RecurringTokenPrivilege
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.tasks.FetchTokensTask
import com.univapay.utils.FunctionalUtils.Either
import com.univapay.utils.FunctionalUtils.Either.Left
import com.univapay.utils.FunctionalUtils.Either.Right
import com.univapay.utils.getUnivapay

class UnivapayViewModel(val args: CheckoutArguments, val config: CheckoutConfiguration<*>, packageName: String): ViewModel(){

    val univapay: UnivapaySDK = getUnivapay(args, packageName)

    private var _checkoutData: MutableLiveData<Either<Throwable, CheckoutData>>? = null
    val checkoutData: MutableLiveData<Either<Throwable, CheckoutData>>
        get() {
            if(_checkoutData == null){
                _checkoutData = MutableLiveData()
                getCheckoutData()
            }
            return _checkoutData ?: throw AssertionError("Could not retrieve checkout data")
        }

    var _paymentsPlan: MutableLiveData<Either<Throwable, PaymentsPlan>>? = null

    fun getPaymentsPlan(paymentType: PaymentTypeName): MutableLiveData<Either<Throwable, PaymentsPlan>> {
            if(_paymentsPlan == null){
                _paymentsPlan = MutableLiveData()
                fetchPaymentsPlan(paymentType)
            }
            return _paymentsPlan ?: throw java.lang.AssertionError()
        }

    init {
        instance = this
    }

    private fun shouldDisplayStoredCards(): Boolean{
        return args.canRememberCards()
    }

    private fun fetchPaymentsPlan(paymentType: PaymentTypeName){

        if(config.settings is SubscriptionSettings && config.settings.installmentPlan != null){

            config.settings.apply {
                univapay.simulateSubscriptionPlan(
                        money,
                        paymentType,
                        period.toSubscriptionPeriod()
                )
                        .withInitialAmount(initialAmount)
                        .withInstallmentPlan(installmentPlan?.toRequest())
                        .withPreserveEndOfMonth(preserveEndOfMonth)
                        .withStartOn(startOn)
                        .withZoneId(zoneId)
                        .build()
                        .dispatch(object: UnivapayCallback<PaymentsPlan>{
                            override fun getResponse(response: PaymentsPlan) {
                                _paymentsPlan?.value = Right(response)
                            }

                            override fun getFailure(error: Throwable) {
                                _paymentsPlan?.value = Left(error)
                            }
                        })
            }

        }


    }


    private fun getCheckoutData(){
        val money = config.settings?.money

        univapay.getCheckoutInfo().dispatch(object: UnivapayCallback<CheckoutInfo>{
            override fun getResponse(checkoutInfo: CheckoutInfo) {


//              Check if the merchant can use recurring tokens. If he can, proceed to request the relevant tokens.
                    if(checkoutInfo.recurringTokenPrivilege == RecurringTokenPrivilege.INFINITE){
                        money?.let {

                        if(shouldDisplayStoredCards()){
//                  If CVV thresholds are enabled, verify whether the CVV should be provided
                            if(isKnownThreshold(checkoutInfo, money)){
                                FetchTokensTask(univapay, args.customerId!!){ tokens->
                                    checkoutData.value = Right(CheckoutData(
                                            checkoutInfo,
                                            tokens,
                                            exceedsThreshold(checkoutInfo, money)
                                    )
                                    )
                                }.execute()
                            } else {
                                convertMoneyAndProceed(checkoutInfo, money, true)
                            }
                        }
                        else {
//                       If the merchant is not gonna display stored tokens, verify the threshold without fetching tokens
                            if(isKnownThreshold(checkoutInfo, money)){
                                checkoutData.value = Right(
                                        CheckoutData(checkoutInfo, exceedsThreshold(checkoutInfo, money)
                                        )
                                )
                            } else {
                                convertMoneyAndProceed(checkoutInfo, money, false)
                            }
                        }

                    }  ?: kotlin.run {
                            if(shouldDisplayStoredCards()){
                                FetchTokensTask(univapay, args.customerId!!){ tokens->
                                    checkoutData.value = Right(CheckoutData(
                                            checkoutInfo,
                                            tokens,
                                            false
                                    )
                                    )
                                }.execute()
                            } else {
                                checkoutData.value = Right(CheckoutData(checkoutInfo))
                            }
                        }
//              If the merchant doesn't have permissions for recurring tokens or is not using recurring tokens
//              TODO: Don't proceed. Display a configuration error if the merchant is attempting to make recurring token payments without the required privilege
                } else {
                        checkoutData.value = Right(CheckoutData(checkoutInfo))
                    }
            }

            override fun getFailure(error: Throwable?) {
                checkoutData.value = Left(error!!)
            }
        })
    }

    private fun isKnownThreshold(checkoutInfo: CheckoutInfo, money: MoneyLike):Boolean{
        return getThresholdForCurrency(checkoutInfo, money.currency) != null
    }

    /**
     * Evaluates whether a CVV check threshold is set for the target currency.
     * Returns null in the case the threshold is not set.
     */
    private fun getThresholdForCurrency(checkoutInfo: CheckoutInfo, currency: String): MoneyLike?{
        return checkoutInfo.recurringTokenCVVConfirmation.threshold?.filter {
            it.currency == currency
        }?.firstOrNull()
    }

    /**
     * Checks whether the CVV check threshold is exceeded. Returns false if money is null.
     */
    private fun exceedsThreshold(checkoutInfo: CheckoutInfo, money: MoneyLike?): Boolean{

        money?.let {
            val cvvThresholdSettings = checkoutInfo.recurringTokenCVVConfirmation
//      CVV is always required for recurring token reuse if the threshold is not set or is disabled
            if(cvvThresholdSettings.enabled == false || cvvThresholdSettings.enabled == null){
                return true
            }
//      Otherwise, if there's at least one currency/amount entry for the threshold, verify that the amount exceeds the threshold.
//      If there's no currency/amount pair, check the CVV (just to be on the safe side, this doesn't happen, since at least one entry is required).
            else {
                return getThresholdForCurrency(checkoutInfo, money.currency)?.let{threshold->
                    money.compareTo(threshold) == 1
                }?: true
            }
        } ?:
        return false
    }

    fun shouldRequireCvv(checkoutInfo: CheckoutInfo, exceedsThreshold: Boolean): Boolean{
        return if(exceedsThreshold){
            true
        } else{
            args.cvvRequired || !(checkoutInfo.cardConfiguration.allowEmptyCvv ?: false)
        }
    }

    private fun convertMoneyAndProceed(checkoutInfo: CheckoutInfo, money: MoneyLike, fetchTokens: Boolean){
        univapay.convertMoney(money, "platform")
                .build()
                .dispatch(object: UnivapayCallback<MoneyLike>{
                    override fun getResponse(converted: MoneyLike?) {

                        if(fetchTokens){
                            FetchTokensTask(univapay, args.customerId!!){ tokens->
                                checkoutData.value = Right(CheckoutData(checkoutInfo, tokens, exceedsThreshold(checkoutInfo, converted!!)))
                            }.execute()
                        } else{
                            checkoutData.value = Right(CheckoutData(checkoutInfo, exceedsThreshold(checkoutInfo, converted!!)))
                        }
                    }

                    override fun getFailure(error: Throwable?) {
                            checkoutData.value = Left(error!!)
                    }
                })
    }

    companion object {
        internal lateinit var instance: UnivapayViewModel
    }
}
