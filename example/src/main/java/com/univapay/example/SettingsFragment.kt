package com.univapay.example

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat

class SettingsFragment: PreferenceFragmentCompat(){

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

}
