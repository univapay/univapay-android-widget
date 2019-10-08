package com.univapay.example

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.Espresso.pressBack
import android.support.test.espresso.NoActivityResumedException
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withSubstring
import android.support.test.runner.AndroidJUnit4
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertAddressFragment
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.assertCheckoutLayout
import com.univapay.example.utils.FragmentAssertions.assertKonbiniFragment
import com.univapay.example.utils.FragmentAssertions.assertPaymentTypesFragment
import com.univapay.example.utils.FragmentAssertions.assertServiceErrorView
import com.univapay.example.utils.FragmentAssertions.assertStoredCardView
import com.univapay.example.utils.FragmentAssertions.assertStoredDataEntriesListFragment
import com.univapay.example.utils.FragmentAssertions.assertStoredKonbiniView
import com.univapay.example.utils.FragmentAssertions.assertSubscriptionWithInstallmentDetails
import com.univapay.example.utils.FragmentAssertions.clickActionButton
import com.univapay.example.utils.FragmentAssertions.fillAddressDetails
import com.univapay.example.utils.FragmentAssertions.fillCardDetails
import com.univapay.example.utils.FragmentAssertions.goToCardPayment
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils
import com.univapay.example.utils.TestUtils.createTxToken
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.models.AppCredentials
import com.univapay.models.SupportedPaymentMethod
import com.univapay.payments.ChargeSettings
import com.univapay.payments.OneTimeTokenCheckout
import com.univapay.payments.SubscriptionCheckout
import com.univapay.payments.SubscriptionSettings
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import com.univapay.sdk.types.Konbini
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.math.BigInteger
import java.net.URI
import java.util.*


@RunWith(AndroidJUnit4::class)
class ScreenTransitionTest: UnivaPayAndroidTest() {

    @Test
    fun shouldDisplayFirstPaymentTypesFragmentIfMoreThanOnePaymentTypeAllowed(){
        allowKonbiniPayments()

        val customerId = UUID.randomUUID()

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .rememberCardsForUser(customerId)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setAddress(true)
                .build()

        showAndWait(checkout)

        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldDisplayErrorIfNoPaymentTypesAvailable(){
        disableCardPayments()

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)
        assertServiceErrorView()

    }

    @Test
    fun shouldDisplayKonbiniDetailsIfOnlyKonbiniPaymentsAllowedNotStoringData(){
        allowKonbiniPayments()
        disableCardPayments()

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)

        assertKonbiniFragment(TITLE, DESCRIPTION, settings,false)
    }

    @Ignore("fixme")
    @Test
    fun shouldDisplayStoredKonbinisIfOnlyKonbiniPaymentsAllowedStoringData(){
        allowKonbiniPayments()
        disableCardPayments()

        val customerId = UUID.randomUUID()

        val token = createTxToken(univapay, appJWT, TestUtils.createKonbiniData(Konbini.FAMILY_MART), customerId)

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .rememberCardsForUser(customerId)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setAddress(true)
                .build()

        showAndWait(checkout)

        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        assertStoredKonbiniView(token)
    }

    @Test
    fun shouldDisplayCardDetailsIfOnlyCardPaymentsAllowedNotStoringDataNoAddress(){

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings, true, false)
    }

    @Test
    fun shouldDisplayAddressDetailsIfOnlyCardPaymentsAllowedNotStoringDataRequireAddress(){
        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setAddress(true)
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)

        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }

    @Ignore("fixme")
    @Test
    fun shouldDisplayStoredCardsIfOnlyCardPaymentsAllowedStoringData(){
        val customerId = UUID.randomUUID()

        val token = createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .rememberCardsForUser(customerId)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setAddress(true)
                .build()

        showAndWait(checkout)

        assertCheckoutLayout(TITLE, DESCRIPTION, settings)
        assertStoredCardView(token)
    }

    @Test
    fun shouldDisplayFirstSubscriptionDetailsIfSubscriptionWithInstallments(){

        val settings = SubscriptionSettings(MoneyLike(BigInteger.valueOf(50000), "JPY"), SubscriptionPeriod.Monthly)
                .withInitialAmount(BigInteger.valueOf(5000))
                .withInstallmentPlan(FixedCycleInstallmentsPlan(3))

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                SubscriptionCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .setCvvRequired(true)
                .setAddress(true)
                .build()

        showAndWait(checkout)

        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldForceCardPaymentEvenIfOtherPaymentMethodsAvailable(){
        allowKonbiniPayments()

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .forcePaymentMethod(SupportedPaymentMethod.CREDIT_CARD)
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)

        assertCardDetailFragment(TITLE, DESCRIPTION, settings,true, false)
    }

    @Test
    fun shouldForceKonbiniPaymentEvenIfOtherPaymentMethodsAvailable(){
        allowKonbiniPayments()

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .forcePaymentMethod(SupportedPaymentMethod.CONVENIENCE_STORE)
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)

        assertKonbiniFragment(TITLE, DESCRIPTION, settings)
    }

    @Test
    fun shouldDisplayServiceErrorIfForcedPaymentMethodNotEnabledForStore(){
        allowKonbiniPayments()
        disableCardPayments()

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(
                        appJWT.jwt
                ),
                OneTimeTokenCheckout(settings)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
                .forcePaymentMethod(SupportedPaymentMethod.CREDIT_CARD)
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)

        assertServiceErrorView()
    }


//  Test that the back button takes the customer to the expected fragment
    @Ignore("fixme")
    @Test()
    fun shouldTransitionAllScreens() {
        allowKonbiniPayments()

        val customerId = UUID.randomUUID()
        val token = createTxToken(univapay, appJWT, TestUtils.createCreditCard(), customerId)

        val settings = SubscriptionSettings(MoneyLike(BigInteger.valueOf(50000), "JPY"), SubscriptionPeriod.Monthly)
                .withInitialAmount(BigInteger.valueOf(5000))
                .withInstallmentPlan(FixedCycleInstallmentsPlan(3))

        checkout = UnivapayCheckout.Builder(
            AppCredentials(
                appJWT.jwt
            ),
            SubscriptionCheckout(settings)
        )
            .setTitle(TestConstants.TITLE)
            .setDescription(TestConstants.DESCRIPTION)
            .rememberCardsForUser(customerId)
            .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .setCvvRequired(true)
            .setAddress(true)
            .build()

        showAndWait(checkout)

        //payment type
        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)
        goToCardPayment()

        //subscription details
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
        clickActionButton()

        //stored data
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        assertStoredCardView(token)
        clickActionButton()

        //address details
        assertAddressFragment(TITLE, DESCRIPTION, settings)
        fillAddressDetails(true)
        clickActionButton()

        //card details
        assertCardDetailFragment(TITLE, DESCRIPTION, settings,true, true)
        fillCardDetails()
        pressBack()

        //go back to address details
        assertAddressFragment(TITLE, DESCRIPTION, settings)
        pressBack()

        //go back to stored cards
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        assertStoredCardView(token)
        onView(withId(R.id.stored_card_number)).check(matches(withSubstring("XXXX XXXX XXXX ")))
        pressBack()

        //go back to subscription details
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
        pressBack()

        //go back to payment type
        assertPaymentTypesFragment(TITLE, DESCRIPTION, settings)

        //should throw NoActivityResumedException because the process is killed
        try {
            pressBack()
        } catch (e: NoActivityResumedException){

        }
    }
}
