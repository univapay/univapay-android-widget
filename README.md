# UnivaPay Android Widget

The UnivaPay Android Widget offers a convenient way of making payments with UnivaPay from your application in a convenient way. It is PCI-compliant, so you don't have to worry about storing sensitive information from your customers.

The Android widget lets you configure and display a Checkout activity for collecting payments data by connecting with the UnivaPay API. Its features include:

- Appearance customization to make it fit your Store's UI

- Tokenize your customer's payment data into `One-Time`, `Recurring` or `Subscription` tokens

- Create charges and subscriptions

- Allow customers to store and manage their credit cards for faster payments.

- Several hook points to execute your own custom callbacks on token, charge or subscription creation or failure. 


## Installation

### Android Studio (or Gradle)

No need to clone the repository or download any files -- just add this line to your app's `build.gradle` inside the `dependencies` section:

    implementation 'com.univapay:univapay-android-widget:0.0.1'

Note: We recommend that you don't use `compile 'com.univapay:univapay-android-widget:+`, as future versions of the Android Widget may not maintain full backwards compatibility. When such a change occurs, a major version number change will accompany it.

### Eclipse

Note: as Google has stopped supporting Eclipse for Android Development, we will no longer be actively testing the project's compatibility within Eclipse. You may still clone and include the library as you would any other Android library project.

## Basic Usage

The Android Widget uses the builder pattern to let you configure the Checkout widget. First, instantiate a builder:

```kotlin
val builder = UnivapayCheckout.Builder(
                    AppCredentials(APP_ID),
                    MoneyLike(amount, currency),
                    OneTimeTokenCheckout().asPayment()
            )
