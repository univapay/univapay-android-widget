package com.univapay.example

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import com.univapay.UnivapayCheckout
import com.univapay.example.utils.FragmentAssertions.assertStoredCardView
import com.univapay.example.utils.FragmentAssertions.assertStoredDataEntriesListFragment
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.TEST_ENDPOINT
import com.univapay.example.utils.TestConstants.Companion.TEST_ORIGIN_URL
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.models.AppCredentials
import com.univapay.payments.ChargeSettings
import com.univapay.payments.OneTimeTokenCheckout
import com.univapay.payments.PaymentSettings
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.common.auth.AppJWTStrategy
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import org.hamcrest.Matchers
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger
import java.net.URI
import java.util.*

/***
 * Tests whether the Stored Cards Fragment requires a CVV when making a payment with a stored card
 * if the amount exceeds the threshold or if the merchant requires a CVV.
 */
@Ignore("fixme")
@RunWith(AndroidJUnit4::class)
class RequireCvvThresholdTest: UnivaPayAndroidTest() {

    private val highAmount: BigInteger = TestConstants.jpyLimit.plus(BigInteger.valueOf(1000))
    private val lowAmount: BigInteger = TestConstants.jpyLimit.minus(BigInteger.valueOf(1000))
    private val limitEUR: BigInteger by lazy {
        univapay.clone(AppJWTStrategy(appJWT.jwt)).convertMoney(
                MoneyLike(TestConstants.jpyLimit, "JPY"),
                "EUR")
                .build()
                .dispatch()
                .amount
    }

    private val highAmountEUR: BigInteger = limitEUR.plus(BigInteger.valueOf(500))
    private val lowAmountEUR: BigInteger = limitEUR.minus(BigInteger.valueOf(500))

    /*
      Factors
    ・cvv threshold
      The merchant has configurations, and checkout amount is higher than it or not
      The merchant doesn't have configurations, and checkout amount is higher than the amount of default currency(platform configuration) rate or not
    ・6 token types
    ・builder.setCvvRequired
    */

    private fun assertCVVDialog(token: TransactionTokenWithData, settings: PaymentSettings, displayed: Boolean){
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            ViewMatchers.assertThat((view as RecyclerView).adapter.itemCount, Matchers.`is`(1))
        }
        assertStoredCardView(token)
        onView(withId(R.id.stored_card_paper)).perform(ViewActions.click())
        onView(withId(R.id.stored_data_bottom_pay)).perform(ViewActions.click())

        if(displayed){
            onView(withText(R.string.checkout_recurring_token_cvv_challenge_title)).check(matches(isDisplayed()))
        } else {
            onView(withText(R.string.checkout_recurring_token_cvv_challenge_title)).check(ViewAssertions.doesNotExist())
        }
    }

    @Test
    fun shouldRequireCVVIfHigherThanThresholdOneTimeTokenCheckout(){
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(highAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings, true)
    }

    @Test
    fun shouldNotRequireCVVIfLowerThanThresholdOneTimeTokenCheckout() {
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(lowAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings, false)
    }

    @Test
    fun shouldRequireCVVIfHigherThanThresholdOneTimePaymentCheckout(){
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(highAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings, true)
    }

    @Test
    fun shouldRequireCVVIfLowerThanThresholdOneTimePaymentCheckout() {
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(lowAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings,false)
    }

    @Test
    fun shouldRequireCVVIfHigherThanThresholdRecurringTokenCheckout(){
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(highAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings, true)
    }

    @Test
    fun shouldNotRequireCVVIfLowerThanThresholdRecurringTokenCheckout() {
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(lowAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings,false)
    }

    @Test
    fun shouldRequireCVVIfHigherThanThresholdRecurringPaymentCheckout(){
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(highAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings, true)
    }

    @Test
    fun shouldRequireCVVIfLowerThanThresholdRecurringPaymentCheckout() {
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(lowAmount, "JPY"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings,false)
    }

    @Test
    fun shouldRequireCVVIfHigherThanThresholdOneTimeTokenCheckoutOtherCurrency(){
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(highAmountEUR, "EUR"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings,true)
    }

    @Test
    fun shouldRequireCVVIfLowerThanThresholdOneTimeTokenCheckoutOtherCurrency() {
        val customerId = UUID.randomUUID()
        val token = TestUtils.createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MoneyLike(lowAmountEUR, "EUR"))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings, processFullPayment = true))
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TEST_ORIGIN_URL))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCVVDialog(token, settings,false)
    }
}
