package com.coeater.android.mypage

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.invitation.InvitationActivity
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_my_page.*
import java.io.File

class MyPageActivity: AppCompatActivity() {

    companion object {
        const val TAG = "MyPageActivity"
    }

    private val GET_IMAGE: Int = 1
    private val viewModelFactory by lazy {
        MyPageViewModelFactory(
            provideUserApi(this)
        )
    }
    private lateinit var destinationUri : Uri

    private lateinit var viewModel: MyPageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory
        )[MyPageViewModel::class.java]
        destinationUri = Uri.fromFile(File(cacheDir, "profile.jpeg"))

        setRecyclerView(rv_requests)
        setMyInfo()
        iv_back.setOnClickListener { finish() }
        ib_share.setOnClickListener { shareToKakao() }
        iv_edit.setOnClickListener { setChangeNickname() }
        iv_profile.setOnClickListener { setProfile() }
    }

    //media app을 통해 이미지 파일을 가져옴
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

            UCrop.of(data.data!!, destinationUri)
                .withAspectRatio(1F, 1F)
                .withOptions(options)
                .start(this)
        }
        if(requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            viewModel.changeProfile(destinationUri)
        }
    }

    private fun setChangeNickname() {
        val et_nickname = EditText(this)
        et_nickname.hint = "nickname"


        AlertDialog.Builder(this)
            .setTitle("Change Nickname")
            .setMessage("Please enter a new nickname")
            .setView(et_nickname)
            .setPositiveButton("Enter",
                DialogInterface.OnClickListener {dialog, whichButton ->
                    val nickname = et_nickname.text.toString()
                    this.viewModel.changeNickname(nickname)
                })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener {dialog, whichButton -> dialog.dismiss() })
            .show()
    }

    /**
     * 카카오에 내 친구코드를 공유한다.
     */
    private fun shareToKakao() {
        val myCode = viewModel.requests.value?.owner?.code ?: return
        val defaultFeed = FeedTemplate(
            content = Content(
                title = "Coeater Friend Request: $myCode",
                description = "I want to be friend with you. Will you accept me?",
                imageUrl = "https://raw.githubusercontent.com/coeater/presentation/master/coeater.png",
                link = Link(
                    webUrl = "https://github.com/coeater/"
                )
            ),
            buttons = listOf(
                Button(
                    "Open Coeater",
                    Link(
                        androidExecParams = mapOf("user_code" to myCode, "type" to "user_code")
                    )
                )
            )
        )

        // 피드 메시지 보내기
        LinkClient.instance.defaultTemplate(this, defaultFeed) { linkResult, error ->
            if (error != null) {
                Log.e(InvitationActivity.TAG, "카카오링크 보내기 실패", error)
            } else if (linkResult != null) {
                Log.d(InvitationActivity.TAG, "카카오링크 보내기 성공 ${linkResult.intent}")
                startActivity(linkResult.intent)
                // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                Log.w(InvitationActivity.TAG, "Warning Msg: ${linkResult.warningMsg}")
                Log.w(InvitationActivity.TAG, "Argument Msg: ${linkResult.argumentMsg}")
            }
        }
    }

    private fun setMyInfo() {
        //dummy image
        Glide.with(this)
            .load(R.drawable.ic_dummy_profile)
            .apply(RequestOptions.circleCropTransform())
            .into(iv_profile)
            .clearOnDetach()

        viewModel.myInfo.observe(this, Observer { myInfo ->
            tv_nickname.text = myInfo.nickname
            tv_code.text = "My Code : " + myInfo.code
            Toast.makeText(this, "profile uri: " + myInfo.profile, Toast.LENGTH_SHORT).show()
            Glide.with(this)
                .load(R.drawable.ic_dummy_profile)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile)
                .clearOnDetach()
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchRequest()
    }

    private fun setRecyclerView(FriendRequestRecyclerView: RecyclerView) {
        viewModel.requests.observe(this, Observer { friendRequests ->
            FriendRequestRecyclerView.apply {
                adapter = RequestsAdapter(viewModel, context, friendRequests.friends)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
            if(friendRequests.friends.isEmpty()) tv_empty.visibility = View.VISIBLE
            else tv_empty.visibility = View.GONE
        })
    }
}