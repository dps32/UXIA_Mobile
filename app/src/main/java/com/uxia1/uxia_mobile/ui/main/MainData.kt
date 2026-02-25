package com.uxia1.uxia_mobile.ui.main

import com.uxia1.uxia_mobile.data.model.User

class MainData {
    companion object{
        private var token :String = ""

        fun isToken(): Boolean{
            return token != ""
        }
        fun getToken(): String{

            return token
        }
        fun setToken(t:String){
            token=t

        }
    }
}