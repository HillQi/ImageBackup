package com.iceqi.mydemo.ui.gallery

import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.iceqi.mydemo.databinding.ImageDisplayBinding
import com.iceqi.mydemo.ui.common.AsyncImageLoader
import com.iceqi.mydemo.ui.home.EXTRA_MSG_IMAGE_PATH
import java.lang.RuntimeException

class ImageDisplay : AppCompatActivity() {
    private lateinit var binding : ImageDisplayBinding
    private var imgPath : String? = null
    private val imageLoader = AsyncImageLoader()


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imgPath = intent.getStringExtra(EXTRA_MSG_IMAGE_PATH)
        if(imgPath == null)
            throw RuntimeException("image path not set")

        binding = ImageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadImage(imgPath!!)
    }

    // load image on to view
    @RequiresApi(Build.VERSION_CODES.Q)
    fun loadImage(imgPath : String){
        imageLoader.loadImage(binding.imageDisplay, imgPath, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}