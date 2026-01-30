package com.uxia1.uxia_mobile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uxia1.uxia_mobile.ui.home.Device

class DeviceViewModel : ViewModel(){
    private val _device = MutableLiveData<Device>()
    val device: LiveData<Device> get() = _device

    fun updateDevice(newDevice: Device){
        _device.value = newDevice
    }
}