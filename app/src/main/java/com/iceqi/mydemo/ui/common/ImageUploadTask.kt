package com.iceqi.mydemo.ui.common

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.iceqi.mydemo.R
import java.io.File

interface TaskHandler{
    fun onError(msg : String)
    fun onProgress(finishedImgPos : Int, totalImgCount : Int)
    fun onFinished()
}

class ImageUploadTask {

    lateinit var ctx : Context
    lateinit var taskHandler : TaskHandler
    private var images : Array<String>? = null
    private var folder : String? = null

    var cancelled = false

    private var lastModifyTime : Long = 0
    private lateinit var editor : SharedPreferences.Editor
    private lateinit var ftp : FTPClient

    fun setImages(images : Array<String>){
        this.images = images
    }

    fun setFolder(folder : String){
        this.folder = folder
    }

    /**
     * Update last succeed sync image time.
     */
    private fun updateLastSyncTime(file: File): Unit {
        lastModifyTime = file.lastModified()

        editor.putLong("lastUploadedImageModifiedTime", lastModifyTime)
        editor.commit()
    }

    /**
     * param: file Specify the folder for which to upload image
     */
    private fun setImageFolder(folderPath : String){
        var path = folderPath.replace("/", "")
        path = "folderSetting:$path"
        val setting = ctx.getSharedPreferences(path, Context.MODE_PRIVATE)
        lastModifyTime = setting!!.getLong("lastUploadedImageModifiedTime", 0)
        editor = setting!!.edit()
    }

    /**
     * upload images. All images must be in the same folder.
     */
    fun uploadImages() {
        var files : Array<File>? = null
        files = if(images != null){
            setImageFolder(File(images!![0]).parent!!)
            sortImages(images!!)
        }else{
            setImageFolder(folder!!)
            sortImages(File(folder).listFiles())
        }

        cancelled = files.isEmpty()

        if(!cancelled)
            cancelled = !init()
        if(!cancelled) {
            ftp.login { msg ->
                    cancelled = true
                    taskHandler.onError(ctx.resources.getString(R.string.ftp_login_failed))}
        }
        if(!cancelled) {
            try {
                ftp.makeDirectories(files[0].parent)

                for((i, f) in files.withIndex()) {
                    if(cancelled)
                        break
                    if(!f.isFile)
                        continue
                    val s = f.inputStream()
                    ftp.upload(f.name, s) { msg ->
                        taskHandler.onError(msg)
                        cancelled = true
                    }
                    try {
                        s.close()
                    }catch (e : Exception){

                    }
                    if(cancelled)
                        break
                    updateLastSyncTime(f)
                    taskHandler.onProgress(i, files.size)
                }
            }
            catch (t : Throwable)
            {
            }

            ftp.disconnect()
        }
        taskHandler.onFinished()
    }

    private fun sortImages(images : Array<File>) : Array<File> {
        var l = images.filter { it.isFile && it.lastModified() > lastModifyTime }
        l = l.sortedBy { it.lastModified() }
        return l.toTypedArray()
    }

    private fun sortImages(images : Array<String>) : Array<File>{
        val files = arrayOfNulls<File>(images.size) as Array<File>
        for(i in images.indices)
            files[i] = File(images[i])
        return sortImages(files)
    }

    /**
     * Cancel upload
     */
    fun cancel(): Unit {
        this.cancelled = true
    }

    private fun init() : Boolean{
        val cfgStore = FTPConfigStore()
        cfgStore.ctx = ctx
        var cfg = cfgStore.open()
        if(cfg == null) {
            taskHandler.onError(ctx.resources.getString(R.string.please_setup_ftp_server))
            return false
        }

        ftp = FTPClient()
        ftp.ip = cfg.ip
        ftp.port = cfg.port
        ftp.username = cfg.user
        ftp.password = cfg.password

        return true
    }
}