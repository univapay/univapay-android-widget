package com.univapay.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.response.UnivapayBinaryData
import com.univapay.sdk.models.response.transactiontoken.TransactionToken
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenAlias
import com.univapay.sdk.types.TemporaryTokenAliasQRLogo
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.views.UnivapayViewModel
import java.util.*

class UpdateQRJob constructor(val nextUpdate: Date, val univapay: UnivapaySDK, val token: TransactionToken, val view: View): JobService(){

    override fun onStopJob(params: JobParameters?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        UnivapayViewModel.instance.univapay.createTokenAlias(token.id)
                .build()
                .dispatch(object: UnivapayCallback<TransactionTokenAlias> {
                    override fun getResponse(alias: TransactionTokenAlias) {
                        UnivapayViewModel.instance.univapay
                                .getTokenAliasImage(token.storeId, alias.key)
                                .withLogoType(TemporaryTokenAliasQRLogo.BACKGROUND)
                                .build()
                                .dispatch(object: UnivapayCallback<UnivapayBinaryData> {
                                    override fun getResponse(image: UnivapayBinaryData) {
                                        val qrBytes = image.bytes
                                        val bitmap: Bitmap = BitmapFactory.decodeByteArray(qrBytes, 0, qrBytes.size)
                                        val qrView: ImageView? = view.findViewById(com.univapay.R.id.stored_data_qr_display)
                                        qrView?.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 800, 800, false))
                                    }

                                    override fun getFailure(t: Throwable?) {
                                        Toast.makeText(view.context, "Could not display QR code", Toast.LENGTH_SHORT).show()
                                    }
                                })
                    }

                    override fun getFailure(t: Throwable?) {
                        Toast.makeText(view.context, "Could not display QR code", Toast.LENGTH_SHORT).show()
                    }
                })

        return false
    }
}
