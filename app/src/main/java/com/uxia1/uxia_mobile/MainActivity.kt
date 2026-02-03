package com.uxia1.uxia_mobile

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.uxia1.uxia_mobile.databinding.ActivityMainBinding
import java.io.File
import androidx.fragment.app.activityViewModels

import com.uxia1.uxia_mobile.ShareViewModel
import kotlin.getValue


class MainActivity : AppCompatActivity(), BLEconnDialog.BLEConnectionCallback {
    private val viewModel: ShareViewModel by viewModels()

    lateinit var bleDialog: BLEconnDialog

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }



    // DIALOG : cridar aquesta funció per mostrar-lo
////////////////////////////////////////////////
    fun showBLEDialog(device: BluetoothDevice) {
        bleDialog = BLEconnDialog(this, device, this)
        bleDialog?.apply {
            setCancelable(false)
            setOnCancelListener {
                onConnectionCancelled()
            }
            show()
        }
    }

    // DIALOG CALLBACKS
///////////////////////////////
    override fun onConnectionSuccess(gatt: BluetoothGatt) {
        runOnUiThread {
            Toast.makeText(this, "Connectat amb èxit!", Toast.LENGTH_SHORT).show()
            // Aquí pots fer operacions amb el gatt connectat
            // Per exemple: llegir/escribre característiques
        }
    }

    override fun onConnectionFailed(error: String) {
        runOnUiThread {
            Toast.makeText(this, "Error de connexió: $error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onConnectionCancelled() {
        runOnUiThread {
            Toast.makeText(this, "Connexió cancel·lada", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReceivedImage(file: File) {
        runOnUiThread {
            viewModel.updateFile(file)
            val filename = file.name
            Toast.makeText(this, "Imatge rebuda: $filename", Toast.LENGTH_SHORT).show()
        }
    }

    // Aquesta callback és de l'Activity
    override fun onDestroy() {
        super.onDestroy()
        bleDialog?.dismiss()
    }
}