package com.univapay.example

import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertAddressFragment
import com.univapay.example.utils.FragmentAssertions.assertSubscriptionWithInstallmentDetails
import com.univapay.example.utils.FragmentAssertions.clickActionButton
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.TEST_ORIGIN_URL
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.models.AppCredentials
import com.univapay.payments.*
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI

@RunWith(AndroidJUnit4::class)
class RequireBillingAddressTest: UnivaPayAndroidTest() {

    @Test
    fun shouldDisplayAddressWithOneTimeTokenAndTokenOnly() {
        val settings = ChargeSettings(MONEY)
        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings))
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setAddress(true)
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setOrigin(URI(TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldDisplayAddressWithRecurringTokenAndTokenOnly() {
        val settings = ChargeSettings(MONEY)
        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt),
                RecurringTokenCheckout(settings))
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setAddress(true)
            .setOrigin(URI(TEST_ORIGIN_URL))
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldDisplayAddressWithSubscriptionAndTokenOnly() {
        val settings = SubscriptionSettings(MONEY, SubscriptionPeriod.Daily)
        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt),
                SubscriptionCheckout(settings))
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setAddress(true)
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setOrigin(URI(TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldDisplayAddressWithOneTimeTokenAndFullPayments() {
        val settings = ChargeSettings(MONEY)
        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setAddress(true)
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setOrigin(URI(TEST_ORIGIN_URL))
            .build()

        showAndWait(checkout)
        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldDisplayAddressWithRecurringTokenAndFullPayments() {
        val settings = ChargeSettings(MONEY)
        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt),
                RecurringTokenCheckout(settings, processFullPayment = true))
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setAddress(true)
            .setOrigin(URI(TEST_ORIGIN_URL))
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldDisplayAddressWithSubscriptionAndFullPayments() {
        val settings = SubscriptionSettings(MONEY, SubscriptionPeriod.Daily)
                .withInstallmentPlan(FixedCycleInstallmentsPlan(3))

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt),
            SubscriptionCheckout(
                settings,
                    processFullPayment = true
            ))
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setAddress(true)
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setOrigin(URI(TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
        clickActionButton()
        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }
}
