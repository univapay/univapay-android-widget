package com.univapay.adapters

import com.univapay.sdk.models.response.transactiontoken.TransactionTokenWithData

interface StoredDataAdapter{

    fun remove(position: Int)

    fun getToken(position: Int): TransactionTokenWithData

}
