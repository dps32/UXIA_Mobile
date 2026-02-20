package com.uxia1.uxia_mobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.uxia1.uxia_mobile.R
import com.uxia1.uxia_mobile.data.model.User
import com.uxia1.uxia_mobile.services.HttpClientService
import com.uxia1.uxia_mobile.ui.dialog.SMSconnDialog
import com.uxia1.uxia_mobile.ui.main.MainActivity

class RegisterActivity : AppCompatActivity(), SMSconnDialog.SMSconnectionCallback {

    lateinit var smsDialog : SMSconnDialog

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
        txtLogin = findViewById(R.id.tvLogin)
        txtNom = findViewById(R.id.txtNom)
        txtTelefon = findViewById(R.id.txtTelefon)
        txtEmail = findViewById(R.id.txtEmail)
        txtPass = findViewById(R.id.txtPassword)
        btnRegistro = findViewById(R.id.btnRegister)

        btnRegistro.setOnClickListener {
            if(checkCampos()) return@setOnClickListener
            val nom = txtNom.text.toString()
            val pass = txtPass.text.toString()
            val telf = txtTelefon.text.toString()
            var email = ""
            if(!txtTelefon.text.isEmpty() || !txtTelefon.text.isBlank()){
                email= txtEmail.text.toString()
            }
            val user = User(email,pass,nom,telf)

//            val response= HttpClientService.Companion.addUser(user)

            val dialog = SMSconnDialog(this,this,user)
            dialog?.apply {
                setCancelable(false)
                show()
            }

        }

        txtLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }



    }

    fun checkCampos(): Boolean{

        if(txtNom.text.isEmpty() ||txtNom.text.isBlank()) return true
        if(txtEmail.text.isEmpty() ||txtEmail.text.isBlank()) return true
        if(txtPass.text.isEmpty() ||txtPass.text.isBlank()) return true
        if(txtTelefon.text.isEmpty() ||txtTelefon.text.isBlank()) return true

        return false
    }

    override fun onConnectClicked(address: String){

        runOnUiThread {
            Toast.makeText(this, "Usuario Verificado!", Toast.LENGTH_LONG).show()
        }
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}