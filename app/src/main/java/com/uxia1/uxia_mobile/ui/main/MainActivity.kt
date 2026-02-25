package com.uxia1.uxia_mobile.ui.main

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.core.tts.TTS
import com.uxia1.uxia_mobile.data.model.History
import com.uxia1.uxia_mobile.databinding.ActivityMainBinding
import com.uxia1.uxia_mobile.ui.auth.RegisterActivity
import com.uxia1.uxia_mobile.ui.common.ShareViewModel
import com.uxia1.uxia_mobile.ui.dialog.BLEconnDialog
import com.uxia1.uxia_mobile.utils.XmlUtils
import org.json.JSONObject
import java.io.File

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

        TTS.setTtsContext(applicationContext)

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

    fun registerHistory(response:String){
        val jsonObject = JSONObject(response)

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

    override fun onReceivedResponse(img: String, response : String) {
        runOnUiThread {
            try{
                Log.d("TEST_IMG","msg: $response")
                val jsonObject = JSONObject(response)
                Log.d("TEST_IMG_POST","msg: $response")
                if (jsonObject.getString("status") == "OK") {
                    val data = jsonObject.getJSONObject("data")
                    val descriptor = data.getString("description")
                    val tagArray = data.getJSONArray("tags")

                    val list = mutableListOf<String>()
                    for (i in 0 until tagArray.length()) {
                        list.add(tagArray.getString(i))
                    }

                    viewModel.addHistory(History(img, list, descriptor))
                }

            }catch (e: Exception){
                Log.d("TEST_IMG_POST","msg: $response")
                val msg = response.substringBefore(": ")
                Log.d("REGISTER_handleJson",msg)
                if(msg=="Error 401"){
                    Toast.makeText(this@MainActivity,"Sesion expirada", Toast.LENGTH_SHORT).show()
                    XmlUtils.eliminarXML("user",this@MainActivity)
                    MainData.Companion.setToken("")
                    val intent = Intent(this@MainActivity, RegisterActivity::class.java)
                    startActivity(intent)
                }
            }

        }
    }

    // Aquesta callback és de l'Activity
    override fun onDestroy() {
        super.onDestroy()
        bleDialog?.dismiss()
    }
}