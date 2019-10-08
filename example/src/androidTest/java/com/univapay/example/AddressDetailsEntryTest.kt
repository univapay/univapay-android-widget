package com.univapay.example

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.*
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.univapay.example.utils.FragmentAssertions.assertAddressFragment
import com.univapay.example.utils.FragmentAssertions.fillAddressDetails
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_CITY
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_COUNTRY
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_LINE1
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_LINE2
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_NAME
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_POST_CODE
import com.univapay.example.utils.TestConstants.Companion.ADDRESS_STATE
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.TEST_ENDPOINT
import com.univapay.example.utils.TestConstants.Companion.TEST_ORIGIN_URL
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.UnivapayCheckout
import com.univapay.models.AppCredentials
import com.univapay.payments.ChargeSettings
import com.univapay.payments.OneTimeTokenCheckout
import org.hamcrest.Matchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import java.net.URI

@LargeTest
@RunWith(AndroidJUnit4::class)
class AddressDetailsEntryTest: UnivaPayAndroidTest() {

    @Test
    fun addressDetailsEntryTest() {

        val settings = ChargeSettings(TestConstants.MONEY)
        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt),
                OneTimeTokenCheckout(settings)
        ).setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setAddress(true)
                .setOrigin(URI(TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .build()

        showAndWait(checkout)

        assertAddressFragment(TITLE, DESCRIPTION, settings)
        fillAddressDetails(true)

        // Country
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).perform(ViewActions.scrollTo(), click())
        // Search for
        onView(ViewMatchers.withId(com.univapay.R.id.country_code_picker_text_view_search))
                .perform(
                        replaceText(ADDRESS_COUNTRY),
                        closeSoftKeyboard()
                )

        onView(allOf(
                withText(ADDRESS_COUNTRY),
                withId(R.id.row_title)
        ))
                .check(matches(isDisplayed()))
                .perform(click())

        // Check Data on the Form
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_name)).check(matches(withText(ADDRESS_NAME)))
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_line1)).check(matches(withText(ADDRESS_LINE1)))
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_line2)).check(matches(withText(ADDRESS_LINE2)))
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_city)).check(matches(withText(ADDRESS_CITY)))
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_state)).check(matches(withText(ADDRESS_STATE)))
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_country)).check(matches(withText(ADDRESS_COUNTRY)))
        onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_post_code)).check(matches(withText(ADDRESS_POST_CODE)))
    }
}
