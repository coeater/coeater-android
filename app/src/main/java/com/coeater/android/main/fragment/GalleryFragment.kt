package com.coeater.android.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import com.coeater.android.R
import com.coeater.android.gallery.GalleryAdapter
import com.coeater.android.gallery.GalleryViewModel
import kotlinx.android.synthetic.main.fragment_gallery.*

class GalleryFragment : Fragment() {

    val viewModel : GalleryViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        //TODO
        viewModel.gallery.observe(requireActivity(), Observer {gallery ->
            gv_gallery.adapter = GalleryAdapter(requireContext(), gallery)
        })
    }
}