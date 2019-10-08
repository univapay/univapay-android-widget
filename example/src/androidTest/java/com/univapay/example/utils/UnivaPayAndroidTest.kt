package com.univapay.example.utils

import android.support.test.InstrumentationRegistry
import android.support.test.espresso.IdlingRegistry
import android.support.test.espresso.idling.CountingIdlingResource
import com.univapay.UnivapayCheckout
import com.univapay.example.utils.TestUtils.getUnivapay
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.KonbiniConfiguration
import com.univapay.sdk.models.response.applicationtoken.StoreApplicationJWT
import com.univapay.sdk.models.response.store.StoreWithConfiguration
import com.univapay.sdk.utils.builders.CardConfigurationBuilder
import org.junit.After
import org.junit.Before
import java.util.*

abstract class UnivaPayAndroidTest {

    val univapay: UnivapaySDK by getUnivapay()

    val resources = InstrumentationRegistry.getTargetContext().resources

    private fun createIdlingResource(): CountingIdlingResource{
        val idlingResource = CountingIdlingResource(UUID.randomUUID().toString())
        idlingResource.increment()
        idlingRegistry.register(idlingResource)
        return idlingResource
    }

    private fun destroyIdlingResource(idlingResource: CountingIdlingResource){
        idlingResource.decrement()
        idlingRegistry.unregister(idlingResource)
    }

    val idlingRegistry = IdlingRegistry.getInstance()

    var checkout: UnivapayCheckout? = null

    val store: StoreWithConfiguration by TestUtils.getStore(univapay)

    val appJWT: StoreApplicationJWT by TestUtils.getAppToken(univapay, store)

    fun allowKonbiniPayments(){
        univapay.updateStore(store.id)
                .withConvenienceConfiguration(
                        KonbiniConfiguration(true)
                )
                .build()
                .dispatch()
    }

    fun disableCardPayments(){
        univapay.updateStore(store.id)
                .withCardConfiguration(
                        CardConfigurationBuilder()
                                .withEnabled(false)
                                .build()
                ).build()
                .dispatch()
    }

    @Before
    fun setup() {
        TestUtils.setup(univapay, store)
        checkout = null
    }

    @After
    fun teardown() {
        TestUtils.teardown(univapay, store.id, checkout)
    }

    fun showAndWait(checkout: UnivapayCheckout?){
        val idlingResource = createIdlingResource()
        checkout?.let { b->
            b.show({
                destroyIdlingResource(idlingResource)
            },
            {
                destroyIdlingResource(idlingResource)
            }
            )
        }
    }

}
