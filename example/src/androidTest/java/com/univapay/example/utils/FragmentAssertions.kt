package com.univapay.example.utils

import android.support.annotation.IdRes
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions
import android.support.test.espresso.matcher.ViewMatchers
import android.view.View
import com.univapay.adapters.KonbiniAdapter
import com.univapay.example.R
import com.univapay.payments.PaymentSettings
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import org.hamcrest.CoreMatchers
import org.hamcrest.Matchers

object FragmentAssertions {

    val resources = InstrumentationRegistry.getTargetContext().resources


    /**
     * Assert that all the parts of the main checkout layout are displayed.
     * This includes the logo, title, store description and the payment description.
     */
    fun assertCheckoutLayout(title: String, description: String, settings: PaymentSettings?=null){

        assertAllDisplayed(
                listOf(
                        R.id.layout_app_bar,
                        R.id.image_view_checkout_logo,
                        R.id.text_view_checkout_title,
                        R.id.text_view_checkout_description,
                        R.id.text_view_checkout_other_data
                )
        )

        val asserts = mapOf(
                R.id.text_view_checkout_title to title,
                R.id.text_view_checkout_description to description
        )
         settings?.let {
             asserts.plus(
                 R.id.text_view_checkout_other_data to settings.getDescription(resources)
             )
         }
        assertTextContent(asserts)
    }

    /**
     * Assert that the PaymentTypesFragment is being displayed properly.
     */
    fun assertPaymentTypesFragment(title: String, description: String, settings: PaymentSettings?=null){
        assertCheckoutLayout(title, description, settings)
        assertAllDisplayed(
                listOf(
                        R.id.card_payment_type_text_select,
                        R.id.card_payment_type,
                        R.id.card_payment_type_text,
                        R.id.konbini_payment_type,
                        R.id.konbini_payment_type_text
                )
        )

        assertIsNotDisplayed(R.id.action_button)
    }

    /**
     * Asserts that the CardDetailFragment is displayed properly
     */
    fun assertCardDetailFragment(title: String, description: String, settings: PaymentSettings?=null, cvvRequired: Boolean, rememberCard: Boolean){
        assertCheckoutLayout(title, description, settings)
        assertAllDisplayed(
                listOf(
                        R.id.card_detail_edit_text_email,
                        R.id.card_detail_edit_text_cardholder_name,
                        R.id.card_detail_edit_text_card_number,
                        R.id.card_detail_edit_text_expiry
                )
        )

        if(cvvRequired) Espresso.onView(ViewMatchers.withId(R.id.card_detail_edit_text_security)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        if(rememberCard) Espresso.onView(ViewMatchers.withId(R.id.check_can_remember_card)).perform(scrollTo()).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        assertTextContent(R.id.action_button, R.string.checkout_button_pay)
    }

    /**
     * Asserts that the AddressFragment is displayed properly
     */
    fun assertAddressFragment(title: String, description: String, settings: PaymentSettings?=null){
        assertCheckoutLayout(title, description, settings)
        assertAllDisplayed(
                listOf(
                        R.id.address_edit_text_name,
                        R.id.address_edit_text_line1,
                        R.id.address_edit_text_line2,
                        R.id.address_edit_text_city,
                        R.id.address_edit_text_state,
                        R.id.address_edit_text_country,
                        R.id.address_edit_text_post_code
                )
        )

        assertTextContent(R.id.action_button, R.string.checkout_button_next)
    }

    /**
     * Assert that the Konbini details fragment is displayed properly
     */
    fun assertKonbiniFragment(title: String, description: String, settings: PaymentSettings?=null, shouldRememberKonbini: Boolean = false){
        assertCheckoutLayout(title, description, settings)
        assertAllDisplayed(
                listOf(
                        R.id.konbini_edit_text_name,
                        R.id.konbini_edit_text_email,
                        R.id.konbini_edit_phone_local_number,
                        R.id.konbini_spinner
                )
        )

        if(shouldRememberKonbini) assertIsDisplayed(R.id.check_can_remember_konbini_data)
        else assertIsNotDisplayed(R.id.check_can_remember_konbini_data)
        assertTextContent(R.id.action_button, R.string.checkout_button_pay)
    }


    /**
     * Assert that the StoredDataFragment is being displayed properly.
     */
    fun assertStoredDataEntriesListFragment(title: String, description: String, settings: PaymentSettings?=null){
        assertCheckoutLayout(title, description, settings)
        assertAllDisplayed(
                listOf(
                        R.id.stored_data_recycler_view
                )
        )
        assertTextContent(R.id.action_button, R.string.checkout_button_add)
    }

    /**
     * Asserts that a credit card view is properly displayed for a given transaction token.
     */
    fun assertStoredCardView(token: TransactionTokenWithData){
        assertAllDisplayed(
                listOf(
                        R.id.stored_card_logo,
                        R.id.stored_card_number
                )
        )

        Espresso.onView(ViewMatchers.withId(R.id.stored_card_number)).check(
                ViewAssertions.matches(ViewMatchers.withText("XXXX XXXX XXXX ${token.data.card.lastFour}"))
        )
    }

    /**
     * Asserts that a konbini view is properly displayed for a given transaction token.
     */
    fun assertStoredKonbiniView(token: TransactionTokenWithData){
        assertAllDisplayed(
                listOf(
                        R.id.stored_konbini_name,
                        R.id.stored_konbini_phone,
                        R.id.stored_konbini_image
                )
        )

        val konbiniTokenData = token.data.asKonbiniPaymentData()
        val konbini = KonbiniAdapter.fromEnum(konbiniTokenData.convenienceStore)

        Espresso.onView(ViewMatchers.withId(R.id.stored_konbini_name)).check(
                ViewAssertions.matches(ViewMatchers.withText(konbini.nameResId))
        )

        konbiniTokenData.phoneNumber.apply {
            Espresso.onView(ViewMatchers.withId(R.id.stored_konbini_phone)).check(
                    ViewAssertions.matches(ViewMatchers.withText("+$countryCode $localNumber"))
            )
        }
    }

    /**
     * Asserts that the SubscriptionDetailsFragment is displayed properly for a given payments plan.
     */

    fun assertSubscriptionWithInstallmentDetails(title: String, description: String, settings: PaymentSettings?=null){
        assertCheckoutLayout(title, description, settings)
        assertAllDisplayed(
                listOf(
                        R.id.text_view_subscription_details_title,
                        R.id.divider_subscription,
                        R.id.simulated_payments_messages,
                        R.id.simulated_payments_list
                )
        )
        assertTextContent(R.id.action_button, R.string.checkout_button_next)
    }

    /**
     * Assert that the bottom sheet view is displayed properly for a given token.
     */
    fun assertBottomSheetView(){
        assertAllDisplayed(
                listOf(
                        R.id.stored_data_bottom_title_main,
                        R.id.stored_data_bottom_title_secondary,
                        R.id.stored_data_bottom_pay_icon,
                        R.id.stored_data_bottom_pay_text,
                        R.id.stored_data_bottom_delete_icon,
                        R.id.stored_data_bottom_delete_text,
                        R.id.stored_data_bottom_qr_icon,
                        R.id.stored_data_bottom_qr_text
                )
        )
    }

    fun assertServiceErrorView(){
        assertAllDisplayed(
                listOf(
                        R.id.checkout_service_error_button,
                        R.id.checkout_service_error_description,
                        R.id.checkout_service_error_title
                )
        )
    }

    fun goToKonbiniPayment(){
        Espresso.onView(ViewMatchers.withId(R.id.konbini_payment_type)).perform(scrollTo(), click())
    }

    fun goToCardPayment(){
        Espresso.onView(ViewMatchers.withId(R.id.card_payment_type)).perform(scrollTo(), click())
    }

    fun assertQRImageView(){
        assertIsDisplayed(R.id.stored_data_qr_display)
    }

    fun fillAddressDetails(includeLine2: Boolean){
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_name)).perform(scrollTo(), replaceText(TestConstants.ADDRESS_NAME), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_line1)).perform(scrollTo(), replaceText(TestConstants.ADDRESS_LINE1), closeSoftKeyboard())
        if(includeLine2) Espresso.onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_line2)).perform(scrollTo(), replaceText(TestConstants.ADDRESS_LINE2), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_city)).perform(scrollTo(), replaceText(TestConstants.ADDRESS_CITY), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_state)).perform(scrollTo(), replaceText(TestConstants.ADDRESS_STATE), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.address_edit_text_post_code)).perform(scrollTo(), replaceText(TestConstants.ADDRESS_POST_CODE), closeSoftKeyboard())
    }

    fun fillKonbiniDetails(position: Int, rememberKonbiniDetails: Boolean = false){
        Espresso.onView(ViewMatchers.withId(R.id.konbini_edit_text_name)).perform(scrollTo(), replaceText(TestConstants.TEST_NAME), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.konbini_edit_text_email)).perform(scrollTo(), replaceText(TestConstants.TEST_EMAIL), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.konbini_edit_phone_local_number)).perform(scrollTo(), replaceText(TestConstants.TEST_PHONE_NUMBER), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.konbini_spinner)).perform(scrollTo(), click())
        Espresso.onData(CoreMatchers.allOf(CoreMatchers.`is`(CoreMatchers.instanceOf(KonbiniAdapter::class.java)))).atPosition(position).perform(scrollTo(), click())
        if(rememberKonbiniDetails) Espresso.onView(ViewMatchers.withId(R.id.check_can_remember_konbini_data)).perform(scrollTo(), click())
    }

    fun fillCardDetails(cardNumber: String? = null, shouldRememberCard: Boolean = false) {
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_email)).perform(scrollTo(), replaceText(TestConstants.TEST_EMAIL), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_cardholder_name)).perform(scrollTo(), replaceText(TestConstants.TEST_USER), closeSoftKeyboard())
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_card_number))).perform(scrollTo(), replaceText(cardNumber
                ?: generateCardNumber()))
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_expiry))).perform(scrollTo(), replaceText(TestConstants.TEST_EXPIRY), closeSoftKeyboard())
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_security))).perform(scrollTo(), replaceText(TestConstants.TEST_CVV), closeSoftKeyboard())
        if(shouldRememberCard) Espresso.onView(ViewMatchers.withId(R.id.check_can_remember_card)).perform(scrollTo(), click())
    }

    fun performPayment(cardNumber: String? = null, shouldRememberCard: Boolean = false) {
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_email)).perform(scrollTo(), replaceText(TestConstants.TEST_EMAIL), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_cardholder_name)).perform(scrollTo(), replaceText(TestConstants.TEST_USER), closeSoftKeyboard())
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_card_number))).perform(scrollTo(), replaceText(cardNumber
                ?: generateCardNumber()))
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_expiry))).perform(scrollTo(), replaceText(TestConstants.TEST_EXPIRY), closeSoftKeyboard())
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_security))).perform(scrollTo(), replaceText(TestConstants.TEST_CVV), closeSoftKeyboard())
        if(shouldRememberCard) Espresso.onView(ViewMatchers.withId(R.id.check_can_remember_card)).perform(scrollTo(), click())
        clickActionButton()
    }

    fun performRecurringPayment(cardNumber: String? = null) {
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_email)).perform(replaceText(TestConstants.TEST_EMAIL), closeSoftKeyboard(), closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_cardholder_name)).perform(replaceText(TestConstants.TEST_USER), closeSoftKeyboard(), closeSoftKeyboard())
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_card_number))).perform(replaceText(cardNumber
                ?: generateCardNumber()))
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_expiry))).perform(scrollTo(), replaceText(TestConstants.TEST_EXPIRY), closeSoftKeyboard())
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(com.univapay.R.id.card_detail_edit_text_security))).perform(scrollTo(), replaceText(TestConstants.TEST_CVV), closeSoftKeyboard())
        clickActionButton()
        Espresso.onData(Matchers.anything()).inAdapterView(Matchers.allOf<View>(ViewMatchers.withId(R.id.select_dialog_listview))).atPosition(0).perform(scrollTo(), click())
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(android.R.id.button1), ViewMatchers.withText(R.string.checkout_recurring_token_permission_positive_button)))
                .perform(scrollTo(), click())
    }

    fun performKonbiniPayment(title: String, description: String, settings: PaymentSettings?=null, konbiniPosition: Int, rememberCustomer: Boolean){
        assertKonbiniFragment(title, description, settings, rememberCustomer)
        fillKonbiniDetails(konbiniPosition)
        if(rememberCustomer) Espresso.onView(ViewMatchers.withId(R.id.check_can_remember_konbini_data)).perform(scrollTo(), click())
        clickActionButton()
    }

    fun performKonbiniPayment(title: String, description: String, settings: PaymentSettings?=null, konbiniPosition: Int) =
            performKonbiniPayment(title, description, settings, konbiniPosition, false)

    fun performStoredCardPayment(){
        assertBottomSheetView()
        Espresso.onView(ViewMatchers.withId(R.id.stored_data_bottom_pay)).perform(click())
    }

    fun clickActionButton() {
        Espresso.onView(ViewMatchers.withId(R.id.action_button)).perform(click())
    }

    fun assertIsDisplayed(@IdRes id: Int){
        try{
            Espresso.onView(ViewMatchers.withId(id))
                    .perform(scrollTo())
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        } catch (e: Exception){
            Espresso.onView(ViewMatchers.withId(id))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        }
    }

    fun assertIsNotDisplayed(@IdRes id: Int){
        Espresso.onView(Matchers.allOf<View>(ViewMatchers.withId(id),
                ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)))
    }

    fun assertAllDisplayed(resources: List<Int>){
        resources.forEach {id-> assertIsDisplayed(id) }
    }

    fun assertTextContent(fields: Map<Int, String>){
        fields.forEach{(res, s)->
            assertTextContent(res, s)
        }
    }

    fun assertTextContent(@IdRes id: Int, @IdRes textId: Int) {
        Espresso.onView(ViewMatchers.withId(id)).check(ViewAssertions.matches(ViewMatchers.withText(textId)))
    }

    fun assertTextContent(@IdRes id: Int, text: String) {
        Espresso.onView(ViewMatchers.withId(id)).check(ViewAssertions.matches(ViewMatchers.withText(text)))
    }

    fun assertAllNotDisplayed(resources: List<Int>){
        resources.forEach {id-> assertIsNotDisplayed(id) }
    }
}