```

### Credentials
 
You need to create a set of application credentials to begin using the Checkout.
An `APP_ID` can be obtained by creating an Application JWT for your store. You can create one using the UnivaPay [Merchant Console](https://merchant.univapay.com/login).
By default, the checkout widget will use the package name of your app (as stated in your Manifest file) as the origin for each request. Therefore, make sure to add the package name as a domain when creating your Application JWT.
You can override the default origin in the following way:

```kotlin
builder.setOrigin(URL("http://my.custom.origin"))
```

To be able to use the origin shown in the example above, create an Application JWT for your store including `my.custom.origin` as a domain.


### Money

Monetary quantities are handled by using the `MoneyLike` class, which takes care of currency conversion, decimal places, etc. You just have to take care of always passing amounts as `BigInteger` objects.
  
### Checkout Configuration

The way the checkout is performed depends on two parameters: the **type of token** you wish to use, and the **type of checkout**.
There are three types of tokens: *One-Time*, *Recurring* and *Subscription*, where the first two can be used for creating charges.
The checkout can be a *Token-Only Checkout* or a *Full-Payment Checkout*. The first one simply creates the transaction token, while the latter creates a token and completes the payment (charge or subscription, depending on the token type).

There is one type of Checkout Configuration class for each type of token:

- `OneTimeTokenCheckout`
- `RecurringTokenCheckout`
- `SubscriptionCheckout`

Passing an instance of any of these three configuration classes to the Checkout Builder lets you perform a *Token-Only* checkout.
Also, all of the classes above implement the `Payable` interface, which means that instances can be turned into *Full-Payment* checkout configuration instances by calling the `asPayment(settings: PaymentSettings)` method.
Each class requires a different set of `PaymentSettings`.

This is the minimum set of arguments to create your checkout widget. Displaying it is as easy as:
```kotlin
val checkout: UnivapayCheckout = builder.build()
checkout.show()
```

This will display the widget's UI depending on the settings you passed to the builder.

The user will now be able to perform their payment. Be sure to finalize the checkout after it's no longer needed:

```kotlin
checkout.finalize()
``` 

## Customization

The previous code gives you a fully functional checkout widget, and your customers can use it for making payments, but it still doesn't integrate well into your app.

You  can customize the widget in several ways:

### Callbacks

The checkout process consists of up to two stages:

1- Token Creation: the checkout widget sends the customer's data to the UnivaPay API for tokenization. This stage is performed on both Checkout types (Token-Only and Full-Payment).

2- Payment completion: this stage uses the previously created token to create a charge or subscription, depending on the type of token employed. It is only performed on **Full-Payment** checkouts.

You can execute your code at the end of both stages by setting the corresponding callback.

Note that you may set both types of callbacks when setting up your checkout widget, but only the Token Creation callback will be executed if the checkout type is *Token-Only*.

The UnivaPay Checkout allows you to set three types of callbacks:

#### Token callbacks:

These get executed at the end of the *Token Creation* stage, and give you access to the transaction token returned by the API.
Note that this block of code is called whether the checkout type is *Full-Payment* or *Token-Only*. 
If you plan to process the payment on your own, be sure to set the checkout type to *Token-Only* and use the [UnivaPay Java SDK](https://github.com/univapay/univapay-java-sdk) to process the payment using the returned token.
However, if you wish to delegate the payment completion to the checkout by setting the checkout type as *Full-Payment*, you might still want to call some of your code at this stage.

To hook on the token creation stage, you must implements an `onSuccess` and `onError` callback.

#### Charge callbacks:

Code that gets executed after the widget has attempted to create a charge. It requires you to implement the following callbacks:

- **onSuccess**: The charge was successfully created.
- **onError**: This block gets called when the server returns a validation error, or when the charge was created but its status reflects a failure in its processing.
- **onUndetermined**: A charge was created, but its current state is *Pending*, meaning that it is not clear whether it succeeded or failed.

#### Subscription callbacks:

This block gets executed after an attempt to create a subscription. Just like with charges, you must implement the following callbacks:

- **onSuccess**: The subscription was successfully created.
- **onError**: The request could not be processed due to a validation error or to a payment gateway error, resulting on an unsuccessful subscription status.
- **onUndetermined**: Same as with charges, the subscription was created, but its state is still *Unverified*.


### Functionality

You can also customize the way your customers use the checkout widget. The following settings are available:

- **setAddress**: If set to true, it displays an additional screen where your customers have to enter their billing address information.

- **setCVVRequired**: If set to true, the credit card's security code is set as a required field. Setting it to false allows your customers to make payments without entering their security code. Note that in this case your merchant configuration must allow empty CVVs. IF this is not the case, this setting is ignored. Please check your settings in the Merchant Console before using this setting.

- **rememberCardsForUser**: This allows you to pass a customer ID as a **UUID**. When set, the widget will display a checkbox in the payment details screen where the customer can choose whether to remember his card details for easier payments in the future.
If the user has at least one stored card, the first screen presents the list of stored cards, from where a payment can be made. This setting is ignored in the case of `Subscription` tokens.

- **setMetadata**: allows you to attach metadata to your requests.

### Appearance

Customize the appearance of your widget by setting the following options:

- **setImageResource**: your store's logo or any image that you want to display on the billing address and payment details screens.

- **setTitle**: the widget's title

- **setDescription**: the widget's description

#### Styling

Styling the widget theme.

```xml
<resources>
    <color name="checkout_color_primary">#303f9f</color>
    <color name="checkout_color_primary_dark">#283593</color>
    <color name="checkout_main_color">#303f9f</color>
    <color name="checkout_text_color_primary">#212121</color>
    <color name="checkout_text_color_secondary">#757575</color>

    <color name="checkout_header_background">#c5cae9</color>
    <color name="checkout_header_bottom_line">#7986cb</color>
    <color name="checkout_action_button_text_color">@android:color/white</color>
    <color name="checkout_drawable_color">#283593</color>

    <color name="checkout_progress_animation_color">#283593</color>
    <color name="checkout_progress_animation_success_color">#6AB344</color>
    <color name="checkout_progress_animation_fail_color">@android:color/holo_red_light</color>
    <color name="checkout_progress_animation_undetermined_color">#ef6c00</color>
</resources>
```


## Building the example project

1. Clone the git repository.
2. Be sure you've installed the Android SDK with API Level 19, _android-support-v7_ and _com.android.support:design_. This is only a requirement for development.
3. Import the project.
    * For Android Studio, choose _Import Project..._ from the "Welcome to Android Studio" screen. Select the `build.gradle` file at the top of the `univapay-android-widget` repository.
    * For Eclipse, [import](http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.user/tasks/tasks-importproject.htm) the _example_ and _gpaycheckout_ folders into, by using `Import -> General -> Existing Projects into Workspace`, and browsing to the `univapay-android-widget` folder.
4. Build and run the project on your device or in the Android emulator.
