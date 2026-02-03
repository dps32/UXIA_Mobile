package com.uxia1.uxia_mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ShareViewModel : ViewModel(){
    private val _device = MutableLiveData<Device>()
    val device: LiveData<Device> get() = _device
    val imgFile = MutableLiveData<File>()

    fun updateDevice(newDevice: Device){
        _device.value = newDevice
    }

    fun updateFile(newFile: File){
        imgFile.value=newFile
    }
}