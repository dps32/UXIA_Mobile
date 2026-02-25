package com.uxia1.uxia_mobile.utils

import android.content.Context
import android.util.Log
import android.util.Xml
import com.uxia1.uxia_mobile.data.model.Device
import com.uxia1.uxia_mobile.ui.main.MainData
import org.xmlpull.v1.XmlPullParser

object XmlUtils {
    fun guardarToken(token:String, context : Context){
        val xml = """
        <user>
            <token>${token}</token>
        </user>
        """.trimIndent()
        context.openFileOutput("user.xml", Context.MODE_PRIVATE).use {
            it.write(xml.toByteArray())
        }


    }

    fun eliminarXML(filename : String , context:Context) {

        val filename = "$filename.xml"
        val isDeleted = context.deleteFile(filename)

        if (isDeleted) {
            Log.d("FILE_IO", "El archivo $filename fue eliminado con éxito.")
        } else {
            Log.d("FILE_IO", "No se pudo eliminar el archivo o no existe.")
        }
    }

    fun generarDeviceXML(device : Device, context : Context){

        val xml = """
        
        <device>
            <name>${device.nom}</name>
            <address>${device.address}</address>
        </device>
        """.trimIndent()

        context.openFileOutput("setting.xml", Context.MODE_PRIVATE).use {
            it.write(xml.toByteArray())
        }
    }

    fun cagarToken(context: Context){
        val filename = "user.xml"
        try {
            context.openFileInput(filename).use { inputStream ->
                val parser: XmlPullParser = Xml.newPullParser()
                parser.setInput(inputStream, null)

                var eventType = parser.eventType
                var token: String? = null

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "token" -> token = parser.nextText()


                            }
                        }
                    }
                    eventType = parser.next()
                }

                if (token != null) {
                    MainData.Companion.setToken(token)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}