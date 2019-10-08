package com.univapay.example

import android.support.test.espresso.Espresso.onData
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.fillAddressDetails
import com.univapay.example.utils.FragmentAssertions.performPayment
import com.univapay.example.utils.FragmentAssertions.performRecurringPayment
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_CITY
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_COUNTRY
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.METADATA
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.TEST_USER
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils
import com.univapay.example.utils.TestUtils.failureTestCallback
import com.univapay.example.utils.TestUtils.univapayTest
import com.univapay.models.AppCredentials
import com.univapay.payments.*
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.RecurringTokenInterval
import com.univapay.sdk.types.TransactionTokenType
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.anything
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI

@RunWith(AndroidJUnit4::class)
class CheckoutTokenOnlyTest: UnivaPayAndroidTest() {

    @Test
    fun displayWithOneTimeToken() {
        val settings = ChargeSettings(MONEY)
        val checkoutConfiguration = OneTimeTokenCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
            .build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

    }

    @Test
    fun displayWithRecurringTokenNoAddress() {
        val settings = ChargeSettings(MONEY)
        val checkoutConfiguration = RecurringTokenCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

    }

    @Test
    fun displayWithSubscription() {
        val settings = SubscriptionSettings(MONEY, SubscriptionPeriod.Daily)
        val checkoutConfiguration = SubscriptionCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)
    }

    @Test
    fun displayWithOneTimeTokenRequiredAddress() {
        val settings = ChargeSettings(MONEY)
        val checkoutConfiguration = OneTimeTokenCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setAddress(true).setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        // Input Data
        fillAddressDetails(true)
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).perform(ViewActions.scrollTo(), click())
        onView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_text_view_search)).perform(replaceText(ADDRESS_COUNTRY), closeSoftKeyboard())
        // Select Japan.
        onData(anything()).inAdapterView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_list_view)).atPosition(110).perform(click())

        onView(withId(com.univapay.R.id.action_button)).perform(click())

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

    }

    @Test
    fun displayWithRecurringTokenRequiredAddress() {
        val settings = ChargeSettings(MONEY)
        val checkoutConfiguration = RecurringTokenCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setAddress(true)
                .setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setRecurringTokenUsageLimit(RecurringTokenInterval.DAILY)
            .build()

        showAndWait(checkout)
        // Input Data
        fillAddressDetails(true)
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).perform(ViewActions.scrollTo(), click())
        onView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_text_view_search)).perform(replaceText(ADDRESS_COUNTRY), closeSoftKeyboard())
        // Select Japan.
        onData(anything()).inAdapterView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_list_view)).atPosition(110).perform(click())

        onView(withId(com.univapay.R.id.action_button)).perform(click())

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

    }

    @Test
    fun displayWithSubscriptionRequiredAddress() {
        val settings = SubscriptionSettings(MONEY, SubscriptionPeriod.Daily)
        val checkoutConfiguration = SubscriptionCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setAddress(true)
                .setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        // Input Data
        fillAddressDetails(true)
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).perform(ViewActions.scrollTo(), click())
        onView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_text_view_search)).perform(replaceText(ADDRESS_COUNTRY), closeSoftKeyboard())
        // Select Japan.
        onData(anything()).inAdapterView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_list_view)).atPosition(110).perform(click())

        onView(withId(com.univapay.R.id.action_button)).perform(click())

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

    }

    @Test
    fun createWithOneTimeToken() = univapayTest(1) { latch->

        val settings = ChargeSettings(MONEY)
        val checkoutConfiguration = OneTimeTokenCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setAddress(true).setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                    TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                        assertThat(transactionTokenWithData!!.type, `is`(TransactionTokenType.ONE_TIME))
                    },
                    failureTestCallback(latch)
        ).build()

        showAndWait(checkout)
        // Input Data
        fillAddressDetails(true)
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).perform(ViewActions.scrollTo(), click())
        onView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_text_view_search)).perform(replaceText(ADDRESS_COUNTRY), closeSoftKeyboard())
        // Select Japan.
        onData(anything()).inAdapterView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_list_view)).atPosition(110).perform(click())

        onView(withId(com.univapay.R.id.action_button)).perform(click())

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

        performPayment()
    }

    @Test
    fun createWithRecurringToken() = univapayTest(1) { latch->
        val settings = ChargeSettings(MONEY)
        val checkoutConfiguration = RecurringTokenCheckout(settings)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt), checkoutConfiguration
        )
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setAddress(true)
                .setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setRecurringTokenUsageLimit(RecurringTokenInterval.DAILY)
            .setTokenCallback (
                    TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                        assertThat(transactionTokenWithData!!.type, `is`(TransactionTokenType.RECURRING))
                        assertThat(transactionTokenWithData.usageLimit, `is`(RecurringTokenInterval.DAILY))
                    },
                    failureTestCallback(latch)
            ).build()

        showAndWait(checkout)
        // Input Data
        fillAddressDetails(true)
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).perform(ViewActions.scrollTo(), click())
        onView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_text_view_search)).perform(replaceText(ADDRESS_COUNTRY), closeSoftKeyboard())
        // Select Japan.
        onData(anything()).inAdapterView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_list_view)).atPosition(110).perform(click())

        onView(withId(com.univapay.R.id.action_button)).perform(click())

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

        performRecurringPayment()

    }

    @Test
    fun createWithSubscription() = univapayTest(1) { latch->

        val settings = SubscriptionSettings(MONEY, SubscriptionPeriod.Daily)
        val checkoutConfiguration = SubscriptionCheckout(settings)

        checkout = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setAddress(true)
                .setMetadata(METADATA)
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback (
                    TestUtils.testCallback(latch) { transactionTokenWithData: TransactionTokenWithData? ->
                        assertThat(transactionTokenWithData!!.type, `is`(TransactionTokenType.SUBSCRIPTION))
                        assertThat(transactionTokenWithData.data.billing.city, `is`(ADDRESS_CITY))
                        assertThat(transactionTokenWithData.data.card.cardholder, `is`(TEST_USER))
                    },
                    failureTestCallback(latch)
            ).build()

        showAndWait(checkout)

        // Input Data
        fillAddressDetails(true)
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).perform(ViewActions.scrollTo(), click())
        onView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_text_view_search)).perform(replaceText(ADDRESS_COUNTRY), closeSoftKeyboard())
        // Select Japan.
        onData(anything()).inAdapterView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_list_view)).atPosition(110).perform(click())

        onView(withId(com.univapay.R.id.action_button)).perform(click())

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, cvvRequired = true, rememberCard = false)

        performPayment()
    }
}
