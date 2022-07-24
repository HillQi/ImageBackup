package com.iceqi.mydemo.ui.common

import android.content.Context
import android.content.SharedPreferences

class UPLoadTaskConfigStore{
    lateinit var ctx : Context
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var setting : SharedPreferences

    private val keyPaths = "uploadFolders"

    /**
     * open data store and return a folder list those have been added.
     */
    fun open() : List<String>? {
        setting = ctx.getSharedPreferences("uploadTaskInfo", Context.MODE_PRIVATE)
        editor = setting!!.edit()

        return getPaths()
    }

    /**
     * Get folder list those have been added.
     */
    fun getPaths() : List<String>?{
        val v = setting.getString(keyPaths, "")!!
        return if(v.compareTo("") == 0)
            null
        else
            v.split(";;")
    }

    /**
     * add a folder path
     */
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

    /**
     * remove a folder path
     */
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
        store(s.toString())
        return true
    }
}