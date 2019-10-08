# UnivaPay Androidウィジェット

UnivaPay Android Widgetは、アプリケーションからUnivaPayで便利な方法で支払いを行う便利な方法を提供します。 PCIに準拠しているため、顧客からの機密情報の保存について心配する必要はありません。

Androidウィジェットを使用すると、UnivaPay APIに接続して、支払いデータを収集するためのCheckoutアクティビティを構成および表示できます。その機能は次のとおりです。

- ストアのUIに合わせた外観のカスタマイズ

- 顧客の支払いデータを`One-Time`、`Recurring`、または`Subscription`トークンにトークン化する

- 料金とサブスクリプションを作成する

- 顧客がクレジットカードを保管および管理して、支払いを高速化できるようにします。

- トークン、チャージ、サブスクリプションの作成または失敗時に独自のカスタムコールバックを実行するためのいくつかのフックポイント。

## インストール

### Android Studio（またはGradle）

リポジトリのクローンを作成したり、ファイルをダウンロードしたりする必要はありません。アプリの `build.gradle`の` dependencies`セクションに次の行を追加するだけです。

    implementation 'com.univapay:univapay-android-widget:0.0.1'

注：Android Widgetの将来のバージョンでは完全な下位互換性が維持されない可能性があるため、`compile 'com.univapay:univapay-android-widget:+'`は使用しないことをお勧めします。そのような変更が発生すると、メジャーバージョン番号の変更がそれに伴います。

### Eclipse

注：GoogleはAndroid開発用のEclipseのサポートを停止しているため、Eclipse内でのプロジェクトの互換性を積極的にテストすることはありません。他のAndroidライブラリプロジェクトと同様に、ライブラリを複製して含めることができます。

## 基本的な使用法

Androidウィジェットは、ビルダーパターンを使用して、Checkoutウィジェットを構成できます。まず、ビルダーをインスタンス化します。

```kotlin
val builder = UnivapayCheckout.Builder(
                    AppCredentials(APP_ID),
                    MoneyLike(amount, currency),
                    OneTimeTokenCheckout().asPayment()
            )
```

### 資格情報
 
