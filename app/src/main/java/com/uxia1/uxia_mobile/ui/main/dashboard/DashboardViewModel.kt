package com.uxia1.uxia_mobile.ui.main.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.uxia1.uxia_mobile.data.model.History
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.collections.mutableListOf

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    private val _dataset = MutableStateFlow<List<History>>(emptyList())

    val dataset : StateFlow<List<History>> = _dataset.asStateFlow()

    fun addHistory(history : History){
        _dataset.value = _dataset.value+history
    }



}