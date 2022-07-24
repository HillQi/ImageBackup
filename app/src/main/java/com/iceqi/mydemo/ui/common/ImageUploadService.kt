package com.iceqi.mydemo.ui.common

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.Executor


class ImageUploadService : Service(), TaskHandler{

    var cancelled = false

    private lateinit var imgTask : ImageUploadTask
    private var scope : CoroutineScope? = null
    private lateinit var job : Job

    /**
     * Cancel upload
     */
    private fun onCancel(): Unit {
        this.cancelled = true
    }

    private fun toastError(msg : String){
        val mainExecutor: Executor = ContextCompat.getMainExecutor(this)
        mainExecutor.execute(Runnable {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    //The system invokes this method by calling startService() when another component (such as an activity) requests that the service be started. When this method executes, the service is started and can run in the background indefinitely.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(scope == null) {
            scope = CoroutineScope(Job() + Dispatchers.IO)
            scope?.launch {

                val taskCfg = UPLoadTaskConfigStore()
                taskCfg.ctx = applicationContext
                val folders = taskCfg.open()
                if (folders != null)
                    for (f in folders) {
                        imgTask = ImageUploadTask()
                        imgTask.ctx = applicationContext
                        imgTask.taskHandler = this@ImageUploadService
                        imgTask.setFolder(f)
                        if (cancelled)
                            break
                        imgTask.uploadImages()
                    }
                stopSelf()
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onError(msg: String) {
        toastError(msg)
        cancelled = true
    }

    override fun onProgress(finishedImgPos: Int, totalImgCount: Int) {
        // TODO notify UI
    }

    override fun onFinished() {
    }
}