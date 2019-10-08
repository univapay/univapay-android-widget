package com.univapay.tasks

import android.os.AsyncTask
import com.univapay.sdk.UnivapaySDK
import com.univapay.sdk.models.common.UnivapayCustomerId
import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData
import com.univapay.sdk.types.TransactionTokenType
import java.util.*

/**
 * Asynchronous task to retrieve the tokens associated with a <code>univapay-customer-id</code>
 */
class FetchTokensTask(val univapay: UnivapaySDK, val customerId: UUID, val callback: (MutableList<TransactionTokenWithData>) -> Unit): AsyncTask<Unit, Int, MutableList<TransactionTokenWithData>>(){

    override fun doInBackground(vararg params: Unit?): MutableList<TransactionTokenWithData> {
        return univapay.listTransactionTokens()
                .withCustomerId(UnivapayCustomerId(customerId))
                .withType(TransactionTokenType.RECURRING)
                .asIterable().flatMap {tokensList->
                    tokensList.toMutableList().map { token->
                        univapay.getTransactionToken(token.storeId, token.id).dispatch()
                    }
                }.toMutableList()
    }

    override fun onPostExecute(result: MutableList<TransactionTokenWithData>?) {
        callback(result!!)
    }
}
