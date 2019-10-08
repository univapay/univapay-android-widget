package com.univapay.example

import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.assertSubscriptionWithInstallmentDetails
import com.univapay.example.utils.FragmentAssertions.clickActionButton
import com.univapay.example.utils.FragmentAssertions.performPayment
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.SUBSCRIPTION_MONEY
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.models.AppCredentials
import com.univapay.payments.SubscriptionCheckout
import com.univapay.payments.SubscriptionSettings
import com.univapay.sdk.models.request.subscription.FixedCycleAmountInstallmentsPlan
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import com.univapay.sdk.models.request.subscription.RevolvingInstallmentsPlan
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.TransactionTokenType
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.joda.time.LocalDate
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger
import java.net.URI

@RunWith(AndroidJUnit4::class)
class CreateSubscriptionTest: UnivaPayAndroidTest() {
    @Ignore("Be enable after API is fixed")
    @Test
    fun shouldCreateFixedCyclesSubscription() = TestUtils.univapayTest(2) { latch ->
        val date = LocalDate.now().plusDays(2)
        val settings = SubscriptionSettings(SUBSCRIPTION_MONEY, SubscriptionPeriod.Monthly)
            .withInstallmentPlan(FixedCycleInstallmentsPlan(12))
            .withInitialAmount(BigInteger.valueOf(2000))
            .withStartOn(date)

        val checkoutConfiguration = SubscriptionCheckout(settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
            .setTitle(TestConstants.TITLE)
            .setDescription(TestConstants.DESCRIPTION)
            .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
            .setMetadata(TestConstants.METADATA)
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setTokenCallback(
                TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                    assertThat(transactionTokenWithData!!.type, `is`(TransactionTokenType.SUBSCRIPTION))
                    assertThat(transactionTokenWithData.metadata, `is`(TestConstants.METADATA))
                }, TestUtils.failureTestCallback(latch))
            .setSubscriptionCallback(
                TestUtils.testCallback(latch) { subscription: FullSubscription? ->
                    assertThat(subscription!!.amount, `is`(TestConstants.SUBSCRIPTION_AMOUNT))
                    assertThat(subscription.period, `is`(com.univapay.sdk.types.SubscriptionPeriod.MONTHLY))
                    assertThat(subscription.metadata, `is`(TestConstants.METADATA))
                    assertThat(subscription.installmentPlan.planType, `is`(settings.installmentPlan!!.planType))
                    assertThat(subscription.installmentPlan.fixedCycles, `is`(settings.installmentPlan!!.fixedCycles))
                    assertThat(subscription.initialAmount, `is`(settings.initialAmount))
                    assertThat(subscription.scheduleSettings.startOn, `is`(date))
                }, TestUtils.failureTestCallback(latch), TestUtils.failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
        clickActionButton()
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
        performPayment()
        clickActionButton()
    }

    @Ignore("Be enable after API is fixed")
    @Test
    fun shouldCreateFixedCycleAmountSubscription() = TestUtils.univapayTest(2) { latch ->
        val date = LocalDate.now().plusDays(50)
        val settings = SubscriptionSettings(SUBSCRIPTION_MONEY, SubscriptionPeriod.Monthly)
            .withInstallmentPlan(FixedCycleAmountInstallmentsPlan(BigInteger.valueOf(50000)))
            .withInitialAmount(BigInteger.valueOf(20000))
            .withStartOn(date)

        val checkoutConfiguration = SubscriptionCheckout(settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
            .setTitle(TestConstants.TITLE)
            .setDescription(TestConstants.DESCRIPTION)
            .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
            .setMetadata(TestConstants.METADATA)
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setTokenCallback(
                TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                    assertThat(transactionTokenWithData!!.type, `is`(TransactionTokenType.SUBSCRIPTION))
                    assertThat(transactionTokenWithData.metadata, `is`(TestConstants.METADATA))
                }, TestUtils.failureTestCallback(latch))
            .setSubscriptionCallback(
                TestUtils.testCallback(latch) { subscription: FullSubscription? ->
                    assertThat(subscription!!.amount, `is`(TestConstants.SUBSCRIPTION_AMOUNT))
                    assertThat(subscription.period, `is`(com.univapay.sdk.types.SubscriptionPeriod.MONTHLY))
                    assertThat(subscription.metadata, `is`(TestConstants.METADATA))
                    assertThat(subscription.installmentPlan.planType, `is`(settings.installmentPlan!!.planType))
                    assertThat(subscription.installmentPlan.fixedCycleAmount, `is`(settings.installmentPlan!!.fixedCycleAmount))
                    assertThat(subscription.initialAmount, `is`(settings.initialAmount))
                    assertThat(subscription.scheduleSettings.startOn, `is`(date))
                }, TestUtils.failureTestCallback(latch), TestUtils.failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
        clickActionButton()
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
        performPayment()
        clickActionButton()
    }

    @Test
    fun shouldDisplayProperlyRevolvingSubscription() = TestUtils.univapayTest(0) { _ ->
        val date = LocalDate.now().plusDays(50)
        val settings = SubscriptionSettings(SUBSCRIPTION_MONEY, SubscriptionPeriod.Monthly)
                .withInstallmentPlan(RevolvingInstallmentsPlan())
                .withInitialAmount(BigInteger.valueOf(20000))
                .withStartOn(date)

        val checkoutConfiguration = SubscriptionCheckout(settings, processFullPayment = true)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setMetadata(TestConstants.METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .build()

        showAndWait(checkout)
//      It should direct to the card details fragment, skipping the subscription details screen.
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
    }
}
