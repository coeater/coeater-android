package com.coeater.android.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.coeater.android.R
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

class OneOnOneCodeFragment(val state : State) : Fragment() {

    enum class State { SHARE, JOIN }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oneonone_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    fun setup() {
        if(state == State.SHARE) {
            tv_code_title.text = "Your Code"
            tv_code_number.text = "00000" //TODO give code
            iv_state.setImageResource(R.drawable.share_24_px)
            tv_state.text = "Share"
            et_code_number.visibility = View.GONE
        }
        else {
            tv_code_title.text = "Enter Code"
            tv_code_number.text = "_______" //TODO give code
            iv_state.setImageResource(R.drawable.login_24_px)
            tv_state.text = "Join"
            et_code_number.visibility = View.VISIBLE
        }
    }
}
