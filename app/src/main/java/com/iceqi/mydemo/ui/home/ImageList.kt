package com.iceqi.mydemo.ui.home

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import com.iceqi.mydemo.databinding.GalleryRowBinding
import com.iceqi.mydemo.databinding.ImageListViewBinding
import com.iceqi.mydemo.ui.common.AsyncImageLoader
import com.iceqi.mydemo.ui.gallery.ImagePageDisplay
import java.io.File

const val EXTRA_MSG_IMAGE_PATH = "com.iceqi.mydemo.ui.gallery.ImagePath"
const val EXTRA_MSG_CURRENT_IMAGE_INDEX = "com.iceqi.mydemo.ui.gallery.ImageIndex"
class ImageList : Fragment() {
    var onGoBack : (() -> Unit)? = null
    var curFolderPath : String = ""
    set(path) {
        field = path
        albumTitle = path.split("/").last()
    }
    var onMultiSelect : (() -> Unit)? = null
    var onMultiSelectCancelled : (() -> Unit)? = null



    var enableMultiSelMode = false
    val sortStatus = SortStatus()
    var albumTitle : String? = null
    var multiSelMode : Boolean = false
    val multiSelImgs = HashMap<String, Int>()

    private lateinit var binding: ImageListViewBinding
    private val imgAdapter: ImageAdapter = ImageAdapter()
    private val longClickListener = LongClickListener()
    private val clickListener = ClickListener()
    lateinit var imgs : Array<String>
    lateinit var imgIds : Array<Int>
    private val aImageLoader = AsyncImageLoader()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ImageListViewBinding.inflate(inflater, container, false)
        binding.imageList.adapter = imgAdapter

//        binding.root.isFocusableInTouchMode = true
//        binding.root.requestFocus()
//        binding.root.setOnKeyListener { _, keyCode, _ ->
//            if (keyCode == KeyEvent.KEYCODE_BACK && multiSelMode) {
//                exitMultiSelMode()
//            }else if(onGoBack != null){
//                onGoBack?.let { it() }
//            }
//            return@setOnKeyListener true
//        }

