package com.uxia1.uxia_mobile.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.data.model.User
import com.uxia1.uxia_mobile.services.HttpClientService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class SMSconnDialog (context: Context,
                     private val connectionCallback: SMSconnectionCallback,
                     private val user : User
) : Dialog(context) {

    interface SMSconnectionCallback {
        fun onConnect(token: String)
    }

    lateinit var txtInfo : TextView
    lateinit var txtStatus : TextView
    lateinit var pin : EditText

    lateinit var btnCheck : Button
    lateinit var btnCancel : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_sms_conn)

        txtStatus = findViewById(R.id.txtStatus)
        txtInfo = findViewById(R.id.txtSmsInfo)
        pin = findViewById(R.id.txtPin)
        btnCheck = findViewById(R.id.btnCheck)
        btnCancel = findViewById(R.id.btnCancel)


        btnCheck.setOnClickListener {
            if(pin.text.isEmpty()||pin.text.isBlank()) return@setOnClickListener

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("REVISAR","${user.telefon},${pin.text}")
                    val response = HttpClientService.Companion.validar(user.telefon,pin.text.toString())

                    withContext(Dispatchers.Main) {
                        Log.d("REVISAR","msg: $response")
                        handleResponse(response)
                    }
                } catch (e: Exception) {
                    Log.e("HTTP Error", "Error al enviar: ${e.message}")
                }
            }






        }


        btnCancel.setOnClickListener {
            dismiss()
        }



    }
    private fun handleResponse(response : String) {
        try{
            val jsonObject = JSONObject(response)
            if (jsonObject.getString("status") == "OK") {
                val data = jsonObject.getJSONObject("data")
                val token = data.getString("api_key")

                connectionCallback.onConnect(token)
                dismiss()
            }
        }catch(e:Exception){
            txtStatus.setTextColor(Color.RED)
            txtStatus.text = "Pin invalido"
            txtInfo.text = "Intente nuevamente"
        }
    }
}



