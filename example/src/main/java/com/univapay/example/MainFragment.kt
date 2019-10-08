package com.univapay.example

import android.app.FragmentTransaction
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.univapay.UnivapayCheckout
import com.univapay.models.AppCredentials
import com.univapay.models.QROptions
import com.univapay.models.UnivapayError
import com.univapay.sdk.models.response.charge.Charge
import com.univapay.sdk.models.response.subscription.FullSubscription
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.MetadataMap
import com.univapay.sdk.types.TemporaryTokenAliasQRLogo
import java.net.URI
import java.util.*


class MainFragment: Fragment(){
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_screen, container, false)

        val act = activity as MainActivity

        //      Setup the button that opens the settings screen:
        val settingsButton: AppCompatButton = view.findViewById(R.id.pref_configure_button)
        settingsButton.setOnClickListener{
            act.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.demo_main, SettingsFragment())
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
        }

//      Now set up the button that starts the widget:
        val widgetButton: Button = view.findViewById(R.id.pref_checkout_button)
        widgetButton.setOnClickListener {

//          Read the preferences from the demo application.
            val prefs: Helpers.CheckoutDemoSettings = Helpers.parsePreferences(context!!)

//          Create the widget passing your credentials and payment settings:
            val builder = UnivapayCheckout.Builder(
                    AppCredentials(
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcHBfdG9rZW4iLCJpYXQiOjE1Njk1NzUzMzAsInN0b3JlX2lkIjoiMTFlODkzZWMtMzY4NS05NjcwLWEzMDQtYWZkYTdmMjQxMWEzIiwibW9kZSI6InRlc3QiLCJjcmVhdG9yX2lkIjoiMTFlODgzM2QtZGE2YS1kNTc2LWEwYmUtYzNlNzM3Nzc5MzI3IiwiZG9tYWlucyI6WyJleGFtcGxlLnVuaXZhcGF5LmNvbSJdLCJtZXJjaGFudF9pZCI6IjExZTg4MzNkLWRhNmEtZDU3Ni1hMGJlLWMzZTczNzc3OTMyNyIsInZlcnNpb24iOjEsImp0aSI6IjExZTllMTA2LTZiNzgtYzFjNi1hNmZiLTM5ZDQ5NzJiZjgwOSJ9.fErY7_LbbIHmgwCeBKbiDwsFKBLpV-SWwEM1ujnvaV0"
                    ),
                Helpers.getSampleConfig(prefs.checkoutType, prefs.tokenType, prefs.settings)
            )

//          Configure the optional settings using the builder methods.
            builder.apply {

//              Set the widget's logo, title and description
                setImageResource(R.drawable.ic_logo)
                setTitle(prefs.title)
                setDescription(prefs.description)

//              Append some metadata
                setMetadata(object : MetadataMap() {
                    init {
                        put("product_code", "12345")
                    }
                })

//              Define a callback that will be triggered upon transaction token creation.
                setTokenCallback(
                        { transactionTokenWithData: TransactionTokenWithData?->

                            val toast = Toast.makeText(
                                    context,
                                    "Token ${transactionTokenWithData!!.id} of type ${transactionTokenWithData.type} created",
                                    Toast.LENGTH_SHORT
                            )
                            toast.show()
                        },
                        {error: UnivapayError? ->
                            val toast = Toast.makeText(context, error!!.message, Toast.LENGTH_SHORT)
                            toast.show()
                        }
                )

//              The following callback is triggered when a subscription is created. Also, set an onError callback to deal with possible transaction errors.
                setSubscriptionCallback(
                        {subscription: FullSubscription? ->
                            val toast = Toast.makeText(context, "Subscription ${subscription!!.id} Created", Toast.LENGTH_SHORT)
                            toast.show()
                        },
                        {error: UnivapayError? ->
                            val toast = Toast.makeText(context, error!!.message, Toast.LENGTH_SHORT)
                            toast.show()
                        },
                        {subscription ->
                            val toast = Toast.makeText(context, "Subscription ${subscription!!.id} Undetermined", Toast.LENGTH_SHORT)
                            toast.show()
                        }
                )

//              Same here for the case of charges:
                setChargeCallback(
//                          in case of a successful transaction:
                        {charge: Charge?->
                            val toast = Toast.makeText(context, "Charge ${charge!!.id} Created", Toast.LENGTH_SHORT)
                            toast.show()
                        },
//                          in case of error
                        { error: UnivapayError?->
                            val toast = Toast.makeText(context, error!!.message, Toast.LENGTH_SHORT)
                            toast.show()
                        },
//                          in case the charge's status could not be properly verified
                        {charge: Charge?->
                            val toast = Toast.makeText(context, "Charge ${charge!!.id} Undetermined", Toast.LENGTH_SHORT)
                            toast.show()
                        }
                )

//              The following creates a dummy customer ID to be attached to transaction tokens in case the user decides to store his payment data.
//              In a real use case, this customer ID should come from your application. In case your application does not use UUIDs as customer IDs,
//              you may use the `createCustomerID` method in the UnivaPay Java SDK.
                if (prefs.allowRememberCard) {
                    val customerId = UUID.fromString("a851228e-93ea-474c-abf6-23d2b3d67ce6")
                    rememberCardsForUser(customerId)
//                    rememberCardsForUser(customerId, true)
                }

//              Display a fragment to input the billing address data before going to the payment details.
                setAddress(prefs.requireAddress)

//              Require the card's CVV (only if your merchant configuration allows it).
                setCvvRequired(prefs.requireCvv)

                setOrigin(URI("http://example.univapay.com"))

                setTimeout(10)

                setEndpoint(URI("https://api.gyro-n.money/"))

                setQROptions(QROptions(
                        TemporaryTokenAliasQRLogo.BACKGROUND,
                        "#000000"
                ))

            }

            val univapayCheckout = builder.build()

            univapayCheckout.show(

//           Optional callback to be fired if the checkout initialization process is successful
            {
                val toast = Toast.makeText(
                        context,
                        "The initialization was successful!",
                        Toast.LENGTH_SHORT
                )
                toast.show()

//           Optional callback to be fired if the checkout initialization fails
            },
            {error ->
                val message = (
                        if(error?.message.isNullOrEmpty()){
                            ""
                        } else error?.message
                        )
                val toast = Toast.makeText(
                        context,
                        "Could not initialize: $message",
                        Toast.LENGTH_SHORT
                )
                toast.show()
            }
            )
        }
        return view
    }
}
