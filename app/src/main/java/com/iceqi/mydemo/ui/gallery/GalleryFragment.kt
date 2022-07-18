package com.iceqi.mydemo.ui.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.AdapterView
import android.widget.CheckBox
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.RecyclerView
import com.iceqi.mydemo.R
import com.iceqi.mydemo.databinding.FragmentGalleryBinding
import com.iceqi.mydemo.databinding.GalleryRowBinding
import com.iceqi.mydemo.ui.common.AsyncImageLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import java.io.File
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.apply
import kotlin.arrayOf
import kotlin.arrayOfNulls


// check getCount multi times

const val EXTRA_MSG_IMAGE_PATH = "com.iceqi.mydemo.ui.gallery.GalleryFragment.ImagePath"
const val EXTRA_MSG_CURRENT_IMAGE_INDEX = "com.iceqi.mydemo.ui.gallery.GalleryFragment.ImageIndex"
class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null
    private val aImageLoader = AsyncImageLoader()
    private val strUp = "       ↑"
    private val strDown = "       ↓"
    // Marks if the gallery view in multi select mode
    private var multiSelMode : Boolean = false
    // Holds path all multi selected images
    private val multiSelImgs = HashSet<String>()

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var syncImage : SyncImage
    lateinit var imgs : Array<String>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    var imgAdapter: ImageAdapter = ImageAdapter()
    var albumsAdapter : AlbumsAdapter = AlbumsAdapter()
    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        galleryViewModel = ViewModelProvider(this)[GalleryViewModel ::class.java]
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        binding.imageList.adapter = imgAdapter
        albumsAdapter.resources = resources
        albumsAdapter.applicatoinContext = requireActivity().applicationContext
        albumsAdapter.albums = binding.albums
        binding.albums.adapter = albumsAdapter

        binding.albums.onItemSelectedListener = OnAlbumSelected()

        val b = requireActivity().applicationContext.checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if(b == PackageManager.PERMISSION_GRANTED ){
            LoaderManager.getInstance(this).initLoader(1, null, imgAdapter)
            LoaderManager.getInstance(this).initLoader(2, null, albumsAdapter)
        }else if (b == PackageManager.PERMISSION_DENIED ){
            val l = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                if (it) {
                    LoaderManager.getInstance(this).initLoader(2, null, albumsAdapter)
                    LoaderManager.getInstance(this).initLoader(1, null, imgAdapter)
                }
            }
            l.launch("android.permission.WRITE_EXTERNAL_STORAGE")
        }

        setHasOptionsMenu(true)

        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        binding.root.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK && multiSelMode) {
                exitMultiSelMode()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun exitMultiSelMode(){
        multiSelMode = false
        multiSelImgs.clear()
        imgAdapter.notifyDataSetChanged()
    }

    inner class OnAlbumSelected : AdapterView.OnItemSelectedListener{
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val t = if(position == 0)
                        null
                    else
                        albumsAdapter.getItemText(position)
            if(binding.albums.tag as? String != t){
                exitMultiSelMode()
                binding.albums.tag = t
                imgAdapter.albumTitle = t
                LoaderManager.getInstance(this@GalleryFragment).restartLoader(1, null, imgAdapter)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        menu.add(0,0,0,"")
        menu.add(0,1,0,"")
        menu.add(0,2,0,"")

        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun setColor(str : String, enable : Boolean) : SpannableString{
        val s = SpannableString(str)
        val color = if(enable)
                        Color.WHITE
                    else
                        Color.GRAY
        s.setSpan(ForegroundColorSpan(color),
            0,
            s.length,
            0
        )
        return s
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        var bn = resources.getString(R.string.menu_gallery_sort_by_name)
        var bt = resources.getString(R.string.menu_gallery_sort_by_time)
        val upload = resources.getString(R.string.menu_gallery_upload)

        if(sortStatus.sortBy == sortStatus.sortByName){
            bn += if(sortStatus.isSortByDesc)
                strUp
            else
                strDown
        }
        else {
            bt += if(sortStatus.isSortByDesc)
                strUp
            else
                strDown
        }

        if(multiSelMode) {
            menu[0].isEnabled = false
            menu[1].isEnabled = false
        }
        menu[2].isEnabled = binding.albums.selectedItemPosition != 0

        menu[0].title = setColor(bt, menu[0].isEnabled)
        menu[1].title = setColor(bn, menu[1].isEnabled)
        menu[2].title = setColor(upload, menu[2].isEnabled)

        super.onPrepareOptionsMenu(menu)
    }

    private val sortStatus = SortStatus()
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            0 -> sortStatus.setSort(sortStatus.sortByDate)
            1 -> sortStatus.setSort(sortStatus.sortByName)
            2 -> {

                    if(multiSelMode)
                        upload(multiSelImgs.toTypedArray())
                    else{
                        val t = UploadTask()
                        LoaderManager.getInstance(this@GalleryFragment).restartLoader(2, null, t)
                    }
                    return true
                }
            else -> return super.onOptionsItemSelected(item)
        }

        LoaderManager.getInstance(this@GalleryFragment).restartLoader(1, null, imgAdapter)
        return true
    }

    inner class SortStatus{
        val sortByName = "SortByName"
        val sortByDate = "SortByDate"

        var sortBy : String? = sortByDate
        var isSortByDesc = true

        fun setSort(sortBy : String){
            if(sortBy == this.sortBy)
                isSortByDesc = !isSortByDesc
            else {
                this.sortBy = sortBy
                isSortByDesc = true
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        aImageLoader.stop()
        super.onDestroyView()
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

    inner class ImageViewTag(val image : ImageView, val check : CheckBox, var path : String?, var position : Int)

    inner class LongClickListener : View.OnLongClickListener{
        @SuppressLint("NotifyDataSetChanged")
        override fun onLongClick(v: View): Boolean {
            if( binding.albums.selectedItemPosition == 0
                || v.tag == null)
                return true

            val path: String = (v.tag as ImageViewTag).path ?: return false
            multiSelMode = true
            multiSelImgs.add(path)
            imgAdapter.notifyDataSetChanged()
            return true
        }
    }

    inner class ClickListener : View.OnClickListener{
        override fun onClick(v: View) {
            val tag = v.tag as ImageViewTag
            if(tag.path == null)
                return

            if(multiSelMode){
                if(multiSelImgs.contains(tag.path)){
                    multiSelImgs.remove(tag.path)
                }else{
                    multiSelImgs.add(tag.path!!)
                }
                if(v !is CheckBox)
                    tag.check.isChecked = !tag.check.isChecked
            }
            else{
                displayImage(tag)
            }
        }
    }

     fun displayImage(tag : ImageViewTag){
         val intent = Intent(this.context, ImagePageDisplay::class.java).apply{
             putExtra(EXTRA_MSG_IMAGE_PATH, imgs)
             putExtra(EXTRA_MSG_CURRENT_IMAGE_INDEX, tag.position)
         }

         startActivity(intent)
     }

    val longClickListener = LongClickListener()
    val clickListener = ClickListener()
    open inner class ImageAdapter : RecyclerView.Adapter<ImageViewHolder>(),
        LoaderManager.LoaderCallbacks<Cursor> {
        var albumTitle : String? = null
        private var imageCursor: Cursor? = null

        init{
            super.setHasStableIds(true)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val i = LayoutInflater.from(parent.context)
            val b = GalleryRowBinding.inflate(i, parent, false)
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
                    if(tag.path == null
                        || tag.path?.let { p.compareTo(it) } != 0) {
                        tag.path = p
                        aImageLoader.loadImage(imgArray[i], tag.path!!, true)
                    }
                    tag.position = c.position
                    hasMore = c.moveToNext()
                    selArray[i].isVisible = multiSelMode
                }

                if(multiSelMode)
                    selArray[i].isChecked = multiSelImgs.contains(tag.path)
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

        private fun loadImgPath(data : Cursor){
            val path = arrayOfNulls<String>(data.count)
            data.moveToFirst()
            for(i in path.indices){
                path[i] = data.getString(0)
                data.moveToNext()
            }

            this@GalleryFragment.imgs = path as Array<String>
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
        }
    }

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
            arrayOf(MediaStore.Images.Media.DATA),
            selection,
            selArgs,
            order)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun upload(images : Array<String>){
        syncImage = SyncImage()
        syncImage.ctx = requireContext()
        syncImage.images = images
        syncImage.scope = scope
        syncImage.inflater = layoutInflater
        syncImage.parent = requireView()

        syncImage.initView()
        syncImage.start()

        exitMultiSelMode()
    }
    inner class UploadTask : LoaderManager.LoaderCallbacks<Cursor> {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onLoadFinished(p0: Loader<Cursor>, data: Cursor?) {
            if(data == null || data.isClosed)
                return

            val fs = arrayOfNulls<String>(data.count)
            data.moveToFirst()
            do{
                fs[data.position] = data.getString(0)
            }while(!data.isLast && data.moveToNext() )
            this@GalleryFragment.upload(fs as Array<String>)
            LoaderManager.getInstance(this@GalleryFragment).destroyLoader(2)
        }

        override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
                return genCursorLoader(true, imgAdapter.albumTitle)
        }

        override fun onLoaderReset(p0: Loader<Cursor>) {
        }
    }
}

