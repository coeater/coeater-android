package com.coeater.android.invitation

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
import com.coeater.android.code.InvitationViewModelFactory
import com.coeater.android.main.MainActivity
import com.coeater.android.main.fragment.OneOnOneCodeFragment
import com.coeater.android.model.RoomResponse
import com.coeater.android.webrtc.CallActivity
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

class InvitationActivity : AppCompatActivity() {

    private val viewModelFactory by lazy {
        InvitationViewModelFactory(
            provideMatchApi(this),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: InvitationViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)
        setup()
    }

    private fun setup() {

        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[InvitationViewModel::class.java]
        viewModel.onCreate()

        tv_code_title.text = "Your Code"
        iv_state.setImageResource(R.drawable.share_24_px)
        tv_state.text = "Share"
        et_code_number.visibility = View.GONE
        btn_share_or_join.setOnClickListener {

        }
        viewModel.roomCreateSuccess.observe(this, Observer<RoomResponse> {
            tv_code_number.text = it.room_code
        })

    }
    override fun onStart() {
        super.onStart()
    }


}
