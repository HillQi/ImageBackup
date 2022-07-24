package com.iceqi.mydemo.ui.common

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.ImageView
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AsyncImageLoader {
    private val view2Task = ConcurrentHashMap<ImageView, Job>();
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    lateinit var cr : ContentResolver

    /**
     * stop loader and cleanup
     */
    fun stop(){
        view2Task.clear()
        scope.cancel()
    }

    /**
     * add a load task
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadImage(view : ImageView, path : String, isThumbnail : Boolean, id : Long = -1){
        view2Task[view]?.cancel()

        val j = scope.launch() out@{
            try {
                if (!isActive)
                    return@out
                var b: Bitmap? = null
                if(isThumbnail)
                    b = if(id == -1L) {
                        ThumbnailUtils.createImageThumbnail(File(path), Size(100, 100), null)
                    }else{
                        MediaStore.Images.Thumbnails.getThumbnail(
                            cr,
                            id,
                            MediaStore.Images.Thumbnails.MICRO_KIND, null)
                    }

                else
                    b = BitmapFactory.decodeFile(path)

                launch(Dispatchers.Main) {
                    if (isActive){
                        view.setImageBitmap(b)
                        view2Task.remove(view)
                    }
                }
            }catch (t : Throwable){
                Log.e(this.toString(), "Error while loading image: ${path}", t)
            }
        }
        view2Task[view] = j
    }
}