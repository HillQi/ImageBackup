package com.iceqi.mydemo.ui.common

import android.content.Context
import android.content.SharedPreferences

class UPLoadTaskConfigStore{
    lateinit var ctx : Context
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var setting : SharedPreferences

    private val keyPaths = "uploadFolders"

    fun open() : List<String>? {
        setting = ctx.getSharedPreferences("uploadTaskInfo", Context.MODE_PRIVATE)
        editor = setting!!.edit()

        return getPaths()
    }

    fun getPaths() : List<String>?{
        val v = setting.getString(keyPaths, "")!!
        return if(v.compareTo("") == 0)
            null
        else
            v.split(";;")
    }

    fun addPath(path : String){
        if(path == null || path.isEmpty())
            return
        val p = StringBuffer(setting.getString(keyPaths, ""))
        if(p.isNotEmpty())
            p.append(";;")
        p.append(path)
        store(p.toString())
    }

    private fun store(s : String){
        editor.putString(keyPaths, s)
        editor.commit()
    }

    fun removePath(path : String) : Boolean{
        if(path == null || path.isEmpty())
            return false

        val p = setting.getString(keyPaths, "")!!
        var buf = StringBuffer(p)
        var i = buf.indexOf(path)
        if(i == -1)
            return false

        var end = i+path.length
        if(end < buf.length)
            end += 2
        else if(i > 0)
            i -= 2

        var s = buf.removeRange(i, end)
//        if(s.startsWith(";;"))
//            s = s.subSequence(2, s.length)
//        if(s.endsWith(";;"))
//            s = s.subSequence(0, s.length-2)
//        s = s.toString().replace(";;;;", ";;")
        store(s.toString())
        return true
    }
}