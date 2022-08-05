package com.iceqi.mydemo.ui.gallery

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.iceqi.mydemo.R
import com.iceqi.mydemo.databinding.FragmentGalleryBinding
import com.iceqi.mydemo.ui.common.ImageUploadService
import com.iceqi.mydemo.ui.common.UPLoadTaskConfigStore
import com.iceqi.mydemo.ui.home.ImageList
import kotlinx.coroutines.*
import java.io.File


// check getCount multi times


class GalleryFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var _binding: FragmentGalleryBinding? = null
    private val strUp = "       ↑"
    private val strDown = "       ↓"

    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private lateinit var syncImage: SyncImage
    lateinit var imgs: Array<String>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var albumsAdapter: AlbumsAdapter = AlbumsAdapter()
    val imageList = ImageList()

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
        galleryViewModel = ViewModelProvider(this)[GalleryViewModel::class.java]
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        albumsAdapter.resources = resources
        albumsAdapter.applicatoinContext = requireActivity().applicationContext
        albumsAdapter.albums = binding.albums
        binding.albums.adapter = albumsAdapter

        binding.albums.onItemSelectedListener = OnAlbumSelected()
        setHasOptionsMenu(true)

        val b = requireActivity().applicationContext.checkSelfPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (b == PackageManager.PERMISSION_GRANTED) {
            LoaderManager.getInstance(this).initLoader(2, null, albumsAdapter)
            loadImageListView()
        } else if (b == PackageManager.PERMISSION_DENIED) {
            val l = registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                if (it) {
                    LoaderManager.getInstance(this).initLoader(2, null, albumsAdapter)
                    loadImageListView()
                }
            }
            l.launch("android.permission.WRITE_EXTERNAL_STORAGE")
        }

        binding.share.let {
            it.setImageBitmap((it.drawable as BitmapDrawable).bitmap)
            it.setOnClickListener {
                shareImages()
            }
        }
        binding.delete.let {
            it.setImageBitmap((it.drawable as BitmapDrawable).bitmap)
            it.setOnClickListener {
                deleteImages()
                view?.postDelayed({imageList.refreshData()}, 100)

            }
        }
        binding.multiSelect.let {
            it.setImageBitmap((it.drawable as BitmapDrawable).bitmap)
            it.setOnClickListener {
                Toast.makeText(context, "click selAll", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun deleteImages() {
        val paths = imageList.multiSelImgs.keys.toTypedArray()
        for(p in paths){
            val f = File(p)
            f.delete()
        }
        callBroadCast(paths)
    }

    private fun callBroadCast(paths : Array<String>) {
        if (Build.VERSION.SDK_INT >= 14) {
            MediaScannerConnection.scanFile(context,
                paths,
                null,
                null)
        } else {
            val t = paths[0].split("/")
            val p = paths[0].substring(0, paths[0].length - t[t.size -1].length)
            context?.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://$p")
                )
            )
        }
    }

    /**
     * Share current multi selected images
     */
    private fun shareImages(){
        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.type = "image/*"//Change JPEG to PNG if necessary
        val al = ArrayList<Uri>()
        val sb = StringBuilder("content://media/external/images/media/")
        val l = sb.length
        for(s in imageList.multiSelImgs.values){
            al.add(Uri.parse(sb.append(s).toString()))
            sb.setLength(l)
        }
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, al)

        var cd : ClipData? = null
        for(uri in al){
            if(cd == null)
                cd = ClipData(null, arrayOf("image/*"), ClipData.Item(uri))
            else
                cd.addItem(ClipData.Item(uri))
        }

        shareIntent.clipData = cd
        startActivity(shareIntent)
    }

//    private fun loadGalleryListView(){
//        val gl = GalleryList()
//        childFragmentManager.beginTransaction().let {
//            it.add(R.id.container_fragment, gl)
//            it.commit()
//        }
//    }

    private fun loadImageListView() {
        imageList.enableMultiSelMode = true
        childFragmentManager.beginTransaction().let {
            it.add(R.id.container_fragment, imageList)
            it.commit()
        }
        imageList.onMultiSelect = ::onMultiSelModeChanged
        imageList.onMultiSelectCancelled = ::onMultiSelModeChanged

    }

    private fun onMultiSelModeChanged() {
        binding.options.let {
            if (it.visibility == View.INVISIBLE)
                it.visibility = View.VISIBLE
            else
                it.visibility = View.INVISIBLE
        }
    }

//    override fun onResume() {
//        binding.root.isFocusableInTouchMode = true
//        binding.root.requestFocus()
//        binding.root.setOnKeyListener { _, keyCode, _ ->
//            if (keyCode == KeyEvent.KEYCODE_BACK && multiSelMode) {
//                exitMultiSelMode()
//                return@setOnKeyListener true
//            }else
//                return@setOnKeyListener false
//
//        }
//
//        super.onResume()
//    }


    inner class OnAlbumSelected : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val t = if (position == 0)
                null
            else
                albumsAdapter.getItemText(position)
            if (binding.albums.tag as? String != t) {
                binding.albums.tag = t
                imageList.albumTitle = t
                imageList.refreshData()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        menu.add(0, 0, 0, "sort by time")
        menu.add(0, 1, 0, "sort by name")
        menu.add(0, 2, 0, "upload photo")
        menu.add(0, 3, 0, "setup FTP")
        menu.add(0, 4, 0, "add to upload task")
        menu.add(0, 5, 0, "start upload task")

        super.onCreateOptionsMenu(menu, inflater)
    }


    private fun setColor(resourceId: Int, enable: Boolean): SpannableString {
        return setColor(resources.getString(resourceId), enable)
    }

    private fun setColor(str: String, enable: Boolean): SpannableString {
        val s = SpannableString(str)
        val color = if (enable)
            Color.WHITE
        else
            Color.GRAY
        s.setSpan(
            ForegroundColorSpan(color),
            0,
            s.length,
            0
        )
        return s
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        var bn = resources.getString(R.string.menu_gallery_sort_by_name)
        var bt = resources.getString(R.string.menu_gallery_sort_by_time)
        if (imageList.sortStatus.sortBy == imageList.sortStatus.sortByName) {
            bn += if (imageList.sortStatus.isSortByDesc)
                strUp
            else
                strDown
        } else {
            bt += if (imageList.sortStatus.isSortByDesc)
                strUp
            else
                strDown
        }

        if (imageList.multiSelMode) {
            menu[0].isEnabled = false
            menu[1].isEnabled = false
        }
        menu[2].isEnabled = binding.albums.selectedItemPosition != 0

        menu[0].title = setColor(bt, menu[0].isEnabled)
        menu[1].title = setColor(bn, menu[1].isEnabled)
        menu[2].title = setColor(R.string.menu_gallery_upload, menu[2].isEnabled)
        menu[3].title = setColor(R.string.menu_gallery_setup_ftp, menu[3].isEnabled)
        menu[4].isVisible = binding.albums.selectedItemPosition != 0
        if (menu[4].isVisible) {
            if (isFolderAddedToUploadTask(imageList.curFolderPath)) {
                menu[4].title =
                    setColor(R.string.menu_gallery_do_not_upload_folder, menu[4].isEnabled)
                menu[4].setOnMenuItemClickListener {
                    removeFolderFromUploadTask(imageList.curFolderPath)
                    true
                }
            } else {
                menu[4].title = setColor(R.string.menu_gallery_upload_folder, menu[4].isEnabled)
                menu[4].setOnMenuItemClickListener {
                    addFolderToUploadTask(imageList.curFolderPath)
                    true
                }
            }
        }
        menu[5].title = setColor(R.string.menu_gallery_start_batch_upload, menu[3].isEnabled)

        super.onPrepareOptionsMenu(menu)
    }

    private fun uploadTaskConfigAccess(
        folder: String,
        tasks: ((folder: String, cfgStore: UPLoadTaskConfigStore) -> Unit)?
    ): Boolean {
        val cfg = UPLoadTaskConfigStore()
        cfg.ctx = requireContext()
        val p = cfg.open()
        return if (tasks == null) {
            if (p == null)
                false
            else
                !p.none { it.compareTo(folder) == 0 }
        } else {
            tasks(folder, cfg)
            return true
        }
    }

    private fun isFolderAddedToUploadTask(folder: String): Boolean {
        return uploadTaskConfigAccess(folder, null)
    }

    private fun addFolderToUploadTask(folder: String) {
        uploadTaskConfigAccess(folder) { folder, cfg -> cfg.addPath(folder) }
    }

    private fun removeFolderFromUploadTask(folder: String) {
        uploadTaskConfigAccess(folder) { folder, cfg -> cfg.removePath(folder) }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> {
                imageList.sortStatus.sortBy = imageList.sortStatus.sortByDate
                imageList.refreshData()
            }
            1 -> {
                imageList.sortStatus.sortBy = imageList.sortStatus.sortByName
                imageList.refreshData()
            }
            2 -> {

                if (imageList.multiSelMode)
                    upload(imageList.multiSelImgs.keys.toTypedArray())
                else {
                    val t = UploadTask()
                    LoaderManager.getInstance(this@GalleryFragment).restartLoader(2, null, t)
                }
            }
            3 -> setupFTP()
            5 -> context?.startService(Intent(context, ImageUploadService::class.java))
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupFTP() {
        val ftp = FTPServerSetup()
        ftp.ctx = requireContext()
        ftp.inflater = layoutInflater
        ftp.parent = requireView()

        ftp.initView()
        ftp.start()
    }

    fun genCursorLoader(isForUpload: Boolean, albumTitle: String?): CursorLoader {
        var selection: String? = null
        var selArgs: Array<String>? = null
        if (albumTitle != null) {
            selection = MediaStore.Images.Media.BUCKET_DISPLAY_NAME + "=?"
            selArgs = arrayOf(albumTitle)
        }

        var order: String

        if (isForUpload) {
            order = MediaStore.Images.Media.DATE_TAKEN + " desc"
        } else {
            order = if (imageList.sortStatus.sortBy == imageList.sortStatus.sortByDate)
                MediaStore.Images.Media.DATE_TAKEN
            else
                MediaStore.Images.Media.DISPLAY_NAME
            order += if (imageList.sortStatus.isSortByDesc)
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
            order
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun upload(images: Array<String>) {
        syncImage = SyncImage()
        syncImage.ctx = requireContext()
        syncImage.images = images
        syncImage.scope = scope
        syncImage.inflater = layoutInflater
        syncImage.parent = requireView()

        syncImage.initView()
        syncImage.start()
        syncImage.onFinished = {
            if (it)
                imageList.exitMultiSelMode()
        }
    }

    inner class UploadTask : LoaderManager.LoaderCallbacks<Cursor> {
        @RequiresApi(Build.VERSION_CODES.R)
        override fun onLoadFinished(p0: Loader<Cursor>, data: Cursor?) {
            if (data == null || data.isClosed)
                return

            val fs = arrayOfNulls<String>(data.count)
            data.moveToFirst()
            do {
                fs[data.position] = data.getString(0)
            } while (!data.isLast && data.moveToNext())
            this@GalleryFragment.upload(fs as Array<String>)
            LoaderManager.getInstance(this@GalleryFragment).destroyLoader(2)
        }

        override fun onCreateLoader(p0: Int, p1: Bundle?): Loader<Cursor> {
            return genCursorLoader(true, imageList.albumTitle)
        }

        override fun onLoaderReset(p0: Loader<Cursor>) {
        }
    }
}

