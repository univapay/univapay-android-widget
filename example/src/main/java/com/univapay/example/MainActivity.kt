package com.univapay.example

import android.app.FragmentTransaction
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


/**
 * This activity is meant to let you play with all the configurable parts of the UnivaPay Checkout and
 * learn about the widget's behavior under different settings.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState != null){
            return
        }

        supportFragmentManager
                .beginTransaction()
                .add(R.id.demo_main, MainFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit()
    }
}
