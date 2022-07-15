package com.iceqi.mydemo.ui.gallery

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.iceqi.mydemo.databinding.ImagePageDisplayBinding
import com.iceqi.mydemo.ui.common.java.ZoomImageView

class ImagePageDisplay : AppCompatActivity() {

    private val adapter = ImagePageAdapter()
    private lateinit var imgs : Array<String>
    private lateinit var binding : ImagePageDisplayBinding

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imgs = intent.getStringArrayExtra(EXTRA_MSG_IMAGE_PATH) as Array<String>
        if(imgs == null)
            throw RuntimeException("image path not set")
        adapter.imgs = imgs
        adapter.ctx = this

        binding = ImagePageDisplayBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.imageViewPager.adapter = adapter
        binding.imageViewPager.pageMargin = 20

        val l = OnPageChangeListener()
        l.view = binding.imageViewPager
        binding.imageViewPager.addOnPageChangeListener(l)
    }

    inner class OnPageChangeListener : ViewPager.OnPageChangeListener{
        lateinit var view : ViewPager
        private var curPage = -1
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            if(curPage != -1){
                val v = view.getChildAt(curPage) as ZoomImageView
                v.resetScale()
            }

            curPage = position
            output("onPageSelected: $position")
        }

        override fun onPageScrollStateChanged(state: Int) {

        }


        private fun output(o: String){
            println("mine:: $o")
        }
    }

}