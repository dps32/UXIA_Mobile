package com.uxia1.uxia_mobile.services

import android.util.Log
import com.uxia1.uxia_mobile.data.model.User
import com.uxia1.uxia_mobile.ui.main.MainData
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class HttpClientService {

    companion object {
        fun sendImage(base64Image: String): String {
            val url = URL("https://uxia1.ieti.site/api/analitzar-imatge")
            val jsonBody = "{" +
                    "\"stream\": false," +
                    "\"prompt\": \"Descriu aquesta imatge\"," +
                    "\"model\" : \"qwen2.5vl:7b\"," +
                    "\"images\": [\"$base64Image\"] " +
                    "}"
            val connection = getConnection(url,jsonBody,"POST")
//            connection.setRequestProperty("Content-Type", "application/json")
//            connection.setRequestProperty("Accept", "application/json")


            return sendHttps(connection)
        }

        fun register(user : User): String{
            val url = URL("https://uxia1.ieti.site/api/usuaris/registrar")
            val jsonBody = "{\"nickname\": \"${user.nom}\","+
                    "\"email\": \"${user.email}\","+
//                    "\"password\": \"${user.pass}\","+
                    "\"telefon\": \"${user.telefon}\"}"

            val connection = getConnection(url,jsonBody,"POST")
            return sendHttps(connection)
        }

        fun login(user:User): String{
            val url = URL("https://uxia1.ieti.site/api/usuaris/login")
            val jsonBody = "{\"email\": \"${user.email}\","+
                    "\"password\": \"${user.pass}\"}"

            val connection = getConnection(url,jsonBody,"POST")


            return sendHttps(connection)
        }

        fun validar(telefon: String,codi_validacio : String): String{
            val url = URL("https://uxia1.ieti.site/api/usuaris/validar")
            val jsonBody = "{\"telefon\": \"${telefon}\","+
                    "\"codi_validacio\": \"${codi_validacio}\"}"

            val connection = getConnection(url,jsonBody,"POST")


            return sendHttps(connection)
        }





        private fun getConnection(url : URL, jsonBody : String, method : String): HttpURLConnection{
            //val url = URL("https://uxia1.ieti.site/api/admin/usuaris/login")
            val connection = url.openConnection() as HttpURLConnection

//            connection.requestMethod = method

            if(MainData.Companion.isToken()){
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer ${MainData.Companion.getToken()}"
                )
            }


            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")

            connection.doOutput = true
            connection.doInput = true
            connection.outputStream.use { os ->
                val writer = OutputStreamWriter(os, "UTF-8")
                writer.write(jsonBody)
                writer.flush()
                writer.close()
            }
            return connection

        }
        private fun sendHttps(connection : HttpURLConnection): String {
            var inputStream: InputStream? = null
            try {
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.inputStream
                    return inputStream.bufferedReader().use { it.readText() }
                } else {
                    val errorText =
                        connection.errorStream?.bufferedReader()?.use { it.readText() }
                            ?: "Unknown Error"
                    return "Error $responseCode: $errorText"
                }
            } catch (e: Exception) {
                return "Error de conexión: ${e.message}"
            } finally {
                inputStream?.close()
            }
        }
    }
}