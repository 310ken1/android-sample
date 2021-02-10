package com.example.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        when {
            ContextCompat.checkSelfPermission(baseContext, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> {
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            }
            else -> {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                    if (isGranted) {

                    } else {
                        finish()
                    }
                }.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
}