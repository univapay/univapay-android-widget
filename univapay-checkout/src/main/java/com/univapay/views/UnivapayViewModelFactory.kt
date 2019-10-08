package com.univapay.views

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.univapay.models.CheckoutArguments
import com.univapay.payments.CheckoutConfiguration

class UnivapayViewModelFactory(val args: CheckoutArguments, val config: CheckoutConfiguration<*>, val packageName: String): ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return UnivapayViewModel(args, config, packageName) as T
    }
}
