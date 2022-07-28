package com.iceqi.mydemo.ui.home

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.iceqi.mydemo.R
import com.iceqi.mydemo.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private val folderListTag = "folderListTag"
    private val imageListTag = "imageListTag"
    private var curTag : String? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val fl = FolderList()

        fl.openFolder = { folder ->
            val f = loadFragment(imageListTag, null)
            var il : ImageList? = null
            if(f == null){
                il = ImageList()
                il.curFolderPath = folder
                il.onGoBack = {
                    loadFragment(folderListTag, null)
                }
            }else {
                il = f as ImageList
                il.curFolderPath = folder
                il.refreshData()
            }
            loadFragment(imageListTag, il)
        }
        loadFragment(folderListTag, fl)
        return root
    }

    private fun loadFragment(tag : String, fragment : Fragment? = null) : Fragment?{
        var f: Fragment? = fragment
        if(f == null)
            f = childFragmentManager.findFragmentByTag(tag)
        if(f == null)
            return null

        childFragmentManager.beginTransaction().let{
            for(c in childFragmentManager.fragments)
                if(c != f && !c.isHidden)
                    it.hide(c)
            if(f!!.isAdded)
                it.show(f)
            else
                it.add(R.id.container_fragment, f, tag)
            it.commit()
        }

        return f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}