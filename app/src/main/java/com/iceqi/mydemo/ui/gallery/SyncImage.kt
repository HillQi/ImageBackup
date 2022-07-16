package com.iceqi.mydemo.ui.gallery

import android.content.Context
import android.content.SharedPreferences
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import com.iceqi.mydemo.ui.common.FTPClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * To sync image with FTP server.
 *
 * Instance for this class should be one time only not reusable.
 */
class SyncImage {

    var ctx : Context? = null
    // TODO to support multi folders
    var localPath : String? = null
    var images : Array<String>? = null
    var scope : CoroutineScope? = null

    private val ftp : FTPClient = FTPClient()
    private var cancelled = false
    private var popup : PopupWindow? = null
    // TODO read last uploaded image data
    private var lastModifyTime : Long = 0
    private var tv : TextView? = null
    private lateinit var editor: SharedPreferences.Editor


    fun initView(){
        // TODO setup view details
        popup = PopupWindow(ctx)

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val layout = LinearLayout(ctx)
        layout.orientation = LinearLayout.VERTICAL

        tv = TextView(ctx)
        tv?.text = "qwert"
        layout.addView(tv, params)

        val btn = Button(ctx)
        btn.text = "cancel"
        btn.setOnClickListener { onCancel() }
        layout.addView(btn, params)

        popup?.contentView = layout
    }

    /**
     * Start sync
     */
    fun start(){
        val layout = LinearLayout(ctx)
        popup?.showAtLocation(layout, Gravity.CENTER, 10, 10)
        popup?.update(50, 50, 500, 200)

        scope?.launch {
            startUpload()
            clear()
            tv?.post(Runnable { popup?.dismiss() })
        }
    }

    private fun clear(){
        ftp.disconnect()
    }

    private fun output(msg : String){
        println("mine:: $msg")
    }
    private fun startUpload(){
        // TODO load ftp cfg
        ftp.ip = "192.1.1.193"
        ftp.port = 21
        ftp.username = "ice"
        ftp.password = "n"

        // TODO handle error msg
        var succ = true
        ftp.login { errMsg ->
            Toast.makeText(ctx, errMsg, Toast.LENGTH_SHORT)
            succ = false
        }

        if(!succ)
            return

        openDataStore()
        var files : Array<File>? = null
        when {
            localPath != null -> {
                var root = File(localPath)
                files = root.listFiles { f -> f.isFile && f.lastModified() > lastModifyTime }
                files?.sortBy { it.lastModified() }
            }
            images != null -> {
                files = arrayOfNulls<File>(images!!.size) as Array<File>?
                for(i in images!!.indices)
                    files?.set(i, File(images!![i]))

                files?.sortBy { it?.lastModified() }
            }
            else -> {
                clear()
                return
            }
        }


        uploadImages(files!!)
    }

    /**
     * Update last succeed sync image time.
     */
    private fun updateLastSyncTime(file: File): Unit {
        lastModifyTime = file.lastModified()

        editor.putLong("lastUploadedImageModifiedTime", lastModifyTime)
        editor.commit()
    }

    private fun openDataStore(){
        val setting = ctx?.getSharedPreferences("setting", Context.MODE_PRIVATE)
        editor = setting!!.edit()

        lastModifyTime = setting.getLong("lastUploadedImageModifiedTime", 0)
    }

    /**
     * upload images.
     */
    private fun uploadImages(files: Array<File>) {
        for((i, f) in files.withIndex()) {
            if(cancelled) return
            val s = f.inputStream()
            ftp.upload(f.name, s) { msg ->
                Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                SyncImage@this.cancelled = true
            }
            if(cancelled) return
            try {
                s.close()
            }catch (e : Exception){

            }
            updateUI(i, files.size)
            updateLastSyncTime(f)
        }
    }

    /**
     * Update UI
     */
    private fun updateUI(finished: Int, total: Int): Unit {
        tv?.post(Runnable { tv?.text = (finished * 100 / total).toString() })
    }

    /**
     * Cancel upload
     */
    private fun onCancel(): Unit {
        this.cancelled = true
    }

}