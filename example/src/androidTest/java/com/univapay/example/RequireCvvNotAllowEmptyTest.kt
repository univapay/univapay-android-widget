package com.univapay.example

import android.support.test.runner.AndroidJUnit4
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertIsDisplayed
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.example.utils.TestUtils
import com.univapay.models.AppCredentials
import com.univapay.payments.*
import com.univapay.sdk.models.common.MoneyLike
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger

/**
 * For all test methods of this class, the cvv form should always be displayed because the merchant requires.
 */
@RunWith(AndroidJUnit4::class)
class RequireCvvNotAllowEmptyTest: UnivaPayAndroidTest() {

    private val credentials: AppCredentials by lazy {
        AppCredentials(appJWT.jwt)
    }

    private val testMoney = MoneyLike(BigInteger.valueOf(1000), "JPY")

    private fun cvvIsDisplayed() = assertIsDisplayed(R.id.card_detail_edit_text_security)

    /*
      Factors
    ・cvv threshold -> always lower for this class
    ・6 token types
    ・builder.setCvvRequired
     */

    @Test
    fun shouldDisplayCvvIfNotRequiredOnOneTimeTokenCheckout() {
        checkout = TestUtils.createCheckout(OneTimeTokenCheckout(ChargeSettings(testMoney)), false, credentials)
        showAndWait(checkout)

        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfRequiredOnOneTimeTokenCheckout() {
        checkout = TestUtils.createCheckout(OneTimeTokenCheckout(ChargeSettings(testMoney)), true, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfNotRequiredOnOneTimeTokenPaymentCheckout() {
        checkout = TestUtils.createCheckout(OneTimeTokenCheckout(ChargeSettings(testMoney)), false, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfRequiredOnOneTimeTokenPaymentCheckout() {
        checkout = TestUtils.createCheckout(OneTimeTokenCheckout(ChargeSettings(testMoney)), true, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfNotRequiredOnRecurringTokenCheckout() {
        checkout = TestUtils.createCheckout(RecurringTokenCheckout(ChargeSettings(testMoney)), false, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfRequiredOnRecurringTokenCheckout() {
        checkout = TestUtils.createCheckout(RecurringTokenCheckout(ChargeSettings(testMoney)), true, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfNotRequiredOnRecurringTokenPaymentCheckout() {
        checkout = TestUtils.createCheckout(RecurringTokenCheckout(ChargeSettings(testMoney), processFullPayment = true), false, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfRequiredOnRecurringTokenPaymentCheckout() {
        checkout = TestUtils.createCheckout(RecurringTokenCheckout(ChargeSettings(testMoney), processFullPayment = true), true, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfNotRequiredOnSubscriptionTokenCheckout() {
        checkout = TestUtils.createCheckout(SubscriptionCheckout(SubscriptionSettings(testMoney, SubscriptionPeriod.Daily)), false, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfRequiredOnSubscriptionTokenCheckout() {
        checkout = TestUtils.createCheckout(SubscriptionCheckout(SubscriptionSettings(testMoney, SubscriptionPeriod.Daily)), true, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfNotRequiredOnSubscriptionPaymentCheckout() {
        checkout = TestUtils.createCheckout(
                SubscriptionCheckout(SubscriptionSettings(testMoney, SubscriptionPeriod.Daily), processFullPayment = true),
                false, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }

    @Test
    fun shouldDisplayCvvIfRequiredOnSubscriptionPaymentCheckout() {
        checkout = TestUtils.createCheckout(
            SubscriptionCheckout(SubscriptionSettings(testMoney, SubscriptionPeriod.Daily), processFullPayment = true),
                true, credentials)
        showAndWait(checkout)
        cvvIsDisplayed()
    }
}
