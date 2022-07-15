package com.iceqi.mydemo.ui.gallery

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.viewpager.widget.PagerAdapter
import com.iceqi.mydemo.ui.common.AsyncImageLoader
import com.iceqi.mydemo.ui.common.java.ZoomImageView

class ImagePageAdapter : PagerAdapter() {

    // Images to display
    var imgs : Array<String>? = null
    var ctx : Context? = null

    private val imageLoader = AsyncImageLoader()


    override fun getCount(): Int {
        return imgs!!.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val v = ZoomImageView(ctx)
        imgs?.get(position)?.let { imageLoader.loadImage(v, it, false) }
        container.addView(v)

        return v
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val v = `object` as ImageView
        container.removeView(v)

//        super.destroyItem(container, position, `object`)
    }
}