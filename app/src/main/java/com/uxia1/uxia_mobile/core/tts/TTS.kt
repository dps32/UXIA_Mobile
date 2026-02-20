package com.uxia1.uxia_mobile.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale

object TTS {

    private val _isSpeaking = MutableSharedFlow<Boolean>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val isSpeaking = _isSpeaking.asSharedFlow()


    private var tts : TextToSpeech? = null


    fun setTtsContext(context: Context){

        tts = TextToSpeech(context, object : TextToSpeech.OnInitListener {

            override fun onInit(status: Int) {

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.tryEmit(false)
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.tryEmit(false)
                    }

                    override fun onStart(utteranceId: String?) {
                        Log.d("TTS_object", "intentando enviar")
                        _isSpeaking.tryEmit(true)
                    }

                })

//                if (status != TextToSpeech.ERROR) {
//                }
                val result = tts?.setLanguage(Locale("ca", "ES"))

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {

                    Log.e("TTS", "Català no suportat en aquest dispositiu")
                }
            }

        })




    }

    private fun sendSpeakingState(talking: Boolean) {

        CoroutineScope(Dispatchers.Main).launch {
            _isSpeaking.tryEmit(talking)
        }
    }

    fun speak(textToSpeak : String){
        if(tts==null) return
        val id = "frase_${System.currentTimeMillis()}"
        tts?.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null,id)

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