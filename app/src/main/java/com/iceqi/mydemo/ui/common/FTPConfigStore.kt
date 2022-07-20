package com.iceqi.mydemo.ui.common

import android.content.Context
import android.content.SharedPreferences

data class FTPInfo(val ip : String, val port : Int, val user : String, val password : String)

class FTPConfigStore{
    lateinit var ctx : Context
    private lateinit var editor: SharedPreferences.Editor

    fun open() : FTPInfo?{
        val setting = ctx.getSharedPreferences("ftpinfo", Context.MODE_PRIVATE)
        editor = setting!!.edit()
        return if(setting.getString("ip", "-1")?.compareTo("-1")  == 0 )
            null
        else
            FTPInfo(
                setting.getString("ip", "")!!,
                setting.getInt("port", 0),
                setting.getString("user", "")!!,
                setting.getString("password", "")!!
            )
    }

    fun save(data : FTPInfo){
        editor.putString("ip", data.ip)
        editor.putInt("port", data.port)
        editor.putString("user", data.user)
        editor.putString("password", data.password)
        editor.commit()
    }
}