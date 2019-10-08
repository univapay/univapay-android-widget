package com.univapay.example

import android.support.test.espresso.matcher.ViewMatchers.assertThat
import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayApplication
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.assertCheckoutLayout
import com.univapay.example.utils.FragmentAssertions.assertSubscriptionWithInstallmentDetails
import com.univapay.example.utils.FragmentAssertions.clickActionButton
import com.univapay.example.utils.FragmentAssertions.performPayment
import com.univapay.example.utils.FragmentAssertions.performRecurringPayment
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.SUBSCRIPTION_MONEY
import com.univapay.example.utils.TestConstants.Companion.TEST_EMAIL
import com.univapay.example.utils.TestConstants.Companion.TEST_EXPIRY
import com.univapay.example.utils.TestConstants.Companion.TEST_USER
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils.failureTestCallback
import com.univapay.example.utils.TestUtils.testCallback
import com.univapay.example.utils.TestUtils.univapayTest
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.models.AppCredentials
import com.univapay.models.CaptureSettings
import com.univapay.models.UnivapayError
import com.univapay.payments.*
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.ChargeStatus
import com.univapay.sdk.types.ProcessingMode
import com.univapay.sdk.types.TransactionTokenType
import com.univapay.utils.Constants
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers.`is`
import org.joda.time.LocalDate
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.OffsetDateTime
import org.threeten.bp.ZoneId
import java.math.BigInteger
import java.net.URI
import java.util.*

@RunWith(AndroidJUnit4::class)
class CallbackInFullPaymentTest: UnivaPayAndroidTest() {

