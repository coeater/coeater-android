package com.coeater.android.invitation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.UserManageProvider
import com.coeater.android.api.provideMatchApi
import com.coeater.android.code.InvitationViewModelFactory
import com.coeater.android.model.AcceptedState
import com.coeater.android.model.RoomResponse
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

class InvitationActivity : AppCompatActivity() {

    private val viewModelFactory by lazy {
        InvitationViewModelFactory(
            provideMatchApi(this),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: InvitationViewModel
    private var roomId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)
        setup()
    }


    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[InvitationViewModel::class.java]
        viewModel.onCreate(roomId)

        tv_code_title.text = "Your Code"
        iv_state.setImageResource(R.drawable.share_24_px)
        tv_state.text = "Share"
        et_code_number.visibility = View.GONE
        btn_share_or_join.setOnClickListener {

        }
        viewModel.roomCreateSuccess.observe(this, Observer<RoomResponse> {
            tv_code_number.text = it.room_code
            roomId = it.id

            if(it.accepted != AcceptedState.NOTCHECK) {
                if(it.checked) {
                    checkPermission(it.room_code.toString())
                }
                else {
                    if(roomId != null) {
                        viewModel.onAccept(roomId ?: 0)
                    }
            }
            }
            else {
                viewModel.onStart(roomId ?: 0)
            }

        })
    }

    override fun onStart() {
        super.onStart()
    }

    private fun checkPermission(url: String) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                Toast.makeText(
                    this@InvitationActivity,
                    "Permission Granted",
                    Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@InvitationActivity, CallActivity::class.java)
                intent.putExtra("url", url)
                startActivity(intent)
            }
            override fun onPermissionDenied(deniedPermissions: List<String>) {
                Toast.makeText(
                    this@InvitationActivity,
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
