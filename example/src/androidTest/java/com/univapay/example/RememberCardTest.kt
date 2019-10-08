package com.univapay.example

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.assertion.ViewAssertions.doesNotExist
import android.support.test.espresso.matcher.ViewMatchers.assertThat
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import com.univapay.UnivapayCheckout
import com.univapay.adapters.SubscriptionPeriod
import com.univapay.example.utils.FragmentAssertions.assertAddressFragment
import com.univapay.example.utils.FragmentAssertions.assertBottomSheetView
import com.univapay.example.utils.FragmentAssertions.assertCardDetailFragment
import com.univapay.example.utils.FragmentAssertions.assertQRImageView
import com.univapay.example.utils.FragmentAssertions.assertStoredCardView
import com.univapay.example.utils.FragmentAssertions.assertStoredDataEntriesListFragment
import com.univapay.example.utils.FragmentAssertions.assertSubscriptionWithInstallmentDetails
import com.univapay.example.utils.FragmentAssertions.clickActionButton
import com.univapay.example.utils.FragmentAssertions.performPayment
import com.univapay.example.utils.FragmentAssertions.performStoredCardPayment
import com.univapay.example.utils.TestConstants
import com.univapay.example.utils.TestConstants.Companion.DESCRIPTION
import com.univapay.example.utils.TestConstants.Companion.MONEY
import com.univapay.example.utils.TestConstants.Companion.SUBSCRIPTION_MONEY
import com.univapay.example.utils.TestConstants.Companion.TEST_ENDPOINT
import com.univapay.example.utils.TestConstants.Companion.TITLE
import com.univapay.example.utils.TestUtils.createCreditCard
import com.univapay.example.utils.TestUtils.createTxToken
import com.univapay.example.utils.TestUtils.failureTestCallback
import com.univapay.example.utils.TestUtils.testCallback
import com.univapay.example.utils.TestUtils.univapayTest
import com.univapay.example.utils.UnivaPayAndroidTest
import com.univapay.models.AppCredentials
import com.univapay.models.UnivapayError
import com.univapay.payments.ChargeSettings
import com.univapay.payments.OneTimeTokenCheckout
import com.univapay.payments.SubscriptionCheckout
import com.univapay.payments.SubscriptionSettings
import com.univapay.sdk.models.request.subscription.FixedCycleInstallmentsPlan
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.types.ChargeStatus
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.sdk.types.SubscriptionStatus
import com.univapay.sdk.types.TransactionTokenType
import org.hamcrest.Matchers.`is`
import org.joda.time.LocalDate
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.ZoneId
import java.math.BigInteger
import java.net.URI
import java.util.*

@RunWith(AndroidJUnit4::class)
class RememberCardTest: UnivaPayAndroidTest(){

    @Test
    fun shouldNotDisplayListOfCardsIfNoneIsStored(){

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
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

    @Ignore("fixme")
    @Test
    fun shouldDisplayListOfCardsIfCustomerIDIsPassed(){
        val customerId = UUID.randomUUID()
        val creditCard = createCreditCard()

        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
        )
                .rememberCardsForUser(customerId)
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)
    }

    @Ignore("fixme")
    @Test
    fun newCardButtonShouldTakeUserToCardDetailsFragment(){
        val customerId = UUID.randomUUID()

        val creditCard = createCreditCard()
        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)
        clickActionButton()
        assertCardDetailFragment(TITLE, DESCRIPTION, settings,true, true)
    }

    @Test
    fun shouldPerformPaymentRememberingCardDetails(){
        val customerId = UUID.randomUUID()

        val settings = ChargeSettings(MONEY)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
        )
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .build()

        showAndWait(checkout)
        assertCardDetailFragment(TITLE, DESCRIPTION, settings, true, true)
        performPayment(shouldRememberCard = true)
    }

    @Ignore("Be enable after API is fixed")
    @Test
    fun shouldCreateSubscriptionRememberingCardDetails() = univapayTest(2){ latch->
        val settings = SubscriptionSettings(SUBSCRIPTION_MONEY, SubscriptionPeriod.Monthly)
            .withInitialAmount(BigInteger.valueOf(5000))
            .withInstallmentPlan(FixedCycleInstallmentsPlan(3))
            .withStartOn(
                    LocalDate.now().plusDays(2)
            )
            .withZoneId(ZoneId.of("America/Cancun"))
            .withPreserveEndOfMonth(true)

        checkout = UnivapayCheckout.Builder(
            AppCredentials(appJWT.jwt),
            SubscriptionCheckout(settings, processFullPayment = true)
        )
            .setTitle(TestConstants.TITLE)
            .setDescription(TestConstants.DESCRIPTION)
            .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .rememberCardsForUser(UUID.randomUUID())
            .setTokenCallback(
                testCallback(latch){ token ->
                    assertThat(token!!.type, `is`(TransactionTokenType.RECURRING))
                    assertThat(token.paymentTypeName, `is`(PaymentTypeName.CARD))
                },
                failureTestCallback(latch)
            )
            .setChargeCallback(
                testCallback(latch){ _: Charge? ->
                    fail("Unexpected charge creation")
                },
                testCallback(latch){ _: UnivapayError? ->
                    fail("Unexpected charge creation")
                },
                testCallback(latch){ _: Charge? ->
                    fail("Unexpected charge creation")
                }
            )
            .setSubscriptionCallback(
                testCallback(latch){ subscription: FullSubscription? ->
                    assertThat(subscription!!.status, `is`(SubscriptionStatus.CURRENT))
                    assertThat(subscription.installmentPlan.planType, `is`(settings.installmentPlan!!.planType))
                    assertThat(subscription.scheduleSettings.startOn, `is`(settings.startOn))
                    assertThat(subscription.scheduleSettings.zoneId, `is`(settings.zoneId))
                },
                testCallback(latch){ error: UnivapayError? ->
                    fail(error!!.message)
                },
                testCallback(latch){ subscription: FullSubscription? ->
                    fail("Bad subscription status: ${subscription!!.status}")
                })
            .build()

        showAndWait(checkout)
        Thread.sleep(1000)
        assertSubscriptionWithInstallmentDetails(TITLE, DESCRIPTION, settings)
        clickActionButton()
        assertCardDetailFragment(TITLE, DESCRIPTION, settings,true, true)
        performPayment(shouldRememberCard = true)
    }

    @Ignore("fixme")
    @Test
    fun newCardButtonShouldTakeUserToBillingAddressFragment(){
        val customerId = UUID.randomUUID()

        val settings = ChargeSettings(MONEY)

        val creditCard = createCreditCard()
        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
        )
                .setAddress(true)
                .rememberCardsForUser(customerId)
                .setTitle(TestConstants.TITLE)
                .setDescription(TestConstants.DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)
        clickActionButton()
        assertAddressFragment(TITLE, DESCRIPTION, settings)
    }

    @Ignore("fixme")
    @Test
    fun canMakeAPaymentUsingAStoredRecurringToken() = univapayTest(1) { latch->
        val customerId = UUID.randomUUID()

        val settings = ChargeSettings(MONEY)

        val creditCard = createCreditCard()
        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
        )
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setCvvRequired(true)
                .setTokenCallback(
                    testCallback(latch){_ ->
                        fail("Should not create a new token")
                    },
                    testCallback(latch){ _ ->
                        fail("Should not create a new token")
                    }
                )
                .setChargeCallback(
                        testCallback(latch){charge: Charge? ->
                            assertThat(charge!!.status, `is`(ChargeStatus.SUCCESSFUL))

                        },
                        testCallback(latch){ error: UnivapayError? ->
                            fail(error!!.message)

                        },
                        testCallback(latch){charge: Charge? ->
                            fail("Bad charge status: ${charge!!.status}")
                        }
                )
                .setSubscriptionCallback(
                        testCallback(latch){_: FullSubscription? ->
                            fail("Should not create a subscription")
                        },
                        testCallback(latch){ error: UnivapayError? ->
                            fail(error!!.message)

                        },
                        testCallback(latch){_: FullSubscription? ->
                            fail("Should not create a subscription")
                        }
                )
                .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
         onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)
        onView(withId(R.id.stored_card_number)).perform(ViewActions.click())
        performStoredCardPayment()
    }

    @Ignore("fixme")
    @Test
    fun canMakeASubscriptionPaymentsUsingAStoredRecurringToken() = univapayTest(1){ latch->
        val customerId = UUID.randomUUID()

        val settings = SubscriptionSettings(MONEY, SubscriptionPeriod.Monthly)

        val creditCard = createCreditCard()
        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        checkout = UnivapayCheckout.Builder(
            AppCredentials(appJWT.jwt
            ),
            SubscriptionCheckout(settings, processFullPayment = true)
        )
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
            .setEndpoint(URI(TestConstants.TEST_ENDPOINT))
            .rememberCardsForUser(customerId)
            .setCvvRequired(true)
            .setTokenCallback(
                testCallback(latch){
                    fail("Should not create a new token")
                },
                testCallback(latch){
                    fail("Should not create a new token")
                }
            )
            .setChargeCallback(
                testCallback(latch){
                    fail("Should not create a charge")
                },
                testCallback(latch){
                    fail("Should not create a charge")
                },
                testCallback(latch){
                    fail("Should not create a charge")
                }
            )
            .setSubscriptionCallback(
                testCallback(latch){ subscription: FullSubscription? ->
                    assertThat(subscription!!.status, `is`(SubscriptionStatus.CURRENT))
                },
                testCallback(latch){ error: UnivapayError? ->
                    fail(error!!.message)
                },
                testCallback(latch){ subscription: FullSubscription? ->
                    fail("Bad subscription status: ${subscription!!.status}")
                })
            .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)

        onView(withId(R.id.stored_card_number)).perform(ViewActions.click())
        performStoredCardPayment()
    }

    @Ignore("fixme")
    @Test
    fun shouldDisplayQRCodeImage(){
        val customerId = UUID.randomUUID()

        val settings = ChargeSettings(MONEY)

        val creditCard = createCreditCard()
        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
        )
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)

        onView(withId(R.id.stored_card_number)).perform(ViewActions.click())
        assertBottomSheetView()
        onView(withId(R.id.stored_data_bottom_qr)).perform(ViewActions.click())
        Thread.sleep(500)
        assertQRImageView()
    }

    @Ignore("fixme")
    @Test
    fun canDeleteAnEntry(){
        val customerId = UUID.randomUUID()

        val settings = ChargeSettings(MONEY)

        val creditCard = createCreditCard()
        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        checkout = UnivapayCheckout.Builder(
                AppCredentials(appJWT.jwt
                ),
                OneTimeTokenCheckout(settings, processFullPayment = true)
        )
                .setTitle(TITLE)
                .setDescription(DESCRIPTION)
                .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
                .setEndpoint(URI(TEST_ENDPOINT))
                .rememberCardsForUser(customerId)
                .setCvvRequired(true)
                .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)

        onView(withId(R.id.stored_card_number)).perform(ViewActions.click())
        assertBottomSheetView()
        onView(withId(R.id.stored_data_bottom_delete)).perform(ViewActions.click())
        Thread.sleep(1000)
        onView(withId(R.id.stored_card_number)).check(doesNotExist())
    }

    @Ignore("fixme")
    @Test
    fun shouldDisplayQRDirectly() {
        val customerId = UUID.randomUUID()

        val settings = ChargeSettings(MONEY)

        val creditCard = createCreditCard()
        val token = createTxToken(univapay, appJWT, creditCard, customerId)

        checkout = UnivapayCheckout.Builder(
            AppCredentials(appJWT.jwt
            ),
            OneTimeTokenCheckout(settings, processFullPayment = true)
        )
            .setTitle(TITLE)
            .setDescription(DESCRIPTION)
            .setOrigin(URI(TestConstants.TEST_ORIGIN_URL))
            .setEndpoint(URI(TEST_ENDPOINT))
            .rememberCardsForUser(customerId, true)
            .setCvvRequired(true)
            .build()

        showAndWait(checkout)
        assertStoredDataEntriesListFragment(TITLE, DESCRIPTION, settings)
        onView(withId(R.id.stored_data_recycler_view)).check{ view, _ ->
            assertThat((view as RecyclerView).adapter.itemCount, `is`(1))
        }
        assertStoredCardView(token)

        onView(withId(R.id.stored_card_number)).perform(ViewActions.click())
        Thread.sleep(1000)
        assertQRImageView()
    }
}
