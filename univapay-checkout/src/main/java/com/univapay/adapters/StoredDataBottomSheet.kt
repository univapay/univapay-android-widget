package com.univapay.adapters

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.univapay.activities.CheckoutActivity
import com.univapay.fragments.QrDialogFragment
import com.univapay.models.TransactionTokenParcel
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.views.UnivapayViewModel
import com.univapay.sdk.models.common.Void
import com.univapay.sdk.models.response.transactiontoken.TransactionToken
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.utils.UnivapayCallback

class StoredDataBottomSheet: BottomSheetDialogFragment(){

    var onPaymentListener: ((View)-> Unit)? = null
    var onQrListener: ((View)-> Unit)? = null
    var onDeleteListener: ((View)-> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(com.univapay.R.layout.card_detail_bottom_sheet, container, false)

        arguments?.let {args->
            val token: TransactionTokenWithData = args.getParcelable<TransactionTokenWithDataParcel>(ARG_BOTTOM_SHEET_TOKEN).transactionTokenInfo!!

            if(token.data.card != null){
                val cardData = token.data.card
                view.findViewById<TextView>(com.univapay.R.id.stored_data_bottom_title_main)?.text = resources.getString(com.univapay.R.string.checkout_stored_bottom_title_main_card, cardData.cardholder)
                view.findViewById<TextView>(com.univapay.R.id.stored_data_bottom_title_secondary)?.text = resources.getString(com.univapay.R.string.checkout_stored_bottom_title_secondary_card, cardData.lastFour)
            }

            else if (token.data.asKonbiniPaymentData() != null){
                val konbiniData = token.data.asKonbiniPaymentData()
                val konbiniName = resources.getString(KonbiniAdapter.fromEnum(konbiniData.convenienceStore).nameResId)
                view.findViewById<TextView>(com.univapay.R.id.stored_data_bottom_title_main)?.text = resources.getString(com.univapay.R.string.checkout_stored_bottom_title_main_konbini, konbiniName)
                view.findViewById<TextView>(com.univapay.R.id.stored_data_bottom_title_secondary)?.text = resources.getString(com.univapay.R.string.checkout_stored_bottom_title_secondary_konbini, konbiniData.phoneNumber.localNumber)
            }


            view.findViewById<View>(com.univapay.R.id.stored_data_bottom_pay)?.apply {
                setOnClickListener {v ->
                    onPaymentListener?.invoke(v)
                    dismiss()
                }
            }

            view.findViewById<View>(com.univapay.R.id.stored_data_bottom_qr)?.apply{
                setOnClickListener {v->
                    onQrListener?.invoke(v)
                    dismiss()
                }
            }

            view.findViewById<View>(com.univapay.R.id.stored_data_bottom_delete)?.apply{
                setOnClickListener{v->
                    onDeleteListener?.invoke(v)
                    dismiss()

                }
            }
        }

        return view
    }

    companion object {
        const val ARG_BOTTOM_SHEET_TOKEN = "ARG_BOTTOM_SHEET_TOKEN"
        const val ARG_BOTTOM_EXCEEDS_THRESHOLD = "ARG_BOTTOM_EXCEEDS_THRESHOLD"
        const val ARG_QR_ENABLED = "ARG_QR_ENABLED"
        const val ARG_QR_TOKEN = "ARG_QR_TOKEN"

        const val QR_DISPLAY_TAG = "QR_VIEW"
        const val PAYMENT_DATA_DETAILS_TAG = "PAYMENT_DATA_DETAILS_TAG"

        fun deleteToken(token: TransactionTokenWithData, context: Context, removeAndNotify: () -> Unit){
            UnivapayViewModel.instance.univapay.deleteTransactionToken(token.storeId, token.id)
                    .build()
                    .dispatch(object: UnivapayCallback<Void> {
                        override fun getResponse(response: Void?) {
                            removeAndNotify()
                        }

                        override fun getFailure(error: Throwable?) {
                            val toast = Toast.makeText(context, "Could not delete, please try again.", Toast.LENGTH_LONG)
                            toast.show()
                        }
                    })
        }

        fun displayQr(token: TransactionToken, checkoutActivity: CheckoutActivity){
            val qrDisplay = QrDialogFragment()
            val qrArgs = Bundle()
            qrArgs.putParcelable(ARG_QR_TOKEN, TransactionTokenParcel(token))
            qrDisplay.arguments = qrArgs
            qrDisplay.show(checkoutActivity.supportFragmentManager, QR_DISPLAY_TAG)
        }
    }
}
