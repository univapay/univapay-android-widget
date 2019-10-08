package com.univapay.example.utils

import com.univapay.example.R
import com.univapay.example.utils.FragmentAssertions.resources
import com.univapay.sdk.models.common.MoneyLike
import com.univapay.sdk.models.common.auth.UserCredentials
import com.univapay.sdk.types.MetadataMap
import java.math.BigInteger
import java.util.*

class TestConstants {
    companion object {
        const val TITLE = "Test Title"
        const val DESCRIPTION = "Test Description"
        const val CURRENCY = "JPY"
        @JvmField val AMOUNT = BigInteger.valueOf(1500)
        @JvmField val SUBSCRIPTION_AMOUNT = BigInteger.valueOf(120000)
        const val ADDRESS_NAME = "Testing Name"
        const val ADDRESS_LINE1 = "Testing Line1"
        const val ADDRESS_LINE2 = "Testing Line2"
        const val ADDRESS_CITY = "CITY"
        const val ADDRESS_STATE = "STATE"
        const val ADDRESS_POST_CODE = "TEST"
        val ADDRESS_COUNTRY = resources.getString(R.string.japan)
        const val ADDRESS_AMOUNT = "¥1,500"
        const val TEST_NAME = "Mr. Test User"
        const val TEST_EMAIL = "test@test.univapay.com"
        const val TEST_PHONE_NUMBER = "8012341234"
        const val TEST_USER = "TEST USER"
        const val TEST_EXPIRY = "072033"
        const val TEST_CVV = "123"
        @JvmField val MONEY = MoneyLike(AMOUNT, CURRENCY)
        @JvmField val SUBSCRIPTION_MONEY = MoneyLike(SUBSCRIPTION_AMOUNT, CURRENCY)
        @JvmField val METADATA: MetadataMap = run{
            val metadataMap = MetadataMap()
            metadataMap.put("some_key", "some_value")
            metadataMap
        }
        @JvmField val DESCRIPTOR = "Some descriptor"
        @JvmField val CUSTOMER_UUID: UUID = UUID.fromString("a851228e-93ea-474c-abf6-23d2b3d67ce6")
        val LOGIN_CREDENTIALS: UserCredentials = UserCredentials(
                com.univapay.example.BuildConfig.UNIVAPAY_TEST_EMAIL,
                com.univapay.example.BuildConfig.UNIVAPAY_TEST_PASSWORD
        )

        val TEST_ENDPOINT = com.univapay.example.BuildConfig.TEST_ENDPOINT
        const val TEST_ORIGIN = "example.univapay.com"
        const val TEST_ORIGIN_URL = "http://$TEST_ORIGIN"
        val UNIVAPAY_SDK_CONFIG: UnivapayDebugSettings = UnivapayDebugSettings()
                .withEndpoint(TEST_ENDPOINT)
                .withTimeoutSeconds(10)
                .attachOrigin(TEST_ORIGIN_URL)
                .withRequestsLogging(true)

        val jpyLimit: BigInteger = BigInteger.valueOf(10000)
        val usdLimit: BigInteger = BigInteger.valueOf(1000)

        const val defaultLatchTimeout = 20000L
    }
}
