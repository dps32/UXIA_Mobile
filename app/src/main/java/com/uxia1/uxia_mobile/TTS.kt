package com.uxia1.uxia_mobile

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.util.Log
import org.json.JSONObject
import java.util.Locale


object TTS {
    private var tts : TextToSpeech? = null


    fun setTtsContext(context: Context){

        tts = TextToSpeech(context, object : OnInitListener {
            override fun onInit(status: Int) {

//                if (status != TextToSpeech.ERROR) {
//                }
                val result = tts?.setLanguage(Locale("ca","ES"))

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED) {

                    Log.e("TTS", "Català no suportat en aquest dispositiu")
                }
            }
        })
    }

    fun speak(textToSpeak : String){
        if(tts==null) return
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null)

    }

    fun speakResponse(jsonToSpeak: String){
        val jsonObject = JSONObject(jsonToSpeak)
        if(jsonObject.getString("status")=="OK"){
            val data = jsonObject.getJSONObject("data")
            val descriptor = data.getString("description")
            val tagArray = data.getJSONArray("tags")


            val list = mutableListOf<String>()
            for (i in 0 until tagArray.length()) {
                list.add(tagArray.getString(i))
            }
            speak("descripcio: $descriptor tags: $list")

        }
    }

    fun shutdown(){
        if(tts==null) return
        tts?.stop()
        tts?.shutdown()
    }



}