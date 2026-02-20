package com.uxia1.uxia_mobile.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.data.model.User
import com.uxia1.uxia_mobile.services.HttpClientService
import org.json.JSONObject

class SMSconnDialog (context: Context,
                     private val connectionCallback: SMSconnectionCallback,
                     private val user : User
) : Dialog(context) {

    interface SMSconnectionCallback {
        fun onConnectClicked(address: String)
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

//            val response = HttpClientService.Companion.checkPin(pin.text.toString(),user)

            val response = "OK"

            if(response=="OK"){
                connectionCallback.onConnectClicked("")
                dismiss()
            }else{
                txtStatus.text = "Pin invalido"
            }

        }


        btnCancel.setOnClickListener {
            dismiss()
        }


//        btnConnect.setOnClickListener {
//            val address = etAddress.text.toString()
//            if (address.isNotEmpty()) {
//                connectionCallback.onConnectClicked(address)
//                dismiss() // Cerrar el diálogo
//            } else {
//                etAddress.error = "Introduce una dirección"
//            }
//        }
    }
    private fun handleResponse(response : String) {
//        {"status": "OK", "message": "Usuari autenticat correctament", "data": {"token": "D23qswfSgR6VM9cuTuN"}}
        val jsonObject = JSONObject(response)
        if (jsonObject.getString("status") == "OK") {
            val data = jsonObject.getJSONObject("data")
            val token = data.getString("token")
        }else{
            txtInfo.setTextColor(Color.RED)
            txtInfo.text = "Datos incorrectos o usuario no existe"
        }
    }
}



