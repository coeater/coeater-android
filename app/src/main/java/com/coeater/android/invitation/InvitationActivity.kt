package com.coeater.android.invitation

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.UserManageProvider
import com.coeater.android.api.provideMatchApi
import com.coeater.android.code.InvitationViewModelFactory
import com.coeater.android.matching.MatchingActivity
import com.coeater.android.model.AcceptedState
import com.coeater.android.model.RoomResponse
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import kotlinx.android.synthetic.main.activity_join.*

class InvitationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "InvitationActivity"
    }

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
            this, viewModelFactory
        )[InvitationViewModel::class.java]
        viewModel.onCreate(roomId)

        tv_code_title.text = "Your Code"
        iv_state.setImageResource(R.drawable.share_24_px)
        tv_state.text = "Share"
        et_code_number.visibility = View.GONE
        btn_share_or_join.setOnClickListener {
            val roomCode = viewModel.roomCreateSuccess.value?.room_code
            if (roomCode != null) {
                shareToKakao(roomCode)
            }
        }
        viewModel.roomCreateSuccess.observe(this, Observer<RoomResponse> {
            tv_code_number.text = it.room_code
            roomId = it.id

            if (it.accepted == AcceptedState.NOTCHECK) {
                viewModel.checkAcceptance(roomId ?: 0)
            }
        })

        viewModel.inviteeAccepted.observe(this, Observer<RoomResponse> {
            if(it.accepted == AcceptedState.ACCEPTED) {
                val intent = Intent(this@InvitationActivity, MatchingActivity::class.java)
                intent.putExtra("mode", "INVITER")
                intent.putExtra("roomId", roomId ?:0)
                intent.putExtra("nickname", it.target?.nickname)
                intent.putExtra("profile", it.target?.profile)
                startActivity(intent)
                finish()
            }
            else {
                finishWithDialog("Match rejected", "The other person rejected.")
            }
        })
        viewModel.expiredMatch.observe(this, Observer<Unit> {
            finish()
        })
    }

    private fun finishWithDialog(title: String, msg: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@InvitationActivity)
        builder.setTitle(title).setMessage(msg)
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, id ->
                finish()
            })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    /**
     * 카카오에 룸버튼 처리를 한다.
     */
    private fun shareToKakao(roomCode: String) {
        val defaultFeed = FeedTemplate(
            content = Content(
                title = "Coeater Invitation: $roomCode",
                description = "I invite you to my dinner! Join Me?",
                imageUrl = "https://raw.githubusercontent.com/coeater/presentation/master/coeater.png",
                link = Link(
                    webUrl = "https://github.com/coeater/"
                )
            ),
            buttons = listOf(
                Button(
                    "Open Coeater",
                    Link(
                        androidExecParams = mapOf(
                            "room_code" to roomCode,
                            "type" to "room_invitation"
                        )
                    )
                )
            )
        )

        // 피드 메시지 보내기
        LinkClient.instance.defaultTemplate(this, defaultFeed) { linkResult, error ->
            if (error != null) {
                Log.e(TAG, "카카오링크 보내기 실패", error)
            } else if (linkResult != null) {
                Log.d(TAG, "카카오링크 보내기 성공 ${linkResult.intent}")
                startActivity(linkResult.intent)
                // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                Log.w(TAG, "Warning Msg: ${linkResult.warningMsg}")
                Log.w(TAG, "Argument Msg: ${linkResult.argumentMsg}")
            }
        }
    }
}
