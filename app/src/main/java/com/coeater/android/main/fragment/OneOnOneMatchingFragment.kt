package com.coeater.android.main.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import kotlinx.android.synthetic.main.fragment_oneonone_matching.*

class OneOnOneMatchingFragment(val state: State) : Fragment() {

    enum class State { INVITING, INVITED, ACCEPTED }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oneonone_matching, container, false)
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
        setText()
    }

    fun setText() {
        when (state) {
            State.INVITING -> {
                tv_text1.setText("waiting For")
                tv_text2.setText("Mr.Gourmet") // TODO change target's name
                tv_text2.setTypeface(Typeface.create(tv_text2.typeface, Typeface.BOLD))
                tv_text3.visibility = View.GONE
                layout_accept.visibility = View.GONE
                linearLayout.visibility = View.GONE
            }
            State.INVITED -> {
                tv_text1.setText("Mr.Groumet") // TODO change target's name
                tv_text2.setTypeface(Typeface.create(tv_text2.typeface, Typeface.BOLD))
                tv_text2.setText("invites you")
                tv_text3.visibility = View.GONE
            }
            State.ACCEPTED -> {
                tv_text1.setText("Mr.Groumet") // TODO change target's name
                tv_text1.setTypeface(Typeface.create(tv_text2.typeface, Typeface.BOLD))
                tv_text2.setText("accepted")
                tv_text3.setText("your invitation")
            }
        }
    }
}
