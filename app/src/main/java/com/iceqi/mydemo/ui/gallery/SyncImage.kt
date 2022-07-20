package com.iceqi.mydemo.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.DisplayMetrics
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import com.iceqi.mydemo.databinding.SyncImagePopWindowsBinding
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

    lateinit var ctx : Context
    var images : Array<String>? = null
    var scope : CoroutineScope? = null
    lateinit var inflater : LayoutInflater
    lateinit var parent : View

    private val ftp : FTPClient = FTPClient()
    private var cancelled = false
    private lateinit var popup : PopupWindow
    private var lastModifyTime : Long = 0
    private lateinit var progress : ProgressBar
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: SyncImagePopWindowsBinding
//    private var localPath : String? = null

    fun initView(){
//          keep as demo
//        val layout = LinearLayout(ctx)
//        layout.orientation = LinearLayout.VERTICAL
//        layout.layoutParams = LinearLayout.LayoutParams(
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//
//        var params = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT
//        )
//        params.setMargins(50, 50, 50, 50)
//        progress = ProgressBar(ctx, null, 0, android.R.attr.progressBarStyleHorizontal)
//        progress.max = 100
//        progress.progress = 0
//        progress.isIndeterminate = false
//        progress.progressDrawable = ctx?.resources?.getDrawable(R.drawable.progressbar)
//        layout.addView(progress, params)
//
//        params = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            LinearLayout.LayoutParams.WRAP_CONTENT
//        )
//        val btn = Button(ctx)
//        btn.text = ctx?.resources?.getString(R.string.cancel)
//        btn.setOnClickListener { onCancel() }
//        layout.addView(btn, params)

//        binding = SyncImagePopWindowsBinding.inflate(inflater)
//        popup.contentView = binding.root
//        popup = PopupWindow(ctx)
//        popup.height = 600

//        popup = PopupWindow(binding.root,
//            WindowManager.LayoutParams.MATCH_PARENT,
//            WindowManager.LayoutParams.WRAP_CONTENT)

        binding = SyncImagePopWindowsBinding.inflate(inflater)
        popup = PopupWindow(ctx)
        popup.height = WindowManager.LayoutParams.MATCH_PARENT
        popup.width = WindowManager.LayoutParams.MATCH_PARENT
        popup.contentView = binding.root
        popup.isOutsideTouchable =false

        binding.btnCancel.setOnClickListener { onCancel() }
        progress = binding.progress
    }

    /**
     * Start sync
     */
    @SuppressLint("ResourceAsColor")
    fun start(){
        popup.showAtLocation(parent, Gravity.CENTER, 0, 0)

        scope?.launch {
            startUpload()
            clear()
            progress.post(Runnable { popup.dismiss() })
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

        var succ = true
        ftp.login { errMsg ->
            progress.post { Toast.makeText(ctx, errMsg, Toast.LENGTH_SHORT).show() }
            succ = false
        }

        if(!succ)
            return

        var files : Array<File>? = null
        when {
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
        val p = files?.get(0)?.parent!!
        openDataStore(p)
        files = files?.filter { f -> f.lastModified() > lastModifyTime }.toTypedArray()
        if(files.isEmpty())
            return

        ftp.makeDirectories(p)
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

    /**
     * param: file Specify the folder for which to open configuration
     */
    private fun openDataStore(folderPath : String){
        var path = folderPath.replace("/", "")
        path = "folderSetting:$path"
        val setting = ctx.getSharedPreferences(path, Context.MODE_PRIVATE)
        lastModifyTime = setting!!.getLong("lastUploadedImageModifiedTime", 0)
        editor = setting!!.edit()
    }

    /**
     * upload images.
     */
    private fun uploadImages(files: Array<File>) {
        for((i, f) in files.withIndex()) {
            if(cancelled) return
            val s = f.inputStream()
            ftp.upload(f.name, s) { msg ->
                progress.post { Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show() }
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
        progress?.post(Runnable { progress.progress = finished * 100 / total})
    }

    /**
     * Cancel upload
     */
    private fun onCancel(): Unit {
        this.cancelled = true
    }

}