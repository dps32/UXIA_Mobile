package com.uxia1.uxia_mobile.ui.auth

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.Visibility
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.data.model.User
import com.uxia1.uxia_mobile.services.HttpClientService
import com.uxia1.uxia_mobile.ui.dialog.SMSconnDialog
import com.uxia1.uxia_mobile.ui.main.MainActivity
import com.uxia1.uxia_mobile.ui.main.MainData
import com.uxia1.uxia_mobile.utils.XmlUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RegisterActivity : AppCompatActivity(), SMSconnDialog.SMSconnectionCallback {

    lateinit var smsDialog : SMSconnDialog
    lateinit var txtInfo : TextView
    lateinit var txtNom : EditText
    lateinit var txtTelefon : EditText
    lateinit var txtEmail : EditText
    lateinit var txtPass : EditText
    lateinit var txtLogin : TextView
    lateinit var btnRegistro : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        txtInfo = findViewById(R.id.txtRegisterInfo)
        txtLogin = findViewById(R.id.tvLogin)
        txtNom = findViewById(R.id.txtNom)
        txtTelefon = findViewById(R.id.txtTelefon)
        txtEmail = findViewById(R.id.txtEmail)
        txtPass = findViewById(R.id.txtPassword)
        btnRegistro = findViewById(R.id.btnRegister)

        btnRegistro.setOnClickListener {
            uItextWarning()
            if(checkCampos()) return@setOnClickListener
            val nom = txtNom.text.toString()
            val pass = txtPass.text.toString()
            val telf = txtTelefon.text.toString()
            var email = ""
            if(!txtTelefon.text.isEmpty() || !txtTelefon.text.isBlank()){
                email= txtEmail.text.toString()
            }
            val user = User(email,pass,nom,telf)


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response= HttpClientService.Companion.register(user)

                    withContext(Dispatchers.Main) {
                        handleResponse(response, user)
                    }

                } catch (e: Exception) {
                    Log.e("HTTP Error", "Error al enviar: ${e.message}")
                }
            }







        }

        txtLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }



    }

    fun handleResponse(response : String,user : User){
        Log.d("REGISTER_handleResponse","msg: $response")

        try{
            val jsonObject = JSONObject(response)
            Log.d("REGISTER_handleResponse","enviar Dialog")

            if (jsonObject.getString("status") == "OK") {

                Log.d("REGISTER_handleResponse","abriendo Dialog")
                val dialog = SMSconnDialog(this@RegisterActivity,this,user)
                dialog?.apply {
                    setCancelable(false)
                    show()
                }

            }
        }catch (e: Exception){
            val info = response.substringAfter(": ")
            Log.d("REGISTER_handleJson",info)
            val json = JSONObject(info)

            txtInfo.setTextColor(Color.RED)
            txtInfo.text = json.getString("message")
            txtInfo.visibility = View.VISIBLE
        }
    }
    fun checkCampos(): Boolean{

        if(txtNom.text.isEmpty() ||txtNom.text.isBlank()) return true
        if(txtEmail.text.isEmpty() ||txtEmail.text.isBlank()) return true
        if(txtPass.text.isEmpty() ||txtPass.text.isBlank()) return true
        if(txtTelefon.text.isEmpty() ||txtTelefon.text.isBlank()) return true
        if(txtTelefon.length()!=9) return true

        return false
    }
    private fun uItextWarning() {
        if (txtNom.text.isEmpty() || txtNom.text.isBlank()) {
            txtNom.setBackgroundResource(R.drawable.bg_edittext_error)
        }else {
            txtNom.setBackgroundResource(R.drawable.bg_edittext_normal)
        }
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

        if (txtTelefon.text.isEmpty() || txtTelefon.text.isBlank() || txtTelefon.length()!=9) {
            txtTelefon.setBackgroundResource(R.drawable.bg_edittext_error)
        }else{
            txtTelefon.setBackgroundResource(R.drawable.bg_edittext_normal)
        }

        if(txtTelefon.length()!=9&&!txtTelefon.text.isEmpty()) {
            txtInfo.setTextColor(Color.RED)
            txtInfo.text = "Introduïu un número de telèfon vàlid"
            txtInfo.visibility = View.VISIBLE
        }else{
            txtInfo.visibility= View.INVISIBLE
        }
    }

    override fun onConnect(token: String){

        runOnUiThread {
            Toast.makeText(this, "Usuario Verificado!", Toast.LENGTH_LONG).show()
        }
        XmlUtils.guardarToken(token,this)
        MainData.Companion.setToken(token)
        if(MainData.isToken()){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }
}