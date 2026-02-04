package com.uxia1.uxia_mobile.services

import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpClientService {

    companion object {
        fun sendImage(base64Image : String) {
            var inputStream: InputStream? = null
            try {
                val url = URL("https://uxia1.ieti.site/api/analitzar-imatge")
                val connection = url.openConnection() as HttpURLConnection

                connection.doOutput = true
                connection.setChunkedStreamingMode(0);
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.connect()
                inputStream = connection.inputStream
                return inputStream.bufferedReader().use{
                    it.readText()
                }
            }finally {

                if(inputStream != null){
                    inputStream.close()
                }

            }
        }
    }
}