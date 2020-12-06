package com.coeater.android.main.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.invitation.InvitationActivity
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.matching.MatchingViewModel
import com.coeater.android.model.AcceptedState
import com.coeater.android.model.Profile
import com.coeater.android.mypage.*
import com.kakao.sdk.link.LinkClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import kotlinx.android.synthetic.main.activity_my_page.ib_share
import kotlinx.android.synthetic.main.activity_my_page.iv_edit
import kotlinx.android.synthetic.main.activity_my_page.iv_profile
import kotlinx.android.synthetic.main.activity_my_page.rv_requests
import kotlinx.android.synthetic.main.activity_my_page.rv_invitations
import kotlinx.android.synthetic.main.activity_my_page.tv_code
import kotlinx.android.synthetic.main.activity_my_page.tv_empty
import kotlinx.android.synthetic.main.activity_my_page.tv_empty2
import kotlinx.android.synthetic.main.activity_my_page.tv_nickname
import java.io.File

class MyPageFragment : Fragment() {

    private lateinit var destinationUri : Uri

    val viewModel : MyPageViewModel by activityViewModels()
    val matchingViewModel : MatchingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }
    private fun setup() {
        destinationUri = Uri.fromFile(File(requireActivity().cacheDir, "profile.jpeg"))

        setRecyclerView(rv_requests)
        setInvitationRecyclerView(rv_invitations)
        setMyInfo()
        ib_share.setOnClickListener { shareToKakao() }
        iv_edit.setOnClickListener { moveToEdit() }
    }

    private fun moveToEdit() {
        val intent = Intent(requireContext(), EditProfileActivity::class.java)
        startActivity(intent)
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
        LinkClient.instance.defaultTemplate(requireContext(), defaultFeed) { linkResult, error ->
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
            .load(R.drawable.ic_dummy_circle_crop)
            .into(iv_profile)
            .clearOnDetach()

        viewModel.myInfo.observe(requireActivity(), Observer { myInfo ->
            tv_nickname.text = myInfo.nickname
            tv_code.text = "My Code : " + myInfo.code
            Glide.with(this)
                .load(Profile.getUrl(myInfo.profile))
                .error(R.drawable.ic_dummy_circle_crop)
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile)
                .clearOnDetach()
        })
    }

    private fun setRecyclerView(FriendRequestRecyclerView: RecyclerView) {
        viewModel.requests.observe(requireActivity(), Observer { friendRequests ->
            FriendRequestRecyclerView.apply {
                adapter = RequestsAdapter(viewModel, context, friendRequests.friends)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
            if(friendRequests.friends.isEmpty()) tv_empty.visibility = View.VISIBLE
            else tv_empty.visibility = View.GONE
        })
    }

    private fun setInvitationRecyclerView(rvInvitations: RecyclerView) {
        matchingViewModel.invitations.observe(requireActivity(), Observer { invitations  ->
            val liveInvitation = invitations.filter { it.accepted == AcceptedState.NOTCHECK }
            rvInvitations.apply {
                adapter = InvitationsAdapter(matchingViewModel, context, liveInvitation)
                layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            }
            if(liveInvitation.isEmpty()) tv_empty2.visibility = View.VISIBLE
            else tv_empty2.visibility = View.GONE
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchRequest()
        matchingViewModel.fetchInvitations()
    }
}