package com.uxia1.uxia_mobile.data.model

data class User(
    val email : String,
    val pass : String,
    val nom : String = "",
    val telefon : String = "",
    val token : String? = null,
    val verified : Boolean = false
)
