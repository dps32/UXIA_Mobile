package com.uxia1.uxia_mobile.services

import android.util.Log
import com.uxia1.uxia_mobile.data.model.User
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
            val connection = getConnection(url,jsonBody)
//            connection.setRequestProperty("Content-Type", "application/json")
//            connection.setRequestProperty("Accept", "application/json")


            return sendHttps(connection)
        }

        fun addUser(user : User): String{
            val url = URL("https://uxia1.ieti.site/api/admin/usuaris/addUser")
            val jsonBody = "{\"username\": \"${user.nom}\","+
                    "\"email\": \"${user.email}\","+
                    "\"password\": \"${user.pass}\","+
                    "\"phone\": \"${user.telefon}\"}"

            val connection = getConnection(url,jsonBody)


            return sendHttps(connection)
        }

        fun login(user:User): String{
            val url = URL("https://uxia1.ieti.site/api/admin/usuaris/login")
            val jsonBody = "{\"email\": \"${user.email}\","+
                    "\"password\": \"${user.pass}\"}"

            val connection = getConnection(url,jsonBody)


            return sendHttps(connection)
        }





        private fun getConnection(url : URL, jsonBody : String): HttpURLConnection{
            //val url = URL("https://uxia1.ieti.site/api/admin/usuaris/login")
            val connection = url.openConnection() as HttpURLConnection

            connection.requestMethod = "POST"
            connection.setRequestProperty(
                "Authorization",
                "Bearer "
            )

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