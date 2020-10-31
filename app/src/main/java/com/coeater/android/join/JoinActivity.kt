package com.coeater.android.join

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.UserManageProvider
import com.coeater.android.api.provideMatchApi
import com.coeater.android.model.RoomResponse
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

class JoinActivity : AppCompatActivity() {

    companion object {
        const val ROOM_CODE = "ROOM_CODE"
    }

    private val viewModelFactory by lazy {
        JoinViewModelFactory(
            provideMatchApi(this),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: JoinViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[JoinViewModel::class.java]
        tv_code_title.text = "Enter Code"
        tv_code_number.text = "_______"
        iv_state.setImageResource(R.drawable.login_24_px)
        tv_state.text = "Join"
        et_code_number.visibility = View.VISIBLE
        et_code_number.setText(intent?.extras?.getString(ROOM_CODE) ?: "", TextView.BufferType.EDITABLE)
        this?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        btn_share_or_join.setOnClickListener {
            join()
        }
        viewModel.roomCreateSuccess.observe(this, Observer<RoomResponse> {
            checkPermission(it.room_code, it)
        })
        viewModel.roomCreateFail.observe(this, Observer<Unit> {
            noSuchRoomError()
        })
        et_code_number.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                join()
                return@OnKeyListener true
            }
            false
        })
    }

    private fun join() {
        val codeNumber = et_code_number.text.toString()
        if (codeNumber.length != 5) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this@JoinActivity)
            builder.setTitle("Room Code Error").setMessage("Room Code has 5 length.")

            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                })
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
            return
        }

        viewModel.invitation(codeNumber)
    }

    private fun noSuchRoomError() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@JoinActivity)
        builder.setTitle("Room Code Error").setMessage("You entered a wrong room code.")

        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, id ->
            })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onStart() {
        super.onStart()
        viewModel.onCreate()
    }

    private fun checkPermission(roomCode: String, roomResponse: RoomResponse) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val intent = Intent(this@JoinActivity, CallActivity::class.java)
                intent.putExtra(CallActivity.ROOM_CODE, roomCode)
                intent.putExtra(CallActivity.IS_INVITER, false)
                intent.putExtra(CallActivity.ROOM_RESPONSE, roomResponse)
                startActivity(intent)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {

                val builder: AlertDialog.Builder = AlertDialog.Builder(this@JoinActivity)
                builder.setTitle("Permission Denied").setMessage("You Denied Permission, so you cannot use this service.")

                builder.setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialog, id ->
                    })

                val alertDialog: AlertDialog = builder.create()
                alertDialog.show()
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
