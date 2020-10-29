package com.coeater.android.code

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.UserManageProvider
import com.coeater.android.api.provideMatchApi
import com.coeater.android.api.provideUserApi
import com.coeater.android.main.MainActivity
import com.coeater.android.main.fragment.OneOnOneCodeFragment
import com.coeater.android.model.RoomResponse
import com.coeater.android.webrtc.CallActivity
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

class CodeActivity : AppCompatActivity() {

    private val viewModelFactory by lazy {
        CodeViewModelFactory(
            provideMatchApi(this),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: CodeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[CodeViewModel::class.java]
        tv_code_title.text = "Enter Code"
        tv_code_number.text = "_______"
        iv_state.setImageResource(R.drawable.login_24_px)
        tv_state.text = "Join"
        et_code_number.visibility = View.VISIBLE
        this?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        btn_share_or_join.setOnClickListener {
            val codeNumber = et_code_number.text.toString()
            viewModel.invitation(codeNumber)
        }
        viewModel.roomCreateSuccess.observe(this, Observer<RoomResponse> {
            /// TODO: Dealing
        })

    }
    override fun onStart() {
        super.onStart()
        viewModel.onCreate()
    }


}
