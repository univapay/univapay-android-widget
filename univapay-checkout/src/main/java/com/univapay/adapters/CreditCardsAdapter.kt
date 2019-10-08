package com.univapay.adapters

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.univapay.activities.CheckoutActivity
import com.univapay.adapters.StoredDataBottomSheet.Companion.ARG_BOTTOM_SHEET_TOKEN
import com.univapay.adapters.StoredDataBottomSheet.Companion.PAYMENT_DATA_DETAILS_TAG
import com.univapay.adapters.StoredDataBottomSheet.Companion.displayQr
import com.univapay.fragments.ProcessFragment
import com.univapay.models.CardType
import com.univapay.models.TransactionTokenWithDataParcel
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.PaymentTypeName
import com.univapay.sdk.utils.UnivapayCallback
import com.univapay.utils.bindNotNullable
import com.univapay.views.SecurityEditText
import com.univapay.views.UnivapayViewModel

class CreditCardsAdapter(private val tokens: MutableList<TransactionTokenWithData>,
                         val context: Context,
                         val exceedsThreshold: Boolean
): CustomRecyclerViewAdapter<CreditCardsAdapter.ViewHolder>(){

    class ViewHolder(val storedCardView: View): RecyclerView.ViewHolder(storedCardView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val storedCardView: View = LayoutInflater.from(parent.context).inflate(com.univapay.R.layout.stored_card_view, parent, false)
        return ViewHolder(storedCardView)
    }

    override fun getItemCount(): Int {
        return tokens.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardNumberView: TextView = bindNotNullable(holder.storedCardView, com.univapay.R.id.stored_card_number)
        val logoView: ImageView = bindNotNullable(holder.storedCardView, com.univapay.R.id.stored_card_logo)

        val token = tokens[position]
        var cardBrand: CardType
        try{
            cardBrand = CardType.valueOf(token.data.card.brand.toUpperCase())
        } catch (e: Exception){
            cardBrand = CardType.UNKNOWN
        }
        val cardNumberString = "XXXX XXXX XXXX ${token.data.card.lastFour}"
        cardNumberView.text = cardNumberString
        logoView.setImageResource(cardBrand.logoResource)

        val checkout = context as CheckoutActivity?

        checkout?.let {
            holder.storedCardView.setOnClickListener {
                if (UnivapayViewModel.instance.args.useDisplayQrMode) {
                    displayQr(token, checkout)
                } else {
                    val bottomSheet = StoredDataBottomSheet()
                    val args = Bundle()
                    args.putParcelable(ARG_BOTTOM_SHEET_TOKEN, TransactionTokenWithDataParcel(token))
                    bottomSheet.arguments = args

                    bottomSheet.onPaymentListener = {
                        if (exceedsThreshold) {
                            AlertDialog.Builder(checkout).apply {

                                setTitle(com.univapay.R.string.checkout_recurring_token_cvv_challenge_title)
                                setView(com.univapay.R.layout.dialog_cvv)
                                setPositiveButton(com.univapay.R.string.checkout_recurring_token_permission_positive_button, null)
                                setNegativeButton(com.univapay.R.string.checkout_recurring_token_permission_negative_button) { _, _ -> }
                                val cvvDialog = create()
                                cvvDialog.setOnShowListener { dialogInterface ->
                                    val cvvChallengeInput: SecurityEditText = (dialogInterface as AlertDialog).findViewById(com.univapay.R.id.cvv_challenge)!!
                                    dialogInterface.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                        cvvChallengeInput.validate()
                                        if (cvvChallengeInput.isValid) {
                                            UnivapayViewModel.instance.univapay.updateTransactionToken(token.storeId, token.id).withCvv(cvvChallengeInput.text.toString().toInt()).dispatch(object : UnivapayCallback<TransactionTokenWithData> {
                                                override fun getResponse(updatedToken: TransactionTokenWithData) {
                                                    dialogInterface.dismiss()
                                                    ProcessFragment().display(checkout, TransactionTokenWithDataParcel(updatedToken), PaymentTypeName.CARD, exceedsThreshold)
                                                }

                                                override fun getFailure(error: Throwable?) {
                                                    //                                              TODO: how should this be handled?
                                                }
                                            })
                                        }
                                    }
                                }
                                cvvDialog.show()
                            }

                        } else {
                            ProcessFragment().display(checkout, TransactionTokenWithDataParcel(token), PaymentTypeName.CARD, exceedsThreshold)
                        }
                    }

                    bottomSheet.onQrListener = {
                        displayQr(token, checkout)
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
