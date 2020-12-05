package com.coeater.android.mypage

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.model.Profile
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.HttpException
import java.io.File
import java.net.URL
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private val GET_IMAGE: Int = 1
    private val viewModelFactory by lazy {
        MyPageViewModelFactory(
            provideUserApi(this)
        )
    }

    private lateinit var viewModel: MyPageViewModel
    private var profileFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[MyPageViewModel::class.java]

        iv_next.setOnClickListener {
            viewModel.editProfile(et_nickname.text.toString(), profileFile)
        }

        viewModel.myInfo.observe(this, Observer { myInfo ->
            et_nickname.setText(myInfo.nickname)
            Glide.with(this)
                .load(Profile.getUrl(myInfo.profile))
                .error(R.drawable.bg_profile)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile)
                .clearOnDetach()
        })

        viewModel.isEditSuccess.observe(this, Observer { complete ->
            if(complete) {
                finish()
            }
            else {
                showError(viewModel.err)
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
        viewModel.fetchRequest()
        iv_profile.setOnClickListener { setProfile() }
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
        }
    }
    private fun showError(err: Exception?) {
        if(err is HttpException && err.code() == 400) {
            AlertDialog.Builder(this)
                .setTitle("Error").setMessage("The same nickname exists.")
                .create()
                .show()
        }
        else {
            AlertDialog.Builder(this)
                .setTitle("Error").setMessage("An error has occurred.")
                .create()
                .show()
        }
    }
}

