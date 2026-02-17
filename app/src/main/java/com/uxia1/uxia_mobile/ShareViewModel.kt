package com.uxia1.uxia_mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ShareViewModel : ViewModel(){
    private val _device = MutableLiveData<Device>()
    val device: LiveData<Device> get() = _device
    val imgFile = MutableLiveData<File>()

    val response = MutableLiveData<History>()

    fun updateDevice(newDevice: Device){
        _device.value = newDevice
    }

    fun updateFile(newFile: File){
        imgFile.value=newFile
//        var test = "{\n" +
//                "  \"status\": \"OK\",\n" +
//                "  \"message\": \"Imatges processades correctament\",\n" +
//                "  \"data\": {\n" +
//                "    \"description\": \"Català si suportat en aquest dispositiu\",\n" +
//                "    \"tags\": [\"Bon Dia\",\"hola\"],\n" +
//                "    \"processing_time\": \"2.3s\",\n" +
//                "    \"model_used\": \"qwen2.5vl:7b\"\n" +
//                "  }\n" +
//                "}\n"
//
//        TTS.speakResponse(test)
    }

    fun updateResponse(res: History){
        response.value=res
    }
}