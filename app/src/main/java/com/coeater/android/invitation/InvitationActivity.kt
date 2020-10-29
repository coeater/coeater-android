package com.coeater.android.invitation

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View\
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
import com.coeater.android.model.RoomResponse
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.template.model.*
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

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
            this, viewModelFactory)[InvitationViewModel::class.java]
        viewModel.onCreate(roomId)

        tv_code_title.text = "Your Code"
        iv_state.setImageResource(R.drawable.share_24_px)
        tv_state.text = "Share"
        et_code_number.visibility = View.GONE
        btn_share_or_join.setOnClickListener {
            share()
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

    fun share() {
        val defaultFeed = FeedTemplate(
            content = Content(
                title = "딸기 치즈 케익",
                description = "#케익 #딸기 #삼평동 #카페 #분위기 #소개팅",
                imageUrl = "http://mud-kage.kakao.co.kr/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png",
                link = Link(
                    webUrl = "https://developers.kakao.com",
                    mobileWebUrl = "https://developers.kakao.com"
                )
            ),
            social = Social(
                likeCount = 286,
                commentCount = 45,
                sharedCount = 845
            ),
            buttons = listOf(
                Button(
                    "웹으로 보기",
                    Link(
                        webUrl = "https://developers.kakao.com",
                        mobileWebUrl = "https://developers.kakao.com"
                    )
                ),
                Button(
                    "앱으로 보기",
                    Link(
                        androidExecParams = mapOf("key1" to "value1", "key2" to "value2"),
                        iosExecParams = mapOf("key1" to "value1", "key2" to "value2")
                    )
                )
            )
        )

        // 피드 메시지 보내기
        LinkClient.instance.defaultTemplate(this, defaultFeed) { linkResult, error ->
            if (error != null) {
                Log.e(TAG, "카카오링크 보내기 실패", error)
            }
            else if (linkResult != null) {
                Log.d(TAG, "카카오링크 보내기 성공 ${linkResult.intent}")
                startActivity(linkResult.intent)

                // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                Log.w(TAG, "Warning Msg: ${linkResult.warningMsg}")
                Log.w(TAG, "Argument Msg: ${linkResult.argumentMsg}")
            }
        }
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
