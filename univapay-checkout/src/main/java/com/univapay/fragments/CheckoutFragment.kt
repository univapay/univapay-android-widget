package com.univapay.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class CheckoutFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        return inflater.inflate(com.univapay.R.layout.activity_checkout, container, false)

    }

    companion object {
        val FRAGMENT_TAG = this::class.java.name
    }
}
