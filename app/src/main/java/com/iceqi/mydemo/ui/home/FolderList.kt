package com.iceqi.mydemo.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.iceqi.mydemo.R
import com.iceqi.mydemo.databinding.FolderListRowBinding
import com.iceqi.mydemo.databinding.FolderListViewBinding
import com.iceqi.mydemo.ui.common.UPLoadTaskConfigStore

class FolderList : Fragment() {

    lateinit var openFolder : (folderPath : String) -> Unit

    private var binding: FolderListViewBinding? = null
    private var multiSelMode : Boolean = false
    private val selFolderIndex = HashSet<Int>()
    private val fa = FolderAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FolderListViewBinding.inflate(inflater, container, false)

        updateAdapter(fa)

        fa.longClickListener = OnLongClick()
        fa.clickListener = OnClick()

        binding!!.let {
            it.folderList.adapter = fa
            it.root.isFocusableInTouchMode = true
            it.root.requestFocus()
            it.root.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK && multiSelMode) {
                    exitMultiSelMode()
                    fa.notifyDataSetChanged()
                    return@setOnKeyListener true
                }else
                    return@setOnKeyListener false
            }
            it.remove.setOnClickListener {
                val uCfg = UPLoadTaskConfigStore()
                uCfg.ctx = requireContext()
                uCfg.open()

                val folders = ArrayList<String>(selFolderIndex.size)
                for(i in selFolderIndex)
                    folders.add(fa.folders?.get(i)!!)
                uCfg.removePath(folders.toTypedArray())
                exitMultiSelMode()
                fa.folders = uCfg.getPaths()
                fa.notifyDataSetChanged()
            }
        }
        return binding!!.root
    }

    private fun updateAdapter(adapter : FolderAdapter){
        val ultc = UPLoadTaskConfigStore()
        ultc.ctx = requireContext()
        adapter.folders = ultc.open()
    }

    private fun exitMultiSelMode(){
        multiSelMode = false
        binding?.remove?.visibility = View.INVISIBLE

        selFolderIndex.clear()
    }

    private fun beginMultiSelMode(){
        multiSelMode = true
        binding?.remove?.visibility = View.VISIBLE
    }

    inner class OnClick : View.OnClickListener{
        @SuppressLint("UseCompatLoadingForDrawables")
        override fun onClick(v: View?) {
            if(multiSelMode) {
                updateOnSelection(v!!)
                if (selFolderIndex.size == 0)
                    exitMultiSelMode()
            }else{
                val i = (v?.tag as FolderListRowHolder).getIndex(v)
                openFolder(fa.getFolderPath(i))
            }
        }
    }

    inner class OnLongClick : View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            if(!multiSelMode){
                beginMultiSelMode()
                updateOnSelection(v!!)
                selFolderIndex.add((v.tag as FolderListRowHolder).getIndex(v))
            }
            return true
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateOnSelection(v : View) {
        val h = v?.tag as FolderListRowHolder
        if (selFolderIndex.contains(h.getIndex(v))) {
            updateViewBackground(h.getRootView(v), false )
            selFolderIndex.remove(h.getIndex(v))
        } else {
            updateViewBackground(h.getRootView(v), true )
            selFolderIndex.add(h.getIndex(v))
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun updateViewBackground(v : View, sel : Boolean){
        if(sel)
            v.background =
                context?.resources?.getDrawable(
                    R.drawable.folder_list_selected_background,
                    null
                )
        else
            v.background =
            context?.resources?.getDrawable(
                R.drawable.folder_list_background,
                null
            )
    }

    private inner class FolderListRowHolder(view: FolderListRowBinding) : RecyclerView.ViewHolder(view.root) {
        val parent1 = view.parent1
        val image1 = view.image1
        val f1 = view.folder1
        var pos1 : Int = -1
        val parent2 = view.parent2
        val image2 = view.image2
        val f2 = view.folder2
        var pos2 : Int = -1

        fun getRootView(v : View) : View{
            return when(v){
                parent1, image1, f1 -> parent1
                parent2, image2, f2 -> parent2
                else -> parent1  // should never comes here
            }
        }

        fun getIndex(v : View) : Int{
            return when(v){
                parent1, image1, f1 -> pos1
                parent2, image2, f2 -> pos2
                else -> pos1  // should never comes here
            }
        }
    }

    private inner class FolderAdapter : RecyclerView.Adapter<FolderListRowHolder>() {
        lateinit var longClickListener : FolderList.OnLongClick
        lateinit var clickListener : FolderList.OnClick
        var folders : List<String>? = null
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FolderListRowHolder {
            val i = LayoutInflater.from(parent.context)
            val f = FolderListRowBinding.inflate(i, parent, false)
            val h = FolderListRowHolder(f)
            val vs = arrayOf(h.parent1, h.image1, h.f1, h.parent2, h.image2, h.f2)
            for(v in vs) {
                v.setOnLongClickListener(longClickListener)
                v.setOnClickListener(clickListener)
                v.tag = h
            }
            return h
        }

        fun getFolderPath(position : Int) : String{
            return folders?.get(position)!!
        }
        override fun onBindViewHolder(holder: FolderListRowHolder, position: Int) {
            var pos = 2 * position
            var p : String? = folders!![pos].split("/").last()

            holder.f1.text = p
            holder.pos1 = pos
            updateViewBackground(holder.parent1, selFolderIndex.contains(pos))

            p = if(++pos < folders!!.size)
                    folders!![pos].split("/").last()
                else
                    null

            if(p != null) {
                holder.parent2.visibility = View.VISIBLE
                holder.f2.text = p
                holder.pos2 = pos
                updateViewBackground(holder.parent2, selFolderIndex.contains(pos))
            } else {
                holder.parent2.visibility = View.INVISIBLE
            }


        }

        override fun getItemCount(): Int {
            if(folders == null)
                return 0

            var s = folders!!.size / 2
            if(folders!!.size % 2 > 0)
                s++

            return s
        }
    }
}
