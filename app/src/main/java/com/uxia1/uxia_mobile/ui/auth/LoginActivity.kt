package com.uxia1.uxia_mobile.ui.auth

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.wifi.rtt.PasnConfig
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.core.tts.TTS
import com.uxia1.uxia_mobile.data.model.User
import com.uxia1.uxia_mobile.services.HttpClientService
import com.uxia1.uxia_mobile.ui.main.MainActivity
import com.uxia1.uxia_mobile.ui.main.MainData
import com.uxia1.uxia_mobile.utils.XmlUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    lateinit var txtEmail : EditText
    lateinit var txtPass : EditText
    lateinit var txtInfo : TextView
    lateinit var txtRegister : TextView
    lateinit var btnLogin : Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        XmlUtils.cagarToken(this)
        if(MainData.Companion.isToken()){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        txtRegister = findViewById(R.id.tvRegister)

        txtEmail = findViewById(R.id.txtLoginEmail)
        txtPass = findViewById(R.id.txtPassword)

        txtInfo = findViewById(R.id.txtLoginInfo)

        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener{
//            CoroutineScope(Dispatchers.IO).launch {
//                try {
//                    MainData.Companion.setToken("adajdnkansdn1dnjnaskndajsdnasdandj")
//                    val response = HttpClientService.Companion.sendImage("")
//                    Log.d("TEST_TOKEN","msg: $response")
//                    val msg = response.substringBefore(": ")
//                    Log.d("REGISTER_handleJson",msg)
//                    if(msg=="Error 401"){
//                        Log.d("TEST_TOKEN","Al login pa")
//                        withContext(Dispatchers.Main){
//                            Toast.makeText(this@LoginActivity,"Login expirado", Toast.LENGTH_SHORT).show()
//
//                            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
//                            startActivity(intent)
//                        }
//
//                    }
//                } catch (e: Exception) {
//                    Log.e("HTTP Error", "Error al enviar: ${e.message}")
//                }
//            }

            uItextWarning()

            if(checkCampos()){
                return@setOnClickListener
            }

            val email = txtEmail.text.toString()
            val pass = txtPass.text.toString()
            val user = User(email,pass)
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = HttpClientService.Companion.login(user)
                    withContext(Dispatchers.Main) {
                        handleResponse(response)
                    }
                } catch (e: Exception) {
                    Log.e("HTTP Error", "Error al enviar: ${e.message}")
                }
            }
        }

        txtRegister.setOnClickListener {
             val intent = Intent(this, RegisterActivity::class.java)
             startActivity(intent)
        }




    }
    private fun handleResponse(response : String) {
//        {"status": "OK", "message": "Usuari autenticat correctament", "data": {"token": "D23qswfSgR6VM9cuTuN"}}
        val jsonObject = JSONObject(response)

        if (jsonObject.getString("status") == "OK") {
            val data = jsonObject.getJSONObject("data")

            val token = data.getString("token")

            XmlUtils.guardarToken(token,this)
            MainData.Companion.setToken(token)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)





        }else{
            txtInfo.setTextColor(Color.RED)
            txtInfo.text = "Datos incorrectos o usuario no existe"
        }
    }


    private fun uItextWarning() {
        if (txtEmail.text.isEmpty() || txtEmail.text.isBlank()) {
            txtEmail.setBackgroundResource(R.drawable.bg_edittext_error)
        }else {
            txtEmail.setBackgroundResource(R.drawable.bg_edittext_normal)
        }
        if (txtPass.text.isEmpty() || txtPass.text.isBlank()) {
            txtPass.setBackgroundResource(R.drawable.bg_edittext_error)
        }else{
            txtPass.setBackgroundResource(R.drawable.bg_edittext_normal)
        }
    }


    private fun checkCampos(): Boolean {

        if (txtEmail.text.isEmpty() || txtEmail.text.isBlank()) return true
        if (txtPass.text.isEmpty() || txtPass.text.isBlank()) return true

        return false
    }

}