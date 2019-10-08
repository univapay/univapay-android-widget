package com.univapay.example

import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.performPayment
import com.univapay.example.utils.FragmentAssertions.performRecurringPayment
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.TEST_EMAIL
import com.univapay.example.utils.TestConstants.Companion.TEST_EXPIRY
import com.univapay.example.utils.TestConstants.Companion.TEST_USER
import com.univapay.example.utils.TestUtils.failureTestCallback
import com.univapay.example.utils.TestUtils.univapayTest
import com.univapay.example.utils.TestUtils.testCallback
import com.univapay.models.AppCredentials
import com.univapay.payments.*
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.TransactionTokenType
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI

@RunWith(AndroidJUnit4::class)
class CallbackInTokenOnlyTest: UnivaPayAndroidTest() {

    @Test
    fun shouldTriggerCallbackWhenCreateOnetimeTokenSuccessfully() = univapayTest(1){ latch->
        val paymentSettings = ChargeSettings(MONEY)

        val checkoutConfiguration = OneTimeTokenCheckout(paymentSettings)

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setTokenCallback(
                        testCallback(latch) { token: TransactionTokenWithData? ->
                            ViewMatchers.assertThat(token!!.type, Matchers.`is`(TransactionTokenType.ONE_TIME))
                            ViewMatchers.assertThat(token.email, Matchers.`is`(TestConstants.TEST_EMAIL))
                            ViewMatchers.assertThat(token.data.card.cardholder, Matchers.`is`(TestConstants.TEST_USER))
                            ViewMatchers.assertThat(token.data.card.expMonth, Matchers.`is`(Integer.valueOf(TestConstants.TEST_EXPIRY.substring(1, 2))))
                            ViewMatchers.assertThat(token.data.card.expYear, Matchers.`is`(Integer.valueOf(TestConstants.TEST_EXPIRY.substring(2, 6))))
                        },
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TestConstants.TITLE, TestConstants.DESCRIPTION, paymentSettings, true, false)
        performPayment()
    }

    @Test
    fun shouldTriggerCallbackWhenCreateRecurringTokenSuccessfully()  = univapayTest(1){ latch->
        val paymentSettings = ChargeSettings(MONEY)
        val checkoutConfiguration = RecurringTokenCheckout(paymentSettings)

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setTokenCallback(
                        testCallback(latch) { token: TransactionTokenWithData? ->
                            ViewMatchers.assertThat(token!!.type, Matchers.`is`(TransactionTokenType.RECURRING))
                            ViewMatchers.assertThat(token.email, Matchers.`is`(TestConstants.TEST_EMAIL))
                            ViewMatchers.assertThat(token.data.card.cardholder, Matchers.`is`(TestConstants.TEST_USER))
                            ViewMatchers.assertThat(token.data.card.expMonth, Matchers.`is`(Integer.valueOf(TestConstants.TEST_EXPIRY.substring(1, 2))))
                            ViewMatchers.assertThat(token.data.card.expYear, Matchers.`is`(Integer.valueOf(TestConstants.TEST_EXPIRY.substring(2, 6))))
                        },
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TestConstants.TITLE, TestConstants.DESCRIPTION, paymentSettings, true, false)
        performRecurringPayment()
    }

    @Test
    fun shouldTriggerCallbackWhenSubscriptionTokenCreatedSuccessfully()  = univapayTest(1){ latch->
        val paymentSettings = SubscriptionSettings(MONEY, SubscriptionPeriod.Monthly)

        val checkoutConfiguration = SubscriptionCheckout(paymentSettings)

        val builder = UnivapayCheckout.Builder(AppCredentials(appJWT.jwt), checkoutConfiguration)
        checkout = builder
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setTokenCallback(
                        testCallback(latch) { token: TransactionTokenWithData? ->
                            ViewMatchers.assertThat(token!!.type, Matchers.`is`(TransactionTokenType.SUBSCRIPTION))
                            ViewMatchers.assertThat(token.email, Matchers.`is`(TEST_EMAIL))
                            ViewMatchers.assertThat(token.data.card.cardholder, Matchers.`is`(TEST_USER))
                            ViewMatchers.assertThat(token.data.card.expMonth, Matchers.`is`(Integer.valueOf(TEST_EXPIRY.substring(1, 2))))
                            ViewMatchers.assertThat(token.data.card.expYear, Matchers.`is`(Integer.valueOf(TEST_EXPIRY.substring(2, 6))))
                        },
                        failureTestCallback(latch)
                ).build()

        showAndWait(checkout)

        assertCardDetailFragment(TestConstants.TITLE, TestConstants.DESCRIPTION, paymentSettings, true, false)
        performPayment()
    }

}
