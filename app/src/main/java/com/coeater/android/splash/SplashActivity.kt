package com.coeater.android.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.UserManageProvider
import com.coeater.android.api.provideAuthApi
import com.coeater.android.main.MainActivity

class SplashActivity : AppCompatActivity() {

    private val viewModelFactory by lazy {
        SplashViewModelFactory(
            provideAuthApi(),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: SplashViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[SplashViewModel::class.java]
        viewModel.isLoginSuccess.observe(this, Observer<Boolean> { isSuccess ->
            if (isSuccess) {
                moveToMain()
            } else {
                showError()
            }
        })
    }
    override fun onStart() {
        super.onStart()
        viewModel.onCreate()
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
    }

    private fun showError() {
        AlertDialog.Builder(this)
            .setTitle("에러").setMessage("에러가 발생했습니다.")
            .create()
            .show()
    }
}
