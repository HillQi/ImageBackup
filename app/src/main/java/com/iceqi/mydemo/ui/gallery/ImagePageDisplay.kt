package com.iceqi.mydemo.ui.gallery

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.iceqi.mydemo.databinding.ImagePageDisplayBinding
import com.iceqi.mydemo.ui.common.java.ZoomImageView
import com.iceqi.mydemo.ui.home.EXTRA_MSG_IMAGE_PATH
import com.iceqi.mydemo.ui.home.EXTRA_MSG_CURRENT_IMAGE_INDEX

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

        binding.imageViewPager.setCurrentItem(
            intent.getIntExtra(EXTRA_MSG_CURRENT_IMAGE_INDEX, 0),
            false)
    }

    inner class OnPageChangeListener : ViewPager.OnPageChangeListener{
        lateinit var view : ViewPager
        private var curPage = 0
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            view.findViewWithTag<ZoomImageView>(curPage)?.resetScale()
            curPage = position
        }

        override fun onPageScrollStateChanged(state: Int) {

        }
    }

}