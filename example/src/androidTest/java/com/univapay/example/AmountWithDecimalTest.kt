package com.univapay.example

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayCheckout
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.models.AppCredentials
import com.univapay.payments.ChargeSettings
import com.univapay.payments.OneTimeTokenCheckout
import com.univapay.sdk.models.common.MoneyLike
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger
import java.net.URI

@RunWith(AndroidJUnit4::class)
class AmountWithDecimalTest: UnivaPayAndroidTest() {

    @Test
    fun shouldDisplayAmountWithDecimal() {
        val money = MoneyLike(BigInteger.valueOf(1234), "USD")
        val settings = ChargeSettings(money)
        checkout = UnivapayCheckout.Builder(
            AppCredentials(appJWT.jwt),
            OneTimeTokenCheckout(settings, processFullPayment = true)
        )
            .setTitle(TestConstants.TITLE)
            .setDescription(TestConstants.DESCRIPTION)
            .setCvvRequired(true)
            .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .build()

        showAndWait(checkout)
        onView(allOf(withId(R.id.text_view_checkout_other_data), isDisplayed()))
                .check(matches(withText(settings.getDescription(resources))))
        onView(allOf(withId(R.id.text_view_checkout_other_data), isDisplayed()))
                .check(matches(withText(containsString("$12.34"))))
    }
}
