package com.iceqi.mydemo.ui.common

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.Executor


class ImageUploadService : Service(), TaskHandler{

    var cancelled = false

    private lateinit var imgTask : ImageUploadTask
    private var scope : CoroutineScope? = null

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

    //The system invokes this method by calling startService() when another component (such as an activity) requests that the service be started. When this method executes, the service is started and can run in the background indefinitely.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(scope == null)
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

            if(cancelled)
                notificationDialog("My Demo", "Image batch upload", "Batch upload finished with break.")
            else
                notificationDialog("My Demo", "Image batch upload", "Batch upload finished.")
            stopSelf()
        }


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onError(msg: String) {
        toastError(msg)
        cancelled = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onProgress(finishedImgPos: Int, totalImgCount: Int) {
        notificationDialog("My Demo", "Image batch upload", "Batch upload in progressing.")
    }

    override fun onFinished() {
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun notificationDialog(title : String, subtitle : String, message : String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "tutorialspoint_01"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "My Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setSound(null, null);
            notificationChannel.setShowBadge(false);
            // Configure the notification channel.
            notificationChannel.description = "Sample Channel description"
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_LIGHTS)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.sym_def_app_icon)
            .setTicker("Tutorialspoint") //.setPriority(Notification.PRIORITY_MAX)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText(subtitle)
        notificationManager.notify(1, notificationBuilder.build())
    }
}