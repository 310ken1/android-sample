package com.example.sample.ui

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.sample.R

class OsmdroidFragmentDirections private constructor() {
    companion object {
        fun actionOsmdroidFragmentToBlankFragment(): NavDirections =
            ActionOnlyNavDirections(R.id.action_osmdroidFragment_to_blankFragment)
    }
}
