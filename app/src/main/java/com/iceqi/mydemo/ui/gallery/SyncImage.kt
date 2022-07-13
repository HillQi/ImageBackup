package com.iceqi.mydemo.ui.gallery

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Button
import com.iceqi.mydemo.ui.common.FTPClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Thread.sleep

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
    private var tmpLastModifyTime : Long = 0
    private var tv : TextView? = null


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
            output("login err: $errMsg")
            succ = false
        }

        if(!succ)
            return

        var files : Array<File>? = null
        when {
            localPath != null -> {
                var root = File(localPath)
                files = root.listFiles { f -> f.isFile && f.lastModified() > tmpLastModifyTime }
                files?.sortBy { it.lastModified() }
            }
            images != null -> {
                files = arrayOfNulls<File>(images!!.size) as Array<File>?
                for(i in images!!.indices){
                    val f = File(images!![i])
                    if(f.isFile && f.lastModified() > tmpLastModifyTime)
                        files?.set(i, f)
                }

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
        // TODO
        tmpLastModifyTime = file.lastModified()
    }

    /**
     * upload images.
     */
    private fun uploadImages(files: Array<File>) {
        // TODO
        // handle each upload error
        for((i, f) in files.withIndex()) {
            // TODO test only
            if(cancelled) return
            val s = f.inputStream()
            ftp.upload(f.name, s) { msg -> output("upload err: $msg") }
            try {
                s.close()
            }catch (e : Exception){

            }
//            sleep(200)
            updateUI(i, files.size)
            updateLastSyncTime(f)
        }
    }

    /**
     * Update UI
     */
    private fun updateUI(finished: Int, total: Int): Unit {
        // TODO
        tv?.post(Runnable { tv?.text = (finished * 100 / total).toString() })
    }

    /**
     * Cancel upload
     */
    private fun onCancel(): Unit {
        // TODO
        this.cancelled = true
    }

}