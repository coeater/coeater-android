package com.coeater.android.main

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.friends.AddFriendActivity
import com.coeater.android.join.JoinActivity
import com.coeater.android.kakaolink.KakaoLinkExecuter
import com.coeater.android.main.fragment.MyPageFragment
import com.coeater.android.main.fragment.OneOnOneFragment
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"
private const val NUM_PAGES = 3

class MainActivity : FragmentActivity() {

    private val viewModelFactory by lazy {
        MainViewModelFactory(
            provideUserApi(this)
        )
    }

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[MainViewModel::class.java]
        vp_main.adapter = MainPagerAdapter(supportFragmentManager)

        iv_one_on_one.setOnClickListener { vp_main.currentItem = 0 }
        iv_my_page.setOnClickListener { vp_main.currentItem = 1 }
        iv_history.setOnClickListener { vp_main.currentItem = 2 }


    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriends()
        openJoinIfNeeded()
    }

    override fun onBackPressed() {
        vp_main.currentItem = 1;
    }

    private inner class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = NUM_PAGES
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> {
                    iv_one_on_one.setImageResource(R.drawable.ic_group_24px_salmon)
                    iv_my_page.setImageResource(R.drawable.ic_my_page_light_salmon)
                    iv_history.setImageResource(R.drawable.ic_history_light_salmon)
                    OneOnOneFragment()
                }
                1 -> {
                    iv_one_on_one.setImageResource(R.drawable.ic_group_light_salmon)
                    iv_my_page.setImageResource(R.drawable.ic_my_page_salmon)
                    iv_history.setImageResource(R.drawable.ic_history_light_salmon)
                    MyPageFragment()
                }
                2 -> {
                    iv_one_on_one.setImageResource(R.drawable.ic_group_light_salmon)
                    iv_my_page.setImageResource(R.drawable.ic_my_page_light_salmon)
                    iv_history.setImageResource(R.drawable.ic_history_24px_salmon)
                    Fragment()
                }
                else -> {
                    Fragment()
                }
            }
        }
    }

    /**
     * 카카오링크로 열었을 경우, Join Activity 로 랜딩하도록 처리한다.
     */
    private fun openJoinIfNeeded() {
        val kakaoLinkExecuter = KakaoLinkExecuter(this)
        kakaoLinkExecuter.roomCode?.let {
            kakaoLinkExecuter.deleteRoomCode()
            val intent = Intent(this, JoinActivity::class.java)
            intent.putExtra(JoinActivity.ROOM_CODE, it)
            startActivity(intent)
        }
        kakaoLinkExecuter.userCode?.let {
            kakaoLinkExecuter.deleteUserCode()
            val intent = Intent(this, AddFriendActivity::class.java)
            intent.putExtra(AddFriendActivity.USER_CODE, it)
            startActivity(intent)
        }
    }
}