    @Test
    fun shouldTriggerCallbackWhenCreateOnetimeTokenChargeSuccessfully() = univapayTest(2) { latch->
        val paymentSettings = ChargeSettings(MONEY)
        val checkoutConfiguration = OneTimeTokenCheckout(paymentSettings, processFullPayment = true)

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder.setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
            .setTokenCallback(
                testCallback(latch){ token: TransactionTokenWithData? ->
                    assertThat(token!!.type, `is`(TransactionTokenType.ONE_TIME))
                    assertThat(token.email, `is`(TEST_EMAIL))
                    assertThat(token.data.card.cardholder, `is`(TEST_USER))
                    assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                    assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                },
                failureTestCallback(latch)
            ).setChargeCallback(
                testCallback(latch){ charge: Charge? ->
                    assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.ONE_TIME))
                    assertThat(charge.chargedCurrency, `is`(MONEY.currency))
                    assertThat(charge.chargedAmount, `is`(MONEY.amount))
                },
                failureTestCallback(latch),
                failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performPayment()
        clickActionButton()
    }

    @Ignore
    @Test
    fun shouldTriggerErrorCallbackWhenFailToCreateOnetimeToken() = univapayTest(2) { latch->
        val paymentSettings = ChargeSettings(MONEY)
        val checkoutConfiguration = OneTimeTokenCheckout(paymentSettings, processFullPayment = true)

        //TODO token作成がエラーになるような作り方
        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder.setTitle(TITLE).setDescription(DESCRIPTION).setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setTokenCallback(
                failureTestCallback(latch),
                testCallback(latch){ error: UnivapayError? ->
                    assertThat(error!!.message, `is`("aaaaa"))
                }
            ).setChargeCallback(
                failureTestCallback(latch),
                failureTestCallback(latch),
                failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        assertCheckoutLayout(TITLE, DESCRIPTION, paymentSettings)
        performPayment()
        clickActionButton()
    }

    @Test
    fun shouldTriggerErrorCallbackWhenFailToCreateOnetimeCharge() = univapayTest(2){ latch->
        val paymentSettings = ChargeSettings(MONEY)
        val checkoutConfiguration = OneTimeTokenCheckout(paymentSettings, processFullPayment = true)

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setCvvRequired(true)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setTokenCallback(
                testCallback(latch){ token: TransactionTokenWithData? ->
                    assertThat(token!!.type, `is`(TransactionTokenType.ONE_TIME))
                    assertThat(token.email, `is`(TEST_EMAIL))
                    assertThat(token.data.card.cardholder, `is`(TEST_USER))
                    assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                    assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                },
                    failureTestCallback(latch)
            ).setChargeCallback(
                    failureTestCallback(latch),
                testCallback(latch){ error: UnivapayError? ->
                    assertThat(error?.message,
                        `is`(UnivapayApplication.context.resources.getString(R.string.checkout_payment_error_309)))
                },
                    failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performPayment("4111111111111111")
        clickActionButton()
    }

    @Test
    fun shouldTriggerCallbackWhenCreateRecurringTokenChargeSuccessfully() = univapayTest(2){ latch->
        val paymentSettings = ChargeSettings(MONEY)
        val checkoutConfiguration = RecurringTokenCheckout(paymentSettings, processFullPayment = true)

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setCvvRequired(true)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setTokenCallback(
                testCallback(latch){ token: TransactionTokenWithData? ->
                    assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                    assertThat(token.email, `is`(TEST_EMAIL))
                    assertThat(token.data.card.cardholder, `is`(TEST_USER))
                    assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                    assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                },
                failureTestCallback(latch)
            ).setChargeCallback(
                testCallback(latch){ charge: Charge? ->
                    assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                    assertThat(charge.chargedCurrency, `is`(MONEY.currency))
                    assertThat(charge.chargedAmount, `is`(MONEY.amount))
                },
                failureTestCallback(latch),
                failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performRecurringPayment()
        clickActionButton()
    }

    @Test
    fun shouldAuthorizeOneTimeTokenCharge() = univapayTest(2){ latch->
        val paymentSettings = ChargeSettings(MONEY, CaptureSettings(false))
        val checkoutConfiguration = OneTimeTokenCheckout(
                paymentSettings,
                processFullPayment = true
        )

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setTokenCallback(
                        testCallback(latch){ token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.ONE_TIME))
                            assertThat(token.email, `is`(TEST_EMAIL))
                            assertThat(token.data.card.cardholder, `is`(TEST_USER))
                            assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                            assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                        },
                        failureTestCallback(latch)
                ).setChargeCallback(
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.ONE_TIME))
                            assertThat(charge.chargedCurrency, `is`(MONEY.currency))
                            assertThat(charge.chargedAmount, `is`(MONEY.amount))
                            assertThat(charge.status, `is`(ChargeStatus.AUTHORIZED))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performPayment()
        clickActionButton()
    }

    @Test
    fun shouldSetCaptureDateForOneTimeTokenCharge() = univapayTest(2){ latch->

        val captureAt: Date = DateTimeUtils.toDate(OffsetDateTime.now().plusDays(2).toInstant())
        val paymentSettings = ChargeSettings(MONEY, CaptureSettings(captureAt))

        val checkoutConfiguration = OneTimeTokenCheckout(
                paymentSettings,
                processFullPayment = true
        )

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setTokenCallback(
                        testCallback(latch){ token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.ONE_TIME))
                            assertThat(token.email, `is`(TEST_EMAIL))
                            assertThat(token.data.card.cardholder, `is`(TEST_USER))
                            assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                            assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                        },
                        failureTestCallback(latch)
                ).setChargeCallback(
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.ONE_TIME))
                            assertThat(charge.chargedCurrency, `is`(MONEY.currency))
                            assertThat(charge.chargedAmount, `is`(MONEY.amount))
                            assertThat(charge.status, `is`(ChargeStatus.AUTHORIZED))
                            assertThat(charge.captureAt, `is`(captureAt))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performPayment()
        clickActionButton()
    }

    @Test
    fun shouldAuthorizeRecurringTokenCharge() = univapayTest(2){ latch->
        val paymentSettings = ChargeSettings(MONEY, CaptureSettings(false))
        val checkoutConfiguration =
                RecurringTokenCheckout(
                        paymentSettings,
                        processFullPayment = true
                )

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TITLE)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setTokenCallback(
                        testCallback(latch){ token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                            assertThat(token.email, `is`(TEST_EMAIL))
                            assertThat(token.data.card.cardholder, `is`(TEST_USER))
                            assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                            assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                        },
                        failureTestCallback(latch)
                ).setChargeCallback(
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                            assertThat(charge.chargedCurrency, `is`(MONEY.currency))
                            assertThat(charge.chargedAmount, `is`(MONEY.amount))
                            assertThat(charge.status, `is`(ChargeStatus.AUTHORIZED))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performRecurringPayment()
        clickActionButton()
    }

    @Test
    fun shouldSetCaptureDateForRecurringTokenCharge() = univapayTest(2){ latch->

        val captureAt: Date = DateTimeUtils.toDate(OffsetDateTime.now().plusDays(2).toInstant())

        val paymentSettings = ChargeSettings(MONEY, CaptureSettings(captureAt))

        val checkoutConfiguration =
                RecurringTokenCheckout(
                        paymentSettings,
                        processFullPayment = true
                )

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setTokenCallback(
                        testCallback(latch){ token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                            assertThat(token.email, `is`(TEST_EMAIL))
                            assertThat(token.data.card.cardholder, `is`(TEST_USER))
                            assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                            assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                        },
                        failureTestCallback(latch)
                ).setChargeCallback(
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                            assertThat(charge.chargedCurrency, `is`(MONEY.currency))
                            assertThat(charge.chargedAmount, `is`(MONEY.amount))
                            assertThat(charge.status, `is`(ChargeStatus.AUTHORIZED))
                            assertThat(charge.captureAt, `is`(captureAt))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performRecurringPayment()
        clickActionButton()
    }

    @Test
    fun shouldTriggerErrorCallbackWhenFailToCreateRecurringTokenCharge() = univapayTest(2){ latch->
        val paymentSettings = ChargeSettings(MONEY)
        val checkoutConfiguration = RecurringTokenCheckout(paymentSettings, processFullPayment = true)

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
            .setTokenCallback(
                testCallback(latch){ token: TransactionTokenWithData? ->
                    assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                    assertThat(token.email, `is`(TEST_EMAIL))
                    assertThat(token.data.card.cardholder, `is`(TEST_USER))
                    assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                    assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                },
                failureTestCallback(latch)
            ).setChargeCallback(
                failureTestCallback(latch),
                testCallback(latch){ error: UnivapayError? ->
                    assertThat(error?.message,
                        `is`(UnivapayApplication.context.resources.getString(R.string.checkout_payment_error_309)))
                },
                failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performRecurringPayment("4111111111111111")
        clickActionButton()
    }

    @Ignore("Be enable after API is fixed")
    @Test
    fun shouldTriggerCallbackWhenSubscriptionCreatedSuccessfully() = univapayTest(2){ latch->
        val zoneId = ZoneId.of("America/Cancun")
        val startOn = LocalDate.now().plusDays(10)
        val preserveEndOfMonth = true
        val period = SubscriptionPeriod.Monthly
        val initialAmount = BigInteger.valueOf(1000)
        val installmentPlan = FixedCycleInstallmentsPlan(4)

        val paymentSettings = SubscriptionSettings(SUBSCRIPTION_MONEY, period)
                .withStartOn(startOn)
                .withZoneId(zoneId)
                .withPreserveEndOfMonth(preserveEndOfMonth)
                .withInstallmentPlan(installmentPlan)
                .withInitialAmount(initialAmount)

        val checkoutConfiguration =
                SubscriptionCheckout(
                        paymentSettings,
                        processFullPayment = true
                )

        val builder = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                checkoutConfiguration
        )
        checkout = builder
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setMetadata(TestConstants.METADATA)
            .setTokenCallback(
                testCallback(latch){ token: TransactionTokenWithData? ->
                    assertThat(token!!.type, `is`(TransactionTokenType.SUBSCRIPTION))
                    assertThat(token.email, `is`(TEST_EMAIL))
                    assertThat(token.data.card.cardholder, `is`(TEST_USER))
                    assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                    assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))
                    assertThat(token.metadata["some_key"], CoreMatchers.`is`(TestConstants.METADATA["some_key"]))
                },
                failureTestCallback(latch)
            ).setSubscriptionCallback(
                testCallback(latch){ subscription: FullSubscription? ->
                    assertThat(subscription!!.amount, CoreMatchers.`is`(TestConstants.SUBSCRIPTION_MONEY.amount))
                    assertThat(subscription.currency, CoreMatchers.`is`(TestConstants.SUBSCRIPTION_MONEY.currency))
                    assertThat(subscription.period, CoreMatchers.`is`(period.toSubscriptionPeriod()))
                    assertThat(subscription.initialAmount, CoreMatchers.`is`(initialAmount))
                    assertThat(subscription.mode, CoreMatchers.`is`(ProcessingMode.TEST))
                    assertThat(subscription.scheduleSettings.startOn, CoreMatchers.`is`(startOn))
                    assertThat(subscription.scheduleSettings.zoneId, CoreMatchers.`is`(zoneId))
                    assertThat(subscription.scheduleSettings.preserveEndOfMonth, CoreMatchers.`is`(preserveEndOfMonth))
                    assertThat(subscription.metadata["some_key"], CoreMatchers.`is`(TestConstants.METADATA["some_key"]))
            },
            failureTestCallback(latch),
            failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, paymentSettings)
        clickActionButton()
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings,cvvRequired = true, rememberCard = false)
        performPayment()
        clickActionButton()
    }

    @Test
    fun shouldTriggerErrorCallbackWhenFailToCreateSubscriptionCharge() = univapayTest(2) { latch->
        val paymentSettings = SubscriptionSettings(MONEY, SubscriptionPeriod.Monthly)
        val checkoutConfiguration = SubscriptionCheckout(
                paymentSettings,
                processFullPayment = true
        )

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
            .setTokenCallback(
                testCallback(latch){ token: TransactionTokenWithData? ->
                    assertThat(token!!.type, `is`(TransactionTokenType.SUBSCRIPTION))
                    assertThat(token.email, `is`(TEST_EMAIL))
                    assertThat(token.data.card.cardholder, `is`(TEST_USER))
                    assertThat(token.data.card.expMonth, `is`(Integer.valueOf(TEST_EXPIRY.substring(1,2))))
                    assertThat(token.data.card.expYear, `is`(Integer.valueOf(TEST_EXPIRY.substring(2,6))))},
                failureTestCallback(latch)
            ).setSubscriptionCallback(
                failureTestCallback(latch),
                testCallback(latch){ error: UnivapayError? ->
                    assertThat(error?.message, `is`(Constants.SUBSCRIPTION_FAILED_TO_CONFIRM_ERROR))
                },
                failureTestCallback(latch)
                ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, paymentSettings, cvvRequired = true, rememberCard = false)
        performPayment("4111111111111111")
    }
}
