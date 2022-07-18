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

    override fun getCount(): Int {
        return items.size + 1
    }

    override fun getItem(position: Int): Any {
        if(position == 0)
            return ""
        else
            return items[position-1]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun genItemView(context : Context) : LinearLayout {
        var t : TextView = TextView(context)
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
        var l : LinearLayout = if(convertView == null)
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
        return if (position == 0)
            resources.getString(R.string.pic_album_all)
        else
            items[position - 1]
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
        val h = HashSet<String>()
        do{
            h.add(data.getString(0))
        }while(!data.isLast && data.moveToNext() )
        items = h.toArray(items)

        notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
    }

    private fun dpToPx(dp: Int): Int {
        return (dp.toFloat() * resources.displayMetrics.density).roundToInt()
    }
}