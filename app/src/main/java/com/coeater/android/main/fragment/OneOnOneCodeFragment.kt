package com.coeater.android.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import com.coeater.android.R
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

class OneOnOneCodeFragment(val state: State) : Fragment() {

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
        when (state) {
            State.SHARE -> {
                tv_code_title.text = "Your Code"
                tv_code_number.text = "00000"
                iv_state.setImageResource(R.drawable.share_24_px)
                tv_state.text = "Share"
                et_code_number.visibility = View.GONE
            }
            State.JOIN -> {
                tv_code_title.text = "Enter Code"
                tv_code_number.text = "_______"
                iv_state.setImageResource(R.drawable.login_24_px)
                tv_state.text = "Join"
                et_code_number.visibility = View.VISIBLE
                activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            }
        }
    }
}
