package com.iceqi.mydemo.ui.gallery

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.iceqi.mydemo.R
import kotlin.math.roundToInt
import kotlin.properties.Delegates
import android.content.res.Resources

class  AlbumsAdapter : BaseAdapter(), LoaderManager.LoaderCallbacks<Cursor> {
    lateinit var applicatoinContext : Context
    lateinit var resources : Resources
    lateinit var albums : View
    var items : Array<String> = arrayOf()
    private val defaultItems = arrayOf("Camera", "Screenshots", "bluetooth")

    override fun getCount(): Int {
        return items.size + defaultItems.size
    }

    override fun getItem(position: Int): Any {
        return getItemText(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun genItemView(context : Context) : LinearLayout {
        val t : TextView = TextView(context)
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            dpToPx(30)
        )
        params.setMargins(20, 0, 0, 0)
        t.layoutParams = params
        t.gravity = Gravity.CENTER

        val l = LinearLayout(context)
        l.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        l.addView(t)

        return l
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val l : LinearLayout = if(convertView == null)
            genItemView(parent!!.context)
        else
            convertView as LinearLayout

        val t : TextView = l.getChildAt(0) as TextView
        if(t.tag == null || t.tag as Int != position) {
            t.tag = position
            t.text = getItemText(position)
        }

        if(albums.tag == t.text)
            l.background = ResourcesCompat.getDrawable(resources, R.color.cardview_shadow_start_color, null)
        return l
    }

    fun getItemText (position : Int): String{
        return if(position < defaultItems.size)
            defaultItems[position]
        else
            items[position - defaultItems.size]
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(
            applicatoinContext,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.BUCKET_DISPLAY_NAME),
            null,
            null,
            null)
    }

    @SuppressLint("Range")
    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if(data == null)
            return
        data.moveToFirst()
        if(data.isFirst) {
            val h = HashSet<String>()
            do {
                val s = data.getString(0)
                if(!defaultItems.contains(s))
                    h.add(s)
            } while (!data.isLast && data.moveToNext())
            items = h.toTypedArray()
            items.sort()
            notifyDataSetChanged()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }

    private fun dpToPx(dp: Int): Int {
        return (dp.toFloat() * resources.displayMetrics.density).roundToInt()
    }
}