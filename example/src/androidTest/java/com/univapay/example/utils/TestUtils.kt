package com.univapay.example.utils

import com.univapay.UnivapayCheckout
import com.univapay.example.utils.TestConstants.Companion.LOGIN_CREDENTIALS
import com.univapay.example.utils.TestConstants.Companion.TEST_ORIGIN
import com.univapay.example.utils.TestConstants.Companion.TEST_PHONE_NUMBER
import com.univapay.example.utils.TestConstants.Companion.UNIVAPAY_SDK_CONFIG
import com.univapay.models.AppCredentials
import com.univapay.models.UnivapayError
import com.univapay.payments.CheckoutConfiguration
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.*
import com.univapay.sdk.models.common.auth.AppJWTStrategy
import com.univapay.sdk.models.request.transactiontoken.PaymentData
import com.univapay.sdk.models.response.applicationtoken.StoreApplicationJWT
import com.univapay.sdk.models.response.store.RecurringTokenConfiguration
import com.univapay.sdk.models.response.store.StoreWithConfiguration
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.PhoneNumber
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.Konbini
import com.univapay.sdk.types.ProcessingMode
import com.univapay.sdk.types.RecurringTokenPrivilege
import com.univapay.sdk.types.TransactionTokenType
import com.univapay.sdk.utils.builders.CardConfigurationBuilder
import org.joda.time.Period
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import java.net.URI
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object TestUtils{

    fun getUnivapay(): Lazy<UnivapaySDK> {
        return lazy {
            val unauthClient = UnivapaySDK.create(UNIVAPAY_SDK_CONFIG)
            val loginTokenStrategy = unauthClient.getLoginToken(LOGIN_CREDENTIALS).dispatch().loginTokenAuthStrategy
            unauthClient.clone(loginTokenStrategy)
        }
    }

    fun getStore(univapay: UnivapaySDK) = lazy{

        univapay.createStore("Store ${UUID.randomUUID()}")
            .withCardConfiguration(
                CardConfigurationBuilder()
                        .withEnabled(true)
                        .withDebitEnabled(false)
                        .withPrepaidEnabled(false)
                        .withAllowEmptyCvv(true)
                        .build()
            )
            .withRecurringTokenConfiguration(
                RecurringTokenConfiguration(
                    RecurringTokenPrivilege.INFINITE,
                    Period.days(3),
                    RecurringTokenCVVConfirmation(
                        true,
                        listOf(
                            MoneyLike(TestConstants.jpyLimit, "JPY"),
                            MoneyLike(TestConstants.usdLimit, "USD")
                        )
                    )
                )
            )
            .build()
            .dispatch()
    }

    fun getAppToken(univapay: UnivapaySDK, store: StoreWithConfiguration) = lazy{
        val domains: List<Domain> = ArrayList(Collections.singleton(Domain(TEST_ORIGIN)))
        univapay.createStoreAppJWT(store.id)
                .withMode(ProcessingMode.TEST)
                .withDomains(domains)
                .build()
                .dispatch()

    }

    fun setup(univapay: UnivapaySDK, store: StoreWithConfiguration){
        univapay.updateStore(store.id)
                .withCardConfiguration(
                        CardConfigurationBuilder()
                                .withEnabled(true)
                                .build()
                ).withRecurringTokenConfiguration(
                        RecurringTokenConfiguration(
                                RecurringTokenPrivilege.INFINITE,
                                Period.ZERO,
                                null
                        )
                ).withConvenienceConfiguration(
                        KonbiniConfiguration(false)
                )
    }

    fun createTxToken(univapay: UnivapaySDK, appJWT: StoreApplicationJWT, paymentData: PaymentData, userId: UUID): TransactionTokenWithData{
        val token = univapay.copy(AppJWTStrategy(appJWT.jwt, appJWT.secret))
                .createTransactionToken("sldfjio@lkasudfo.com", paymentData, TransactionTokenType.RECURRING)
                .withCustomerId(UnivapayCustomerId(userId))
                .build()
                .dispatch()

//      Give the API time to index the token
        Thread.sleep(3000)

        return token
    }

    fun deleteSubscription(univapay: UnivapaySDK, subscription: FullSubscription){
        univapay.deleteSubscription(subscription.storeId, subscription.id).dispatch()
    }

    fun teardown(univapay: UnivapaySDK, storeId: StoreId, checkout: UnivapayCheckout?){
        univapay.deleteStore(storeId).dispatch()
    }

    fun createCreditCard(): CreditCard{
        return CreditCard("credit card payments user", generateCardNumber(), 12, 2100, 123)
    }

    fun createKonbiniData(konbini: Konbini = getRandomKonbini().konbini): KonbiniPayment = KonbiniPayment(
            "konbini payments user",
            konbini,
            PhoneNumber(81, TEST_PHONE_NUMBER)
    )

    fun createCheckout(checkoutConfiguration: CheckoutConfiguration<*>, cvvRequired: Boolean, appCredentials: AppCredentials): UnivapayCheckout {
        return UnivapayCheckout.Builder(
                appCredentials,
                checkoutConfiguration)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(cvvRequired)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .build()
    }

    fun <T>testCallback(latch: CountDownLatch, asserts: (a: T) -> Unit ): (a: T) -> Unit{
        return {response->
            asserts(response)
            latch.countDown()
        }
    }

    fun <T>failureTestCallback(latch: CountDownLatch): (response: T) -> Unit{
        return {response->
            when(response){
                is UnivapayError? -> {
                    fail(response?.message ?: "Unexpected result")
                }
                else -> fail("Unexpected result")
            }
//          Release the thread
            val latchCount = latch.count
            (1 to latchCount).toList().forEach { latch.countDown() }
        }
    }

    fun await(latch: CountDownLatch){
        try {
            assertTrue(latch.await(TestConstants.defaultLatchTimeout, TimeUnit.MILLISECONDS))
        } catch (_: AssertionError){
            fail("Test timed out")
        }
    }

    fun univapayTest(latchCount: Int, setup: (latch: CountDownLatch) -> Unit){
        val latch = CountDownLatch(latchCount)
        setup(latch)
        await(latch)
    }

//    fun univapayTest(latchCount: Int, setup: (latch: CountDownLatch) -> Unit) = univapayTest(latchCount, setup)
}
