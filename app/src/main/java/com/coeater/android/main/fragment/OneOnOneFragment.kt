package com.coeater.android.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.invitation.InvitationActivity
import com.coeater.android.join.JoinActivity
import com.coeater.android.main.MainViewModel
import com.coeater.android.main.MainViewModelFactory
import com.coeater.android.main.recyclerview.AddFriendAdapter
import com.coeater.android.webrtc.CallActivity
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.fragment_oneonone.*
import kotlinx.android.synthetic.main.view_main_friends.view.*

class OneOnOneFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels {
        MainViewModelFactory(
            provideUserApi(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_oneonone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setup()
    }

    private fun setup() {
        main_button_invite_friend.configure(
            R.drawable.send_24_px,
            "Invite Friend",
            "Invite your friend with code"
        )
        main_button_search_friend.configure(
            R.drawable.search_24_px,
            "Look for Co-eater",
            "Find another Co-eater to eat with you"
        )
        main_button_invite_friend.setOnClickListener {
            val intent = Intent(getActivity(), InvitationActivity::class.java)
            startActivity(intent)
        }
        main_button_search_friend.setOnClickListener {
            val intent = Intent(getActivity(), JoinActivity::class.java)
            startActivity(intent)
        }
        setRecyclerView(include_friends.rv_friends)
    }

    private fun setRecyclerView(FriendsRecyclerView: RecyclerView) {
        viewModel.friendsInfo.observe(viewLifecycleOwner, Observer { friendsInfo ->
            FriendsRecyclerView.apply {
                adapter = AddFriendAdapter(requireContext(), friendsInfo.friends)
                layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriends()
    }
}
