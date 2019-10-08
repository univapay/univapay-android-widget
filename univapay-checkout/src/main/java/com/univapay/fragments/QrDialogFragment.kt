package com.univapay.fragments

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.support.annotation.StringRes
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.univapay.adapters.StoredDataBottomSheet
import com.univapay.models.TransactionTokenParcel
import com.univapay.sdk.models.response.UnivapayBinaryData
import com.univapay.sdk.models.response.transactiontoken.TransactionToken
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenAlias
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.views.UnivapayViewModel
import java.util.*
import kotlin.math.max

class QrDialogFragment: DialogFragment(){

    private val handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(com.univapay.R.layout.stored_data_qr_view, null)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val token: TransactionToken = arguments?.getParcelable<TransactionTokenParcel>(StoredDataBottomSheet.ARG_QR_TOKEN)?.transactionTokenInfo!!

        updateQRCodeRunnable(token, view, handler, true).run()
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        handler.removeCallbacksAndMessages(null)
    }

    private fun updateQRCodeRunnable(token: TransactionToken, view: View, handler: Handler, firstTime: Boolean): Runnable {
        return object: Runnable{
            override fun run() {

                val qrMessage: TextView? = view.findViewById(com.univapay.R.id.stored_data_qr_messsage)
                if(firstTime){
                    qrMessage?.text = resources.getText(com.univapay.R.string.checkout_qr_display_wait)
                }


                context?.apply {
                    val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

//                  Check if connected to internet and proceed to update if available
                    val isConnectedToInternet = cm.activeNetworkInfo?.isConnected
                    if(isConnectedToInternet ?: false){
                        UnivapayViewModel.instance.univapay.createTokenAlias(token.id)
                                .withValidUntil(Date(Date().time + 60000 ))
                                .build()
                                .dispatch(object: UnivapayCallback<TransactionTokenAlias> {
                                    override fun getResponse(alias: TransactionTokenAlias) {
                                        val requestBuilder = UnivapayViewModel.instance.univapay
                                                .getTokenAliasImage(token.storeId, alias.key)

                                                UnivapayViewModel.instance.args.qrOptions
                                                        ?.attachOptionsToBuilder(requestBuilder)

                                                requestBuilder
                                                .build()
                                                .dispatch(object: UnivapayCallback<UnivapayBinaryData> {
                                                    override fun getResponse(image: UnivapayBinaryData) {
                                                        val qrBytes = image.bytes
                                                        val bitmap: Bitmap = BitmapFactory.decodeByteArray(qrBytes, 0, qrBytes.size)
                                                        val scaledImage = Bitmap.createScaledBitmap(bitmap, 800, 800, false)
                                                        val qrView: ImageView? = view.findViewById(com.univapay.R.id.stored_data_qr_display)
                                                        qrView?.setImageBitmap(scaledImage)

                                                        qrMessage?.apply{
                                                            text = resources.getText(com.univapay.R.string.checkout_qr_display_refresh_notice)
                                                            width = scaledImage.width
                                                        }

//                                                      If the token is very close to expiring, just refresh immediately, otherwise wait until it expires and refresh
                                                        val millisUntilNextUpdate = max(0, alias.validUntil.time - Date().time - 200)

                                                        handler.postDelayed(updateQRCodeRunnable(token, view, handler, false), millisUntilNextUpdate)
                                                    }

                                                    override fun getFailure(t: Throwable?) {
                                                        displayErrorMessage(com.univapay.R.string.checkout_qr_display_request_error, view)
                                                    }
                                                })
                                    }

                                    override fun getFailure(t: Throwable?) {
                                        displayErrorMessage(com.univapay.R.string.checkout_qr_display_request_error, view)

                                    }
                                })
                    } else {
                        displayErrorMessage(com.univapay.R.string.checkout_qr_display_not_connected, view)
                    }
                }
            }
        }
    }

    private fun displayErrorMessage(@StringRes messageId: Int, view: View){
        val qrView: ImageView? = view.findViewById(com.univapay.R.id.stored_data_qr_display)
        qrView?.setImageBitmap(null)
        val qrMessage: TextView? = view.findViewById(com.univapay.R.id.stored_data_qr_messsage)
        qrMessage?.text = resources.getText(messageId)
        handler.removeCallbacksAndMessages(null)
    }
}