Checkoutの使用を開始するには、一連のアプリケーション資格情報を作成する必要があります。
ストアのアプリケーションJWTを作成することで、`APP_ID`を取得できます。 UnivaPay [マーチャントコンソール](https://merchant.univapay.com/login)を使用して作成できます。
デフォルトでは、チェックアウトウィジェットは、アプリのパッケージ名（マニフェストファイルに記載されているとおり）を各リクエストの発信元として使用します。したがって、アプリケーションJWTを作成するときに、パッケージ名をドメインとして追加するようにしてください。
次の方法でデフォルトの起点をオーバーライドできます。

```kotlin
builder.setOrigin(URL("http://my.custom.origin"))
```

上記の例に示されているオリジンを使用できるようにするには、ドメインとして`my.custom.origin`を含むストアのアプリケーションJWTを作成します。

### お金

通貨の量は、通貨の変換や小数点以下の桁数などを処理する`MoneyLike`クラスを使用して処理されます。常に量を`BigInteger`オブジェクトとして渡すだけで十分です。
  
### チェックアウト設定

チェックアウトの実行方法は、使用する**タイプのトークン**と、**チェックアウトのタイプ**の2つのパラメーターによって異なります。
トークンには3つのタイプがあります：*ワンタイム*、*繰り返し*、および*サブスクリプション*。最初の2つは料金の作成に使用できます。
チェックアウトは、*トークンのみのチェックアウト*または*全額チェックアウト*です。前者は単にトランザクショントークンを作成し、後者はトークンを作成して支払いを完了します（トークンタイプに応じて請求またはサブスクリプション）。

トークンのタイプごとに1つのタイプのチェックアウト構成クラスがあります。

- `OneTimeTokenCheckout`
- `RecurringTokenCheckout`
- `SubscriptionCheckout`

これら3つの構成クラスのいずれかのインスタンスをCheckout Builderに渡すと、* Token-Only *チェックアウトを実行できます。
また、上記のすべてのクラスは `Payable`インターフェースを実装します。つまり、` asPayment（settings：PaymentSettings） `メソッドを呼び出すことにより、インスタンスを* Full-Payment *チェックアウト設定インスタンスに変換できます。
各クラスには、異なる「PaymentSettings」のセットが必要です。

これは、チェックアウトウィジェットを作成するための引数の最小セットです。それを表示するのは簡単です：

```kotlin
val checkout: UnivapayCheckout = builder.build()
checkout.show()
```

これにより、ビルダーに渡した設定に応じてウィジェットのUIが表示されます。

これで、ユーザーは支払いを実行できるようになります。チェックアウトが不要になったら、必ずファイナライズを完了してください。

```kotlin
checkout.finalize()
``` 

## カスタマイズ

前のコードは完全に機能するチェックアウトウィジェットを提供し、顧客はそれを使用して支払いを行うことができますが、それでもアプリにうまく統合できません。

ウィジェットはいくつかの方法でカスタマイズできます。

### コールバック

チェックアウトプロセスは、最大2つのステージで構成されます。

1-トークン作成：チェックアウトウィジェットは、トークン化のために顧客のデータをUnivaPay APIに送信します。この段階は、両方のチェックアウトタイプ（トークンのみと全額）で実行されます。

2-支払いの完了：この段階では、使用したトークンの種類に応じて、以前に作成したトークンを使用して、課金またはサブスクリプションを作成します。 **全額支払い**チェックアウトでのみ実行されます。

対応するコールバックを設定することにより、両方の段階の最後でコードを実行できます。

チェックアウトウィジェットを設定するときに両方のタイプのコールバックを設定できますが、チェックアウトタイプが* Token-Only *の場合、トークン作成コールバックのみが実行されることに注意してください。

UnivaPay Checkoutでは、3種類のコールバックを設定できます。

#### トークンコールバック：

これらは* Token Creation *ステージの最後に実行され、APIから返されたトランザクショントークンへのアクセスを提供します。
このコードブロックは、チェックアウトタイプが* Full-Payment *または* Token-Only *のいずれであるかにかかわらず呼び出されることに注意してください。
自分で支払いを処理する予定の場合は、必ずチェックアウトの種類を* Token-Only *に設定し、[UnivaPay Java SDK]（https://github.com/univapay/univapay-java-sdk）を使用してください。返されたトークンを使用して支払いを処理します。
ただし、チェックアウトタイプを* Full-Payment *に設定して支払い完了をチェックアウトに委任する場合は、この段階でコードの一部を呼び出すことができます。

トークン作成段階でフックするには、 `onSuccess`と` onError`コールバックを実装する必要があります。

#### コールバックの請求：

ウィジェットが課金を作成しようとした後に実行されるコード。次のコールバックを実装する必要があります。


- **onSuccess**: The charge was successfully created.
- **onError**: This block gets called when the server returns a validation error, or when the charge was created but its status reflects a failure in its processing.
- **onUndetermined**: A charge was created, but its current state is *Pending*, meaning that it is not clear whether it succeeded or failed.

#### サブスクリプションコールバック：

このブロックは、サブスクリプションを作成しようとした後に実行されます。料金と同様に、次のコールバックを実装する必要があります。

- **onSuccess**: The subscription was successfully created.
- **onError**: The request could not be processed due to a validation error or to a payment gateway error, resulting on an unsuccessful subscription status.
- **onUndetermined**: Same as with charges, the subscription was created, but its state is still *Unverified*.

### 機能

また、顧客がチェックアウトウィジェットを使用する方法をカスタマイズできます。次の設定を使用できます。


- **setAddress**: If set to true, it displays an additional screen where your customers have to enter their billing address information.

- **setCVVRequired**: If set to true, the credit card's security code is set as a required field. Setting it to false allows your customers to make payments without entering their security code. Note that in this case your merchant configuration must allow empty CVVs. IF this is not the case, this setting is ignored. Please check your settings in the Merchant Console before using this setting.

- **rememberCardsForUser**: This allows you to pass a customer ID as a **UUID**. When set, the widget will display a checkbox in the payment details screen where the customer can choose whether to remember his card details for easier payments in the future.
If the user has at least one stored card, the first screen presents the list of stored cards, from where a payment can be made. This setting is ignored in the case of `Subscription` tokens.

- **setMetadata**: allows you to attach metadata to your requests.

### 外観

以下のオプションを設定して、ウィジェットの外観をカスタマイズします。


- **setImageResource**: your store's logo or any image that you want to display on the billing address and payment details screens.

- **setTitle**: the widget's title

- **setDescription**: the widget's description

#### スタイリング

ウィジェットのテーマのスタイル設定。


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

## サンプルプロジェクトのビルド

1. gitリポジトリを複製します。
2. APIレベル19、_android-support-v7_および_com.android.support:design_を使用してAndroid SDKをインストールしたことを確認してください。これは開発の要件にすぎません。
3.プロジェクトをインポートします。
    * Android Studioの場合、[Android Studioへようこそ]画面から[プロジェクトのインポート...]を選択します。 `univapay-android-widget`リポジトリの上部にある` build.gradle`ファイルを選択します。
    * Eclipseの場合、[import]（http://help.eclipse.org/juno/topic/org.eclipse.platform.doc.user/tasks/tasks-importproject.htm）を使用して、_example_および_gpaycheckout_フォルダーにインポート->一般->既存のプロジェクトをワークスペースに追加し、 `univapay-android-widget`フォルダーを参照します。
4.デバイスまたはAndroidエミュレーターでプロジェクトをビルドして実行します。

## テストの実行
次の環境変数を設定する必要があります。
-`UNIVAPAY_ANDROID_TEST_ENDPOINT`：テストに使用するUnivaPay APIのURI。マーチャントアカウントを所有している必要があります。
-「UNIVAPAY_ANDROID_TEST_EMAIL」：販売者のメールアカウント
-`UNIVAPAY_ANDROID_TEST_PASSWORD`：アカウントのパスワード

端末でこれらの変数を設定したら、 `./ gradlew connectedAndroidTest`を実行します。

Android Studioを使用してテストを実行する場合は、上記の環境変数にアクセスできるターミナルで「open -a Android \ Studio」を実行してテストを初期化する必要があります。これは、Android Studioが必要な環境変数にアクセスできるようにするために必要です。

## 公開
現在、アーティファクトはS3にデプロイされています。新しいバージョンをリリースするには：
1. ** gradle.properties **ファイルの `VERSION_NAME`の値を目的のバージョン番号に変更します。
2. ** GRADLE_OPTS **環境変数に次を追加します。 `--add-modules java.xml.bind '-Dorg.gradle.jvmargs =-add-modules java.xml.bind'`
3. `./gradlew publish`を実行します

## 依存関係として追加
次のリポジトリをプロジェクトに追加します。
`` `
maven {
        url "s3：// gopay-static / android-widget"
        認証{
            awsIm（AwsImAuthentication）
        }
    }
`` `

次に、次の依存関係を追加します。
`` `
実装グループ： 'com.univapay'、名前： 'univapay-checkout'、バージョン： '0.0.1'、ext： 'aar'、分類子： 'release'
`` `
