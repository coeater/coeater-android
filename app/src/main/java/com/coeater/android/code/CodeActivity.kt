package com.coeater.android.code

import android.Manifest
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
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
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.fragment_oneonone_code.*
import java.net.HttpURLConnection

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
            checkPermission(it.room_code)
        })

    }
    override fun onStart() {
        super.onStart()
        viewModel.onCreate()
    }

    private fun checkPermission(url: String) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(
                    this@CodeActivity,
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@CodeActivity, CallActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@CodeActivity,
                    "Permission Denied\n$deniedPermissions",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
            .setPermissions(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            .check()
    }

}
