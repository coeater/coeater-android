package com.coeater.android.splash

import android.app.Activity
import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.api.UserManageProvider
import com.coeater.android.api.provideAuthApi
import com.coeater.android.api.provideUserApi
import com.coeater.android.main.MainActivity
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_register.*
import java.io.File
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private val GET_IMAGE: Int = 1
    private val viewModelFactory by lazy {
        SplashViewModelFactory(
            provideAuthApi(),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: SplashViewModel
    private var profileFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[SplashViewModel::class.java]

        iv_next.setOnClickListener {

            viewModel.setMyInfo(et_nickname.text.toString(), profileFile)
        }
        viewModel.isLoginSuccess.observe(this, Observer { complete ->
            if(complete) {
                moveToMain()
            }
            else {
                showError()
            }
        })

        et_nickname.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent. ACTION_DOWN) {
                val imm = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(et_nickname.windowToken, 0)
                true
            }
            else false
        })

        btn_profile.setOnClickListener { setProfile() }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onCreate()
    }

    private fun setProfile() {
        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            type = "image/*"
        }
        startActivityForResult(intent, GET_IMAGE)
    }

    //GET_IMAGE : image파일을 가져올 경우 crop activity로 보내서 1:1로 crop
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == GET_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {

            val options = UCrop.Options().apply {
                setCircleDimmedLayer(true)
                setShowCropGrid(false)
            }
            profileFile = File(cacheDir, UUID.randomUUID().toString() + ".jpg")

            UCrop.of(data.data!!, Uri.fromFile(profileFile))
                .withAspectRatio(1F, 1F)
                .withOptions(options)
                .start(this)
        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            Glide.with(this)
                .load(profileFile)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile)
                .clearOnDetach()
            iv_photo.visibility = View.GONE
        }
    }

    private fun moveToMain() {
        val intent = Intent(this, MainActivity::class.java).apply {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        this.startActivity(intent)
        finish()
    }
    private fun showError() {
        AlertDialog.Builder(this)
            .setTitle("에러").setMessage("에러가 발생했습니다.")
            .create()
            .show()
    }
}

