package com.coeater.android.matching

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.api.UserManageProvider
import com.coeater.android.api.provideMatchApi
import com.coeater.android.model.AcceptedState
import com.coeater.android.model.Profile
import com.coeater.android.model.RoomResponse
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_matching.*

class MatchingActivity : AppCompatActivity() {
    /*
     Activity 호출 시에...
     1. intent에 putExtra로 "roomId", "mode", "nickname"을 넣어줄 것
     2. "mode"는 Invitation에서는 "INVITER", Join에서는 "INVITEE"
     3. Invitee는 PUT을 해줄 것.
     */
    companion object {
        const val MATCH_INPUT = "MATCH_INPUT"
    }


    private val viewModelFactory by lazy {
        MatchingViewModelFactory(
            provideMatchApi(this),
            UserManageProvider(this)
        )
    }

    private lateinit var viewModel: MatchingViewModel
    private lateinit var mode: MatchingMode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)
        setup()
    }

    private fun setup() {
        val input: MatchingInput = intent.extras?.getParcelable<MatchingInput>(MATCH_INPUT) ?: return


        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[MatchingViewModel::class.java]
        Glide.with(this)
            .load(Profile.getUrl(input.profile))
            .error(R.drawable.ic_dummy_circle_crop)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_profile)
            .clearOnDetach()
        this.mode = input.mode
        when (mode) {
            MatchingMode.INVITER -> {
                tv_text1.text = input.nickname
                tv_text2.text = "accepted"
                tv_text3.text = "your invitation"
                layout_accept.visibility = View.VISIBLE
                linearLayout.visibility = View.VISIBLE

                viewModel.waitToBeMatched(input.roomId)

                button_accept.setOnClickListener {
                    viewModel.onClickAccept(input.roomId)
                    tv_text1.text = "Waiting for"
                    tv_text2.text = input.nickname
                    tv_text3.visibility = View.GONE
                    layout_accept.visibility = View.GONE
                    linearLayout.visibility = View.GONE
                    viewModel.waitToBeMatched(input.roomId)
                }
            }
            MatchingMode.INVITEE -> {
                tv_text1.text = "Waiting for"
                tv_text2.text = input.nickname
                tv_text3.visibility = View.GONE
                layout_accept.visibility = View.GONE
                linearLayout.visibility = View.GONE

                viewModel.waitToBeMatched(input.roomId)
            }
            MatchingMode.FRIEND_INVITER -> {
                tv_text1.text = "Waiting for"
                tv_text2.text = input.nickname
                tv_text3.visibility = View.GONE
                layout_accept.visibility = View.GONE
                linearLayout.visibility = View.GONE

                viewModel.waitToBeGetAccept(input.roomId)
            }
            MatchingMode.FRIEND_INVITEE -> {
                tv_text1.text = "Waiting for"
                tv_text2.text = input.nickname
                tv_text3.visibility = View.GONE
                layout_accept.visibility = View.GONE
                linearLayout.visibility = View.GONE
                viewModel.onClickAccept(input.roomId)

                viewModel.waitToBeMatched(input.roomId)
            }
        }

        button_close.setOnClickListener {
            viewModel.onClickReject(input.roomId)
        }

        viewModel.matched.observe(this, Observer<RoomResponse> {
            if (it.accepted == AcceptedState.ACCEPTED && it.checked) {
                checkPermission(it.room_code, it, this.mode==MatchingMode.INVITER || this.mode==MatchingMode.FRIEND_INVITER)
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

    private fun checkPermission(roomCode: String, roomResponse: RoomResponse, isInviter: Boolean) {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                val intent = Intent(this@MatchingActivity, CallActivity::class.java)
                intent.putExtra(CallActivity.ROOM_CODE, roomCode)
                intent.putExtra(CallActivity.IS_INVITER, isInviter)
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
