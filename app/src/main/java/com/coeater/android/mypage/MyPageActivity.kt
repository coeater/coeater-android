package com.coeater.android.mypage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.main.MainViewModel
import com.coeater.android.main.MainViewModelFactory
import com.coeater.android.model.FriendsInfo
import kotlinx.android.synthetic.main.activity_my_page.*

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
        })
    }
}