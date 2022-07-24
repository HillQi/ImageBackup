package com.iceqi.mydemo.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.view.*
import android.widget.*
import com.iceqi.mydemo.R
import com.iceqi.mydemo.databinding.SyncImagePopWindowsBinding
import com.iceqi.mydemo.ui.common.FTPClient
import com.iceqi.mydemo.ui.common.FTPConfigStore
import com.iceqi.mydemo.ui.common.ImageUploadTask
import com.iceqi.mydemo.ui.common.TaskHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File


/**
 * To sync image with FTP server.
 *
 * Instance for this class should be one time only not reusable.
 */
class SyncImage : TaskHandler {

    lateinit var ctx : Context
    lateinit var images : Array<String>
    lateinit var scope : CoroutineScope
    lateinit var inflater : LayoutInflater
    lateinit var parent : View

    private val ftp : FTPClient = FTPClient()
    private var cancelled = false
    private lateinit var popup : PopupWindow
    private var lastModifyTime : Long = 0
    private lateinit var progress : ProgressBar
    private lateinit var editor: SharedPreferences.Editor
    private lateinit var binding: SyncImagePopWindowsBinding
    private lateinit var imgTask : ImageUploadTask
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

        binding.btnCancel.setOnClickListener { imgTask.cancel() }
        progress = binding.progress
    }

    /**
     * Start sync
     */
    @SuppressLint("ResourceAsColor")
    fun start(){
        popup.showAtLocation(parent, Gravity.CENTER, 0, 0)

        scope.launch {
            startUpload()
        }
    }

    private fun startUpload(){
        imgTask = ImageUploadTask()
        imgTask.ctx = ctx
        imgTask.taskHandler = this
        imgTask.setImages(images)
        imgTask.uploadImages()
    }

    override fun onError(msg: String) {
        progress.post{
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onProgress(finishedImgPos: Int, totalImgCount: Int) {
        progress.post { progress.progress = finishedImgPos * 100 / totalImgCount }
    }

    override fun onFinished() {
        progress.post{
            popup.dismiss()
        }
    }

}