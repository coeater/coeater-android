package com.coeater.android.mypage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.main.MainViewModel
import com.coeater.android.main.MainViewModelFactory

class MyPageActivity: AppCompatActivity() {

    private val viewModelFactory by lazy {
        MyPageViewModelFactory(
            provideUserApi(this)
        )
    }

    private lateinit var viewModel: MyPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[MyPageViewModel::class.java]
    }
}