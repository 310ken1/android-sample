package com.example.sample.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.sample.R

class SettingsFragment : PreferenceFragmentCompat() {

    companion object {
        val TAG: String = SettingsFragment::class.java.simpleName
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}