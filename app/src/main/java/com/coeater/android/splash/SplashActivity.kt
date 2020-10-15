package com.coeater.android.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideAuthApi

class SplashActivity : AppCompatActivity() {

    internal val viewModelFactory by lazy {
        SplashViewModelFactory(
            provideAuthApi()
        )
    }

    lateinit var viewModel: SplashViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[SplashViewModel::class.java]
        viewModel.isLoginSuccess.observe(this, Observer<Boolean> { isSuccess ->
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.onCreate()
    }

    init {
    }
}