        LoaderManager.getInstance(this).initLoader(1, null, imgAdapter)
//        context?.registerReceiver(Receiver(), IntentFilter.create(null, "image/*"))
        return binding!!.root
    }

    override fun onDestroyView() {
        imgAdapter.clear()

        super.onDestroyView()
    }

    override fun onResume() {
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        binding.root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && isMultiSelMode()) {
                exitMultiSelMode()
                return@setOnKeyListener true
            }else if(onGoBack != null){
                onGoBack?.let { it() }
                return@setOnKeyListener true
            }else
                return@setOnKeyListener false

        }
        super.onResume()
    }

    fun refreshData(){
        exitMultiSelMode()
        LoaderManager.getInstance(this).restartLoader(1, null, imgAdapter)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        aImageLoader.cr = context?.contentResolver!!
    }

    fun exitMultiSelMode(){
        multiSelMode = false
        multiSelImgs.clear()
        binding.root.post{imgAdapter.notifyDataSetChanged()}
        onMultiSelectCancelled?.let { it() }
    }

    private fun enterMultiSelMode(){
        multiSelMode = true
        onMultiSelect?.let { it() }
    }

    private fun isMultiSelMode() : Boolean {
        return multiSelMode
    }

    private fun loadImgPath(data : Cursor){
        val path = arrayOfNulls<String>(data.count)
        val ids = arrayOfNulls<Int>(data.count)
        data.moveToFirst()
        for(i in path.indices){
            path[i] = data.getString(0)
            ids[i] = data.getInt(1)
            data.moveToNext()
        }

        imgs = path as Array<String>
        imgIds = ids as Array<Int>
    }

    fun displayImage(tag : ImageViewTag){
        val intent = Intent(this.context, ImagePageDisplay::class.java).apply{
            putExtra(EXTRA_MSG_IMAGE_PATH, imgs)
            putExtra(EXTRA_MSG_CURRENT_IMAGE_INDEX, tag.position)
        }

        startActivity(intent)
    }

    // TODO abstract this
    fun genCursorLoader(isForUpload : Boolean, albumTitle : String?) : CursorLoader {
        var selection : String? = null
        var selArgs : Array<String>? = null
        if(albumTitle != null){
            selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?"
            selArgs = arrayOf(albumTitle)
        }

        var order: String

        if(isForUpload){
            order = MediaStore.Images.Media.DATE_TAKEN + " desc"
        }else{
            order = if(sortStatus.sortBy == sortStatus.sortByDate)
                MediaStore.Images.Media.DATE_TAKEN
            else
                MediaStore.Images.Media.DISPLAY_NAME
            order += if(sortStatus.isSortByDesc)
                " desc"
            else
                " asc"
        }

        return CursorLoader(
            requireActivity().applicationContext,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID),
            selection,
            selArgs,
            order)
    }

    inner class SortStatus{
        val sortByName = "SortByName"
        val sortByDate = "SortByDate"

        var sortBy : String? = sortByDate
        set(value){
            if(value == this.sortBy)
                isSortByDesc = !isSortByDesc
            else {
                isSortByDesc = true
            }
            field = value
        }
        var isSortByDesc = true
    }

    inner class ImageViewTag(val image : ImageView, val check : CheckBox, var path : String?, var position : Int)

    inner class ClickListener : View.OnClickListener{
        override fun onClick(v: View) {
            val tag = v.tag as ImageViewTag
            if(tag.path == null)
                return

            if(isMultiSelMode()){
                if(multiSelImgs.contains(tag.path)){
                    multiSelImgs.remove(tag.path)
                }else{
                    multiSelImgs[tag.path!!] = imgIds[tag.position]
                }
                if(v !is CheckBox)
                    tag.check.isChecked = !tag.check.isChecked
            }
            else{
                displayImage(tag)
            }
        }
    }

    inner class LongClickListener : View.OnLongClickListener{
        @SuppressLint("NotifyDataSetChanged")
        override fun onLongClick(v: View): Boolean {
            if( isMultiSelMode() || albumTitle == null)
                return true
            (v.tag as ImageViewTag).let {
                if(it.path == null)
                    return false
                multiSelImgs[it.path!!] = imgIds[it.position]
            }
            enterMultiSelMode()
            imgAdapter.notifyDataSetChanged()
            return true
        }
    }

    inner class ImageViewHolder(view: GalleryRowBinding) : RecyclerView.ViewHolder(view.root) {
        val c1 = view.galleryRowCheck1
        val c2 = view.galleryRowCheck2
        val c3 = view.galleryRowCheck3
        val c4 = view.galleryRowCheck4

        val v1: ImageView = view.galleryRowPic1
        val v2: ImageView = view.galleryRowPic2
        val v3: ImageView = view.galleryRowPic3
        val v4: ImageView = view.galleryRowPic4
    }

    inner class ImageAdapter : RecyclerView.Adapter<ImageViewHolder>(),
        LoaderManager.LoaderCallbacks<Cursor> {
        private var imageCursor: Cursor? = null

        init{
            super.setHasStableIds(true)
        }

        fun clear(){
            imageCursor?.close()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val i = LayoutInflater.from(parent.context)
            val b = GalleryRowBinding.inflate(i, parent, false)
            // TODO lp??
            val lp = b.galleryRow.layoutParams
            lp.width = parent.width
            b.galleryRow.layoutParams = lp

            val ivh = ImageViewHolder(b)
            val listener = longClickListener
            ivh.v1.setOnLongClickListener(listener)
            ivh.v1.setOnClickListener(clickListener)
            ivh.c1.setOnClickListener(clickListener)
            ivh.v2.setOnLongClickListener(listener)
            ivh.v2.setOnClickListener(clickListener)
            ivh.c2.setOnClickListener(clickListener)
            ivh.v3.setOnLongClickListener(listener)
            ivh.v3.setOnClickListener(clickListener)
            ivh.c3.setOnClickListener(clickListener)
            ivh.v4.setOnLongClickListener(listener)
            ivh.v4.setOnClickListener(clickListener)
            ivh.c4.setOnClickListener(clickListener)

            ivh.v1.tag = ImageViewTag(ivh.v1, ivh.c1, null, -1)
            ivh.c1.tag = ivh.v1.tag
            ivh.v2.tag = ImageViewTag(ivh.v2, ivh.c2, null, -1)
            ivh.c2.tag = ivh.v2.tag
            ivh.v3.tag = ImageViewTag(ivh.v3, ivh.c3, null, -1)
            ivh.c3.tag = ivh.v3.tag
            ivh.v4.tag = ImageViewTag(ivh.v4, ivh.c4, null, -1)
            ivh.c4.tag = ivh.v4.tag

            return ivh
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val c = imageCursor!!
            val p = position * 4
            val imgArray = arrayOf(holder.v1, holder.v2, holder.v3, holder.v4)
            val selArray = arrayOf(holder.c1, holder.c2, holder.c3, holder.c4)
            c.moveToPosition(p)
            var hasMore = true
            for(i in imgArray.indices){
                val tag = imgArray[i].tag as ImageViewTag
                if(!hasMore) {
                    tag.path = null
                    imgArray[i].setImageBitmap(null)
                    selArray[i].isVisible = false
                    tag.position = -1
                }else {
                    val p = c.getString(0)
                    if(c.position == 0 && albumTitle != null)
                        curFolderPath = File(p).parentFile.path

                    if(tag.path == null
                        || tag.path?.let { p.compareTo(it) } != 0) {
                        tag.path = p
                        imgArray[i].setImageBitmap(null)
                        aImageLoader.loadImage(imgArray[i], tag.path!!, true, c.getLong(1))
                    }
                    tag.position = c.position
                    hasMore = c.moveToNext()
                    selArray[i].isVisible = isMultiSelMode()
                }

                if(selArray[i].isChecked || isMultiSelMode()) {
                    selArray[i].isChecked = multiSelImgs.contains(tag.path)
                    selArray[i].jumpDrawablesToCurrentState()
                }
            }
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        private var ic = -1
        override fun getItemCount(): Int {
            if(ic != -1)
                return ic

            val c = imageCursor?.count ?: 0
            if(c == 0)
                return 0
            ic = c / 4
            if(4 * ic < c)
                ++ic

            return ic
        }

        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            return genCursorLoader(false, albumTitle)
        }

        @SuppressLint("NotifyDataSetChanged")
        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            if(imageCursor != data) {
                loadImgPath(data!!)

                ic = -1
                imageCursor?.close()
                imageCursor = data
                imageCursor?.moveToFirst()
                notifyDataSetChanged()
                binding.imageList.scrollToPosition(0)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
        }
    }

    inner class Receiver : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            val a = p0
            val b = a
        }

    }
}