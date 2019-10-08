package com.univapay.example

import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.assertThat
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.*
import com.univapay.example.utils.FragmentAssertions.assertBottomSheetView
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.assertKonbiniFragment
import com.univapay.example.utils.FragmentAssertions.assertPaymentTypesFragment
import com.univapay.example.utils.FragmentAssertions.assertQRImageView
import com.univapay.example.utils.FragmentAssertions.assertStoredDataEntriesListFragment
import com.univapay.example.utils.FragmentAssertions.assertStoredKonbiniView
import com.univapay.example.utils.FragmentAssertions.assertSubscriptionWithInstallmentDetails
import com.univapay.example.utils.FragmentAssertions.clickActionButton
import com.univapay.example.utils.FragmentAssertions.fillKonbiniDetails
import com.univapay.example.utils.FragmentAssertions.goToKonbiniPayment
import com.univapay.example.utils.FragmentAssertions.performKonbiniPayment
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.METADATA
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.SUBSCRIPTION_MONEY
import com.univapay.example.utils.TestConstants.Companion.TEST_ENDPOINT
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils.createKonbiniData
import com.univapay.example.utils.TestUtils.failureTestCallback
import com.univapay.example.utils.TestUtils.testCallback
import com.univapay.example.utils.TestUtils.univapayTest
import com.univapay.models.AppCredentials
import com.univapay.payments.*
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.ProcessingMode
import com.univapay.sdk.types.TransactionTokenType
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.joda.time.LocalDate
import org.joda.time.Period
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import org.threeten.bp.ZoneId
import java.math.BigInteger
import java.net.URI
import java.util.*

class KonbiniPaymentEntryTest: UnivaPayAndroidTest(){

    @Test
    fun shouldDisplayPaymentTypesSelectionScreenIfKonibiniPaymentsEnabled(){
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldTakeCustomerToCardPaymentIfKonbiniPaymentsNotEnabled(){
        val settings = ChargeSettings(TestConstants.MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
    }

    @Test
    fun shouldFillKonbiniDetails(){

        val settings = ChargeSettings(TestConstants.MONEY)

        allowKonbiniPayments()

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        assertKonbiniFragment(TITLE, DESCRIPTION, settings)
        fillKonbiniDetails(getRandomKonbiniPosition())
    }

    @Test
    fun shouldMakeTokenOnlyOneTimeKonbiniCheckout() = TestUtils.univapayTest(1) { latch ->
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .setTokenCallback(
                        TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            ViewMatchers.assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.ONE_TIME))
                        },
                        TestUtils.failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        performKonbiniPayment(TITLE, DESCRIPTION, settings, getRandomKonbiniPosition())
    }

    @Test
    fun shouldMakeTokenOnlyRecurringKonbiniCheckout() = TestUtils.univapayTest(1) { latch ->
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                RecurringTokenCheckout(settings)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .setTokenCallback(
                        TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            ViewMatchers.assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.RECURRING))
                        },
                        TestUtils.failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        performKonbiniPayment(TITLE, DESCRIPTION, settings, getRandomKonbiniPosition())
    }

    @Test
    fun shouldMakeTokenOnlySubscriptionKonbiniCheckout() = TestUtils.univapayTest(1) { latch ->
        allowKonbiniPayments()

        val settings = SubscriptionSettings(TestConstants.MONEY, SubscriptionPeriod.Daily)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                SubscriptionCheckout(
                        settings
                )
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .setTokenCallback(
                        TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            ViewMatchers.assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.SUBSCRIPTION))
                        },
                        TestUtils.failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        performKonbiniPayment(TITLE, DESCRIPTION, settings, getRandomKonbiniPosition())
    }

    @Test
    fun shouldMakeOneTimeTokenKonbiniCharge() = TestUtils.univapayTest(2) { latch ->
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(
                        settings,
                        processFullPayment = true)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setTokenCallback(
                        TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.ONE_TIME))
                            assertThat(transactionTokenWithData.metadata, `is`(METADATA))
                        },
                        TestUtils.failureTestCallback(latch)
                ).setChargeCallback(
                        //                          in case of a successful transaction:
                        TestUtils.testCallback(latch) { charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, Matchers.`is`(TransactionTokenType.ONE_TIME))
                            assertThat(charge.metadata, `is`(METADATA))
                        },
                        TestUtils.failureTestCallback(latch),
                        TestUtils.failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        performKonbiniPayment(TITLE, DESCRIPTION, settings, getRandomKonbiniPosition())
        clickActionButton()
    }

    @Test
    fun shouldMakeRecurringTokenKonbiniCharge() = TestUtils.univapayTest(2) { latch ->
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                RecurringTokenCheckout(
                        settings,
                        processFullPayment = true
                )
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setTokenCallback(
                        testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.RECURRING))
                            assertThat(transactionTokenWithData.metadata, `is`(METADATA))
                        },
                        failureTestCallback(latch)
                ).setChargeCallback(
                        testCallback(latch) { charge: Charge? ->
                            assertThat(charge!!.transactionTokenType, Matchers.`is`(TransactionTokenType.RECURRING))
                            assertThat(charge.metadata, `is`(METADATA))
                        },
                        failureTestCallback(latch),
                        failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        performKonbiniPayment(TITLE, DESCRIPTION, settings, getRandomKonbiniPosition())
        clickActionButton()
    }

    @Test
    fun shouldCreateKonbiniSubscription() = TestUtils.univapayTest(2) { latch ->
        allowKonbiniPayments()

        val settings = SubscriptionSettings(TestConstants.MONEY, SubscriptionPeriod.Monthly)

        val subscriptionPeriod = SubscriptionPeriod.Monthly

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                SubscriptionCheckout(
                        settings,
                        processFullPayment = true)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .setMetadata(METADATA)
                .setTokenCallback(
                        testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.SUBSCRIPTION))
                            assertThat(transactionTokenWithData.metadata, `is`(METADATA))
                        },
                        failureTestCallback(latch)
                )
                .setSubscriptionCallback(
                        //                          in case of a successful transaction:
                        testCallback(latch) { subscription: FullSubscription? ->
                            assertThat(subscription!!.amount, `is`(TestConstants.MONEY.amount))
                            assertThat(subscription.period, `is`(subscriptionPeriod.toSubscriptionPeriod()))
                            assertThat(subscription.metadata, `is`(METADATA))
                        },
                        //                          in case of error
                        failureTestCallback(latch),
                        //                          in case the charge's status could not be properly verified
                        failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        performKonbiniPayment(TITLE, DESCRIPTION, settings, getRandomKonbiniPosition())
    }

    @Test
    fun shouldFillKonbiniDetailsRememberCard(){

        val customerId = getRandomCustomerID()

        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        assertKonbiniFragment(TITLE, DESCRIPTION, settings, true)
        fillKonbiniDetails(getRandomKonbiniPosition(), true)
    }

    @Test
    fun shouldDisplayStoredKonbiniDetails(){
        val customerId = getRandomCustomerID()
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        val token = TestUtils.createTxToken(univapay, appJWT, createKonbiniData(), customerId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(
                        settings,
                        processFullPayment = true)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        //TODO temporary unable
        //assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        //assertStoredKonbiniView(token)
    }

    @Test
    fun shouldTakeCustomerToNewKonbiniForm(){
        val customerId = getRandomCustomerID()
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        val token = TestUtils.createTxToken(univapay, appJWT, createKonbiniData(), customerId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(
                        settings,
                        processFullPayment = true)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        //TODO temporary unable
//        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
//        assertStoredKonbiniView(token)
        clickActionButton()

        assertKonbiniFragment(TITLE, DESCRIPTION, settings, true)
    }

    @Ignore("fixme")
    @Test
    fun shouldPerformChargePaymentWithStoredKonbiniDetails() = TestUtils.univapayTest(1) { latch ->
        val customerId = getRandomCustomerID()
        allowKonbiniPayments()

        val settings = ChargeSettings(TestConstants.MONEY)

        val token = TestUtils.createTxToken(univapay, appJWT, createKonbiniData(), customerId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(
                        settings,
                        processFullPayment = true)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setMetadata(METADATA)
                .setTokenCallback(
                    TestUtils.testCallback(latch) {
                        Assert.fail("Should not create a new token")
                    },
                    TestUtils.testCallback(latch) {
                        Assert.fail("Should not create a new token")
                    }
                )
                .setChargeCallback(
                    //                          in case of a successful transaction:
                    TestUtils.testCallback(latch) { charge: Charge? ->
                        MatcherAssert.assertThat(charge!!.transactionTokenType, Matchers.`is`(TransactionTokenType.RECURRING))
                        assertThat(charge.metadata["some_key"], `is`(METADATA["some_key"]))
                    },
                    //                          in case of error
                    TestUtils.failureTestCallback(latch),
                    //                          in case the charge's status could not be properly verified
                    TestUtils.failureTestCallback(latch)
                )
                .setSubscriptionCallback(
                    TestUtils.failureTestCallback(latch),
                    TestUtils.failureTestCallback(latch),
                    TestUtils.failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)

        goToKonbiniPayment()
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        assertStoredKonbiniView(token)

        Espresso.onView(ViewMatchers.withId(R.id.stored_konbini_paper_card)).perform(ViewActions.click())
        assertBottomSheetView()
        Espresso.onView(ViewMatchers.withId(R.id.stored_data_bottom_pay)).perform(ViewActions.click())
    }

    @Ignore("fixme")
    @Test
    fun shouldCreateSubscriptionWithStoredKonbiniDetails() = TestUtils.univapayTest(1) { latch ->
        val customerId = getRandomCustomerID()
        allowKonbiniPayments()

        val token = TestUtils.createTxToken(univapay, appJWT, createKonbiniData(), customerId)

        val zoneId = ZoneId.of("America/Cancun")
        val startOn = LocalDate.now().plusDays(10)
        val preserveEndOfMonth = true
        val period = SubscriptionPeriod.Monthly
        val initialAmount = BigInteger.valueOf(1000)

        val settings = SubscriptionSettings(TestConstants.SUBSCRIPTION_MONEY, period)
                .withInitialAmount(initialAmount)
                .withInstallmentPlan(FixedCycleInstallmentsPlan(7))
                .withStartOn(startOn)
                .withPreserveEndOfMonth(preserveEndOfMonth)
                .withZoneId(zoneId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                SubscriptionCheckout(
                        settings,
                        processFullPayment = true
                )
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setMetadata(METADATA)
                .setTokenCallback(
                    TestUtils.failureTestCallback(latch),
                    TestUtils.failureTestCallback(latch)
                )
                .setChargeCallback(
                    failureTestCallback(latch),
                    failureTestCallback(latch),
                    failureTestCallback(latch)
                )
                .setSubscriptionCallback(
                    //                          in case of a successful transaction:
                    testCallback(latch) { subscription: FullSubscription? ->
                        assertThat(subscription!!.amount, `is`(SUBSCRIPTION_MONEY.amount))
                        assertThat(subscription.currency, `is`(SUBSCRIPTION_MONEY.currency))
                        assertThat(subscription.period, `is`(period.toSubscriptionPeriod()))
                        assertThat(subscription.initialAmount, `is`(initialAmount))
                        assertThat(subscription.mode, `is`(ProcessingMode.TEST))
                        assertThat(subscription.scheduleSettings.startOn, `is`(startOn))
                        assertThat(subscription.scheduleSettings.zoneId, `is`(zoneId))
                        assertThat(subscription.scheduleSettings.preserveEndOfMonth, `is`(preserveEndOfMonth))
                        assertThat(subscription.metadata["some_key"], `is`(METADATA["some_key"]))
                    },
                    //                          in case of error
                    failureTestCallback(latch),
                    //                          in case the charge's status could not be properly verified
                    failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
        clickActionButton()
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        assertStoredKonbiniView(token)

        Espresso.onView(ViewMatchers.withId(R.id.stored_konbini_paper_card)).perform(ViewActions.click())
        assertBottomSheetView()
        Espresso.onView(ViewMatchers.withId(R.id.stored_data_bottom_pay)).perform(ViewActions.click())
    }

    @Ignore("fixme")
    @Test
    fun shouldDisplayQRImage(){
        val customerId = getRandomCustomerID()
        allowKonbiniPayments()

        val token = TestUtils.createTxToken(univapay, appJWT, createKonbiniData(), customerId)

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(
                        settings,
                        processFullPayment = true)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setMetadata(METADATA)
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)

        goToKonbiniPayment()
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        assertStoredKonbiniView(token)

        Espresso.onView(ViewMatchers.withId(R.id.stored_konbini_paper_card)).perform(ViewActions.click())
        assertBottomSheetView()
        Espresso.onView(ViewMatchers.withId(R.id.stored_data_bottom_qr)).perform(ViewActions.click())
        Thread.sleep(500)
        assertQRImageView()
    }

    @Ignore("fixme")
    @Test
    fun shouldDeleteStoredEntry(){
        val customerId = getRandomCustomerID()
        allowKonbiniPayments()

        val token = TestUtils.createTxToken(univapay, appJWT, createKonbiniData(), customerId)

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(
                        settings,
                        processFullPayment = true)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setMetadata(METADATA)
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)

        goToKonbiniPayment()
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        assertStoredKonbiniView(token)

        Espresso.onView(ViewMatchers.withId(R.id.stored_konbini_paper_card)).perform(ViewActions.click())
        assertBottomSheetView()
        Espresso.onView(ViewMatchers.withId(R.id.stored_data_bottom_delete)).perform(ViewActions.click())
        Thread.sleep(1000)
        Espresso.onView(ViewMatchers.withId(R.id.stored_konbini_paper_card)).check(ViewAssertions.doesNotExist())
    }

    @Test
    fun shouldSetKonbiniPaymentExpirationDate() = TestUtils.univapayTest(1){ latch ->
        allowKonbiniPayments()

//      The payment expires in 15 days from today
        val expirationPeriod: Period = Period.days(15)

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .setConvenienceStorePaymentExpirationDate(expirationPeriod)
                .setTokenCallback(
                        TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            ViewMatchers.assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.ONE_TIME))
                            val konbiniPaymentData = transactionTokenWithData.data.asKonbiniPaymentData()
                            assertThat(konbiniPaymentData.expirationPeriod, `is`( expirationPeriod))
                        },
                        TestUtils.failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        assertKonbiniFragment(TITLE, DESCRIPTION, settings)
        performKonbiniPayment(TITLE, DESCRIPTION, settings,  1)
    }

    @Test
    fun shouldCreateSubscriptionRememberingCardDetails() = univapayTest(2){ latch->

        allowKonbiniPayments()

        val settings = SubscriptionSettings(TestConstants.MONEY, SubscriptionPeriod.Monthly)

        val subscriptionPeriod = SubscriptionPeriod.Monthly
        val customerId = UUID.randomUUID()

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                SubscriptionCheckout(
                        settings,
                        processFullPayment = true)
        ).setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setTokenCallback(
                        TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                            MatcherAssert.assertThat(transactionTokenWithData!!.type, Matchers.`is`(TransactionTokenType.RECURRING))
                            assertThat(transactionTokenWithData.metadata["univapay-customer-id"], `is`(customerId.toString()))
                        },
                        TestUtils.failureTestCallback(latch)
                )
                .setSubscriptionCallback(
                        //                          in case of a successful transaction:
                        TestUtils.testCallback(latch) { subscription: FullSubscription? ->
                            assertThat(subscription!!.amount, `is`(TestConstants.MONEY.amount))
                            assertThat(subscription.period, `is`(subscriptionPeriod.toSubscriptionPeriod()))
                        },
                        //                          in case of error
                        TestUtils.failureTestCallback(latch),
                        //                          in case the charge's status could not be properly verified
                        TestUtils.failureTestCallback(latch)
                )
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToKonbiniPayment()
        performKonbiniPayment(TITLE, DESCRIPTION, settings, getRandomKonbiniPosition(), true)
    }
}
