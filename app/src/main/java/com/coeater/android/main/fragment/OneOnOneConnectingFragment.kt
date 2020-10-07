package com.coeater.android.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import kotlinx.android.synthetic.main.fragment_oneonone_connecting.*

class OneOnOneConnectingFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oneonone_connecting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    fun setup() {
        Glide.with(this)
            .load(R.drawable.unnamed2)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_profile)
            .clearOnDetach()
    }
}
