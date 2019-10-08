package com.univapay.example

import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.performPayment
import com.univapay.example.utils.FragmentAssertions.performRecurringPayment
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.AMOUNT
import com.univapay.example.utils.TestConstants.Companion.CURRENCY
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTOR
import com.univapay.example.utils.TestConstants.Companion.METADATA
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils.failureTestCallback
import com.univapay.example.utils.TestUtils.testCallback
import com.univapay.example.utils.TestUtils.univapayTest
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.models.AppCredentials
import com.univapay.payments.*
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.RecurringTokenInterval
import com.univapay.sdk.types.TransactionTokenType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI
import java.util.*

@RunWith(AndroidJUnit4::class)
class CheckoutFullPaymentTest: UnivaPayAndroidTest() {

    @Test()
    fun shouldCreateWithOneTimeToken() = univapayTest(2){ latch->
        val settings = ChargeSettings(MONEY, descriptorSettings = DescriptorSettings(DESCRIPTOR))

        val checkoutConfiguration = OneTimeTokenCheckout(
                settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setTokenCallback(
                testCallback(latch){ transactionTokenWithData: TransactionTokenWithData? ->
                    assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.ONE_TIME))
                    assertThat(transactionTokenWithData.metadata, `is`(METADATA))
                },
                failureTestCallback(latch)
            ).setChargeCallback(
                testCallback(latch){ charge: Charge? ->
                    assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.ONE_TIME))
                    assertThat(charge.metadata, `is`(METADATA))
//                    assertThat(charge.descriptor, `is`(DESCRIPTOR))
                },
                failureTestCallback(latch),
                failureTestCallback(latch)
            ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
        performPayment()
    }

    @Test()
    fun shouldCreateWithOneTimeTokenRememberingData() = univapayTest(2){ latch->
        val settings = ChargeSettings(MONEY, descriptorSettings = DescriptorSettings(DESCRIPTOR))

        val checkoutConfiguration = OneTimeTokenCheckout(
                settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .rememberCardsForUser(UUID.randomUUID())
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                        testCallback(latch){ transactionTokenWithData: TransactionTokenWithData? ->
                            assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.RECURRING))
                            assertThat(transactionTokenWithData.metadata["some_key"], `is`(METADATA["some_key"]))
                        },
                        failureTestCallback(latch)
                ).setChargeCallback(
                        //                          in case of a successful transaction:
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                            assertThat(charge.metadata["some_key"], `is`(METADATA["some_key"]))
//                            assertThat(charge.descriptor, `is`(DESCRIPTOR))
                        },
                        //                          in case of error
                        failureTestCallback(latch),
                        //                          in case the charge's status could not be properly verified
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = true)
        performPayment(shouldRememberCard = true)
    }

    @Test()
    fun shouldCreateWithOneTimeTokenRememberingDataWithUsageLimit() = univapayTest(2){ latch->
        val settings = ChargeSettings(MONEY, descriptorSettings = DescriptorSettings(DESCRIPTOR))
        val checkoutConfiguration = OneTimeTokenCheckout(settings, processFullPayment = true)

        val recurringTokenUsageLimit = RecurringTokenInterval.WEEKLY

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .rememberCardsForUser(UUID.randomUUID())
                .setRecurringTokenUsageLimit(recurringTokenUsageLimit)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                        testCallback(latch){ transactionTokenWithData: TransactionTokenWithData? ->
                            assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.RECURRING))
                            assertThat(transactionTokenWithData.usageLimit, `is`(recurringTokenUsageLimit))
                            assertThat(transactionTokenWithData.metadata["some_key"], `is`(METADATA["some_key"]))
                        },
                        failureTestCallback(latch)
                ).setChargeCallback(
                        //                          in case of a successful transaction:
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                            assertThat(charge.metadata["some_key"], `is`(METADATA["some_key"]))
//                            assertThat(charge.descriptor, `is`(DESCRIPTOR))
                        },
                        //                          in case of error
                        failureTestCallback(latch),
                        //                          in case the charge's status could not be properly verified
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = true)
        performPayment(shouldRememberCard = true)
    }

    @Test
    fun shouldCreateWithRecurringToken() = univapayTest(2){ latch->
        val settings = ChargeSettings(MONEY, descriptorSettings = DescriptorSettings(DESCRIPTOR))
        val checkoutConfiguration = RecurringTokenCheckout(
                settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                        testCallback(latch){token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                            assertThat(token.metadata, `is`(METADATA))
                        },
                        failureTestCallback(latch)
                )
            .setChargeCallback(
                testCallback(latch){ charge: Charge? ->
                    assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                    assertThat(charge.chargedAmount, `is`(AMOUNT))
                    assertThat(charge.chargedCurrency, `is`(CURRENCY))
                    assertThat(charge.metadata, `is`(METADATA))
//                    assertThat(charge.descriptor, `is`(DESCRIPTOR))
        },
                failureTestCallback(latch),
                failureTestCallback(latch)
            ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
        performRecurringPayment()
    }

    @Test
    fun shouldCreateWithRecurringTokenWithUsageLimit() = univapayTest(2){ latch->
        val settings = ChargeSettings(MONEY, descriptorSettings = DescriptorSettings(DESCRIPTOR))
        val checkoutConfiguration = RecurringTokenCheckout(
                settings, processFullPayment = true)
        val recurringTokenUsageLimit = RecurringTokenInterval.DAILY

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setRecurringTokenUsageLimit(recurringTokenUsageLimit)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                        testCallback(latch){token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                            assertThat(token.usageLimit, `is`(recurringTokenUsageLimit))
                            assertThat(token.metadata, `is`(METADATA))
                        },
                        failureTestCallback(latch)
                )
                .setChargeCallback(
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                            assertThat(charge.chargedAmount, `is`(AMOUNT))
                            assertThat(charge.chargedCurrency, `is`(CURRENCY))
                            assertThat(charge.metadata, `is`(METADATA))
//                            assertThat(charge.descriptor, `is`(DESCRIPTOR))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
        performRecurringPayment()
    }

    @Test
    fun shouldCreateWithRecurringTokenRememberingData() = univapayTest(2){ latch->
        val settings = ChargeSettings(MONEY, descriptorSettings = DescriptorSettings(DESCRIPTOR))
        val checkoutConfiguration = RecurringTokenCheckout(
                settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .rememberCardsForUser(UUID.randomUUID())
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                        testCallback(latch){token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                            assertThat(token.metadata["some_key"], `is`(METADATA["some_key"]))
                        },
                        failureTestCallback(latch)
                )
                .setChargeCallback(
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                            assertThat(charge.chargedAmount, `is`(AMOUNT))
                            assertThat(charge.chargedCurrency, `is`(CURRENCY))
                            assertThat(charge.metadata["some_key"], `is`(METADATA["some_key"]))
//                            assertThat(charge.descriptor, `is`(DESCRIPTOR))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = true)
        performPayment(shouldRememberCard = true)
    }

    @Test
    fun shouldCreateWithRecurringTokenRememberingDataWithUsageLimit() = univapayTest(2){ latch->
        val settings = ChargeSettings(MONEY, descriptorSettings = DescriptorSettings(DESCRIPTOR))
        val checkoutConfiguration = RecurringTokenCheckout(
                settings, processFullPayment = true)
        val recurringTokenUsageLimit = RecurringTokenInterval.DAILY

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setRecurringTokenUsageLimit(recurringTokenUsageLimit)
                .rememberCardsForUser(UUID.randomUUID())
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                        testCallback(latch){token: TransactionTokenWithData? ->
                            assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                            assertThat(token.usageLimit, `is`(recurringTokenUsageLimit))
                            assertThat(token.metadata["some_key"], `is`(METADATA["some_key"]))
                        },
                        failureTestCallback(latch)
                )
                .setChargeCallback(
                        testCallback(latch){ charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, `is`(TransactionTokenType.RECURRING))
                            assertThat(charge.chargedAmount, `is`(AMOUNT))
                            assertThat(charge.chargedCurrency, `is`(CURRENCY))
                            assertThat(charge.metadata["some_key"], `is`(METADATA["some_key"]))
//                            assertThat(charge.descriptor, `is`(DESCRIPTOR))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = true)
        performPayment(shouldRememberCard = true)
    }

    @Test
    fun shouldCreateWithSubscription() = univapayTest(2) { latch->
        val settings = SubscriptionSettings(MONEY, SubscriptionPeriod.Monthly)
                .withDescriptor(DESCRIPTOR)

        val checkoutConfiguration = SubscriptionCheckout(
                settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setTokenCallback(
                testCallback(latch){ transactionTokenWithData: TransactionTokenWithData? ->
                    assertThat(transactionTokenWithData!!.type, `is`(TransactionTokenType.SUBSCRIPTION))
                    assertThat(transactionTokenWithData.metadata, `is`(METADATA))
                },
                failureTestCallback(latch)
            )
                .setSubscriptionCallback(
                testCallback(latch){ subscription: FullSubscription? ->
                    assertThat(subscription!!.amount, `is`(AMOUNT))
                    assertThat(subscription.period, `is`(com.univapay.sdk.types.SubscriptionPeriod.MONTHLY))
                    assertThat(subscription.metadata, `is`(METADATA))
                    assertThat(subscription.installmentPlan, `is`(nullValue()))
//                    assertThat(subscription.descriptor, `is`(DESCRIPTOR))
            },
                failureTestCallback(latch),
                failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
        performPayment()
    }
}
