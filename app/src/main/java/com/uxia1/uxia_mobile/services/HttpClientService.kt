package com.uxia1.uxia_mobile.services

import android.util.Log
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class HttpClientService {

    companion object {
        fun sendImage(base64Image: String): String {
            var inputStream: InputStream? = null
            try {
                val url = URL("https://uxia1.ieti.site/api/analitzar-imatge")
                //val url = URL("https://uxia1.ieti.site/api/admin/usuaris/login")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.doOutput = true
                connection.doInput = true

                val jsonBody = "{" +
                        "\"images\": [\"$base64Image\"]," +
                        "\"stream\": false," +
                        "prompt: \"\"" +
                        "}"

                /*val jsonBody = "{" +
                        "\"email\": [\"juan@example.com\"]," +
                        "\"password\": 123" +
                        "}"*/

                connection.outputStream.use { os ->
                    val writer = OutputStreamWriter(os, "UTF-8")
                    writer.write(jsonBody)
                    writer.flush()
                    writer.close()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = connection.inputStream
                    return inputStream.bufferedReader().use { it.readText() }
                } else {
                    val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown Error"
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