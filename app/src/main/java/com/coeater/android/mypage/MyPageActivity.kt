package com.coeater.android.mypage

import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.main.MainViewModel
import com.coeater.android.main.MainViewModelFactory
import com.coeater.android.model.FriendsInfo
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.android.synthetic.main.view_friends_recycler_item.view.*

class MyPageActivity: AppCompatActivity() {

    private val viewModelFactory by lazy {
        MyPageViewModelFactory(
            provideUserApi(this)
        )
    }

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

        setRecyclerView(rv_requests)
        setMyInfo()
        iv_back.setOnClickListener { finish() }
        ib_share.setOnClickListener { shareToKakao() }
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

        viewModel.requests.observe(this, Observer<FriendsInfo> { requests ->
            tv_nickname.text = requests.owner.nickname
            tv_code.text = "My Code : " + requests.owner.code
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
        viewModel.requests.observe(this,Observer { friendRequests ->
            FriendRequestRecyclerView.apply {
                adapter = RequestsAdapter(viewModel, context, friendRequests.friends)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
            if(friendRequests.friends.isEmpty()) tv_empty.visibility = View.VISIBLE
            else tv_empty.visibility = View.GONE
        })
    }
}