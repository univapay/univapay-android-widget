package com.univapay.adapters

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.univapay.activities.CheckoutActivity
import com.univapay.adapters.StoredDataBottomSheet.Companion.PAYMENT_DATA_DETAILS_TAG
import com.univapay.fragments.ProcessFragment
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.utils.bindNotNullable
import com.univapay.views.UnivapayViewModel

class StoredKonbinisAdapter(private val tokens: MutableList<TransactionTokenWithData>,
                            val context: Context): CustomRecyclerViewAdapter<StoredKonbinisAdapter.ViewHolder>(){
    class ViewHolder(val storedKonbinisView: View): RecyclerView.ViewHolder(storedKonbinisView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoredKonbinisAdapter.ViewHolder {
        val storedKonbiniView: View = LayoutInflater.from(parent.context).inflate(com.univapay.R.layout.stored_konbini_view, parent, false)
        return StoredKonbinisAdapter.ViewHolder(storedKonbiniView)
    }

    override fun getItemCount(): Int {
        return tokens.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val konbiniNameView: TextView = bindNotNullable(holder.storedKonbinisView, com.univapay.R.id.stored_konbini_name)
        val konbiniPhoneView: TextView = bindNotNullable(holder.storedKonbinisView, com.univapay.R.id.stored_konbini_phone)
        val konbiniImageView: ImageView = bindNotNullable(holder.storedKonbinisView, com.univapay.R.id.stored_konbini_image)

        val token = tokens[position]

        val konbiniData = token.data.asKonbiniPaymentData()
        val phoneNumber = konbiniData.phoneNumber
        val konbiniName = context.resources.getString(KonbiniAdapter.fromEnum(konbiniData.convenienceStore).nameResId)

        konbiniNameView.text = konbiniName
        konbiniPhoneView.text = "+${phoneNumber.countryCode} ${phoneNumber.localNumber}"
        konbiniImageView.setImageResource(KonbiniAdapter.fromEnum(konbiniData.convenienceStore).imageResId)

        val checkout = context as CheckoutActivity?

        checkout?.let {
            holder.storedKonbinisView.setOnClickListener{
                if (UnivapayViewModel.instance.args.useDisplayQrMode) {
                    StoredDataBottomSheet.displayQr(token, checkout)
                } else {

                    val bottomSheet = StoredDataBottomSheet()
                    val args = Bundle()
                    args.putParcelable(StoredDataBottomSheet.ARG_BOTTOM_SHEET_TOKEN, TransactionTokenWithDataParcel(token))
                    bottomSheet.arguments = args

                    bottomSheet.onPaymentListener = {
                        ProcessFragment().display(checkout, TransactionTokenWithDataParcel(token), PaymentTypeName.CARD, null)
                    }

                    bottomSheet.onQrListener = {
                        StoredDataBottomSheet.displayQr(token, checkout)
                    }

                    bottomSheet.onDeleteListener = {
                        StoredDataBottomSheet.deleteToken(token, context) {
                            remove(holder.adapterPosition)
                        }
                    }

                    bottomSheet.show(checkout.supportFragmentManager, PAYMENT_DATA_DETAILS_TAG)
                }
            }
        }

    }

    override fun remove(position: Int){
        tokens.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    override fun getToken(position: Int): TransactionTokenWithData{
        return tokens[position]
    }
}
