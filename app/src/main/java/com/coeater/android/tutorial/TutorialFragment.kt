package com.coeater.android.tutorial

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coeater.android.R
import kotlinx.android.synthetic.main.view_tutorial.*

open class TutorialFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.view_tutorial, container, false)
        return view
    }


    fun configure(image: Int, closeVisible: Boolean, closeListener: () -> Unit) {
        iv_tutorial.setImageResource(image)
        if (closeVisible) {
            ib_tutorial_close.visibility = View.VISIBLE
        } else {
            ib_tutorial_close.visibility = View.GONE
        }
        ib_tutorial_close.setOnClickListener { closeListener() }
    }
}
