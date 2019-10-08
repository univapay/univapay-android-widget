package com.univapay.example

/**
 * Tests for the `setCVVRequired` feature in the card details fragment.
 * The assumption is that the store allows empty CVV, so whether it is required or not depends on
 * the merchant's settings in the Android Widget.
 */
//@RunWith(AndroidJUnit4::class)
class RequireCvvAllowEmptyTest {
    // The merchant allows empty cvv
//    private val univapay: UnivapaySDK = getUnivapay()
//
//    private val store: StoreWithConfiguration by TestUtils.getStore(univapay)
//
//    private val appJWT: StoreApplicationJWT by TestUtils.getAppToken(univapay, store)
//
//    private val credentials: AppCredentials by lazy {
//        AppCredentials(appJWT.jwt, appJWT.secret)
//    }
//
//    private var checkout: UnivapayCheckout? = null
//
//    @Before
//    fun setup() {
//        TestUtils.setup(univapay, store)
//        checkout = null
//    }
//
//    @After
//    fun teardown() {
//        TestUtils.teardown(univapay, store.id, checkout)
//    }
//
//    /*
//      Factors
//    ・cvv threshold -> always lower for this class
//    ・6 token types
//    ・builder.setCvvRequired
//     */
//
//    @Test
//    fun shouldNotDisplayCvvIfNotRequiredOnOneTimeTokenCheckout() {
//        checkout = createCheckout(OneTimeTokenCheckout(), false, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(not(isDisplayed())))
//    }
//
//    @Test
//    fun shouldDisplayCvvIfRequiredOnOneTimeTokenCheckout() {
//        checkout = createCheckout(OneTimeTokenCheckout(), true, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun shouldNotDisplayCvvIfNotRequiredOnOneTimeTokenPaymentCheckout() {
//        checkout = createCheckout(OneTimeTokenCheckout().asPayment(), false, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(not(isDisplayed())))
//    }
//
//    @Test
//    fun shouldDisplayCvvIfRequiredOnOneTimeTokenPaymentCheckout() {
//        checkout = createCheckout(OneTimeTokenCheckout().asPayment(), true, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun shouldNotDisplayCvvIfNotRequiredOnRecurringTokenCheckout() {
//        checkout = createCheckout(RecurringTokenCheckout(), false, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(not(isDisplayed())))
//    }
//
//    @Test
//    fun shouldDisplayCvvIfRequiredOnRecurringTokenCheckout() {
//        checkout = createCheckout(RecurringTokenCheckout(), true, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun shouldNotDisplayCvvIfNotRequiredOnRecurringTokenPaymentCheckout() {
//        checkout = createCheckout(RecurringTokenCheckout().asPayment(), false, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(not(isDisplayed())))
//    }
//
//    @Test
//    fun shouldDisplayCvvIfRequiredOnRecurringTokenPaymentCheckout() {
//        checkout = createCheckout(RecurringTokenCheckout().asPayment(), true, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun shouldNotDisplayCvvIfNotRequiredOnSubscriptionTokenCheckout() {
//        checkout = createCheckout(SubscriptionCheckout(), false, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(not(isDisplayed())))
//    }
//
//    @Test
//    fun shouldDisplayCvvIfRequiredOnSubscriptionTokenCheckout() {
//        checkout = createCheckout(SubscriptionCheckout(), true, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(isDisplayed()))
//    }
//
//    @Test
//    fun shouldNotDisplayCvvIfNotRequiredOnSubscriptionPaymentCheckout() {
//        checkout = createCheckout(
//                SubscriptionCheckout()
//                        .asPayment(SubscriptionSettings(SubscriptionPeriod.Daily)),
//                false, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(not(isDisplayed())))
//    }
//
//    @Test
//    fun shouldDisplayCvvIfRequiredOnSubscriptionPaymentCheckout() {
//        checkout = createCheckout(
//                SubscriptionCheckout()
//                        .asPayment(SubscriptionSettings(SubscriptionPeriod.Daily)),
//                true, credentials)
//        checkout?.show()
//        onView(withId(R.id.card_detail_edit_text_security)).check(matches(isDisplayed()))
//    }
}
