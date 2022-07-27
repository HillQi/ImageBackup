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
        var b = removeSection(buf, path)
        store(buf.toString())
        return true
    }

    fun removePath(paths : Array<String>) : Boolean{
        if(paths == null || paths.isEmpty())
            return false

        val p = setting.getString(keyPaths, "")!!
        var buf = StringBuffer(p)
        for(s in paths)
            removeSection(buf, s)
        store(buf.toString())
        return true


    }

    private fun removeSection(from : StringBuffer, section : String) : StringBuffer{

        var i = from.indexOf(section)
        if(i == -1)
            return from

        var end = i+section.length
        if(end < from.length)
            end += 2
        else if(i > 0)
            i -= 2

        return from.delete(i, end)
    }
}