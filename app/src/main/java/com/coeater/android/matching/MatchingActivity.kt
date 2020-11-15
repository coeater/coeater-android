package com.coeater.android.matching

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.view.KeyEvent
import android.view.View
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
import android.os.Bundle
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.model.AcceptedState
import com.coeater.android.model.Profile
import kotlinx.android.synthetic.main.activity_matching.*

class MatchingActivity : AppCompatActivity() {
    /*
     Activity 호출 시에...
     1. intent에 putExtra로 "roomId", "mode", "nickname"을 넣어줄 것
     2. "mode"는 Invitation에서는 "INVITER", Join에서는 "INVITEE"
     3. Invitee는 PUT을 해줄 것.
     */
    private val viewModelFactory by lazy {
        MatchingViewModelFactory(
            provideMatchApi(this),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: MatchingViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)
        setup()
    }

    private fun setup() {
        val b: Bundle? = intent.extras;
        var roomId: Int = -1
        var nickname: String = ""
        var mode: String = ""
        var profile: String = ""
        if (b != null) {
            roomId = b?.getInt("roomId") ?: -1
            mode = b?.getString("mode") ?: ""
            nickname = b?.getString("nickname") ?: ""
            profile = b?.getString("profile") ?: ""
        }

        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[MatchingViewModel::class.java]
        Glide.with(this)
            .load(Profile.getUrl(profile))
            .error(R.drawable.ic_dummy_circle_crop)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_profile)
            .clearOnDetach()
        when (mode) {
            "INVITER" -> {
                tv_text1.text = nickname
                tv_text2.text = "accepted"
                tv_text3.text = "your invitation"
                layout_accept.visibility = View.VISIBLE
                linearLayout.visibility = View.VISIBLE

                viewModel.waitToBeMatched(roomId)

                button_accept.setOnClickListener {
                    viewModel.onClickAccept(roomId)
                    tv_text1.text = "Waiting for"
                    tv_text2.text = nickname
                    tv_text3.visibility = View.GONE
                    layout_accept.visibility = View.GONE
                    linearLayout.visibility = View.GONE
                    viewModel.waitToBeMatched(roomId)
                }
            }
            "INVITEE" -> {
                tv_text1.text = "Waiting for"
                tv_text2.text = nickname
                tv_text3.visibility = View.GONE
                layout_accept.visibility = View.GONE
                linearLayout.visibility = View.GONE

                viewModel.waitToBeMatched(roomId)
            }
            else -> {
                tv_text1.text = "Wrong request"
                tv_text2.text = nickname
                tv_text3.visibility = View.GONE
                layout_accept.visibility = View.GONE
                linearLayout.visibility = View.GONE
            }
        }

        button_close.setOnClickListener {
            viewModel.onClickReject(roomId)
        }

        viewModel.matched.observe(this, Observer<RoomResponse> {
            if (it.accepted == AcceptedState.ACCEPTED && it.checked) {
                checkPermission(it.room_code, it)
                finish()
            }
        })

        viewModel.notMatched.observe(this, Observer<RoomResponse> {
            val title: String = "Match rejected"
            if (it.accepted == AcceptedState.NOTCHECK) {
                finishWithDialog(title, "Successfully rejected match.")
            } else {
                finishWithDialog(title, "The other person rejected.")
            }
        })
        viewModel.matchRejected.observe(this, Observer<Unit> {
            finishWithDialog("Match rejected", "You or the other person rejected match.")
        })


        viewModel.matchError.observe(this, Observer<Unit> {
            finishWithDialog("Matching Error", "Room expired.")
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.onCreate()
    }

    private fun finishWithDialog(title: String, msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@MatchingActivity)
        builder.setTitle(title).setMessage(msg)
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, id ->
                finish()
            })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun checkPermission(roomCode: String, roomResponse: RoomResponse) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val intent = Intent(this@MatchingActivity, CallActivity::class.java)
                intent.putExtra(CallActivity.ROOM_CODE, roomCode)
                intent.putExtra(CallActivity.IS_INVITER, false)
                intent.putExtra(CallActivity.ROOM_RESPONSE, roomResponse)
                startActivity(intent)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>) {

                val builder: AlertDialog.Builder = AlertDialog.Builder(this@MatchingActivity)
                builder.setTitle("Permission Denied")
                    .setMessage("You Denied Permission, so you cannot use this service.")

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
