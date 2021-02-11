package com.example.sample

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.sample.ui.OsmdroidFragment


class MainActivity : AppCompatActivity() {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }


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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected")
        return super.onOptionsItemSelected(item)
    }
}