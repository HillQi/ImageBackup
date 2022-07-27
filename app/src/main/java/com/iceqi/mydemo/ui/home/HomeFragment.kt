package com.iceqi.mydemo.ui.home

import android.os.Bundle
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
    private var curFragment : Fragment? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })
        binding.textHome.setOnClickListener {
            when(curTag){
                null -> curTag = folderListTag
                folderListTag -> curTag = imageListTag
                imageListTag -> curTag = folderListTag
            }

            var f: Fragment? = childFragmentManager.findFragmentByTag(curTag)
            if (f == null)
                f = genChild(curTag!!)
            childFragmentManager.beginTransaction().let{
                for(c in childFragmentManager.fragments)
                    it.hide(c)
                if(f.isAdded)
                    it.show(f)
                else
                    it.add(R.id.container_fragment, f)
                it.commit()
            }
        }

        binding.textHome.callOnClick()
        return root
    }

    private fun genChild(tag : String) : Fragment{
        if(tag == folderListTag)
            return FolderList()
        else
            return ImageList()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}