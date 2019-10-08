package com.univapay

import android.app.Application
import android.content.Context

class UnivapayApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {

        private lateinit var instance: UnivapayApplication

        val context: Context
            get() = instance
    }
}
