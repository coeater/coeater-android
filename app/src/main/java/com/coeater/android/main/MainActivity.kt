package com.coeater.android.main

import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.widget.TextView
import androidx.fragment.app.*
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.coeater.android.R
import com.coeater.android.api.provideHistoryApi
import com.coeater.android.api.provideUserApi
import com.coeater.android.friends.AddFriendActivity
import com.coeater.android.history.HistoryViewModel
import com.coeater.android.history.HistoryViewModelFactory
import com.coeater.android.join.JoinActivity
import com.coeater.android.kakaolink.KakaoLinkExecuter
import com.coeater.android.main.fragment.HistoryFragment
import com.coeater.android.main.fragment.MyPageFragment
import com.coeater.android.main.fragment.OneOnOneFragment
import com.coeater.android.mypage.MyPageViewModel
import com.coeater.android.mypage.MyPageViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FragmentActivity() {

    private val mainViewModelFactory by lazy {
        MainViewModelFactory(
            provideUserApi(this)
        )
    }
    private val myPageViewModelFactory by lazy {
        MyPageViewModelFactory(
            provideUserApi(this)
        )
    }
    private val historyViewModelFactory by lazy {
        HistoryViewModelFactory(
            provideHistoryApi(this)
        )
    }

    private lateinit var mainViewModel: MainViewModel
    private lateinit var myPageViewModel: MyPageViewModel
    private lateinit var historyViewModel: HistoryViewModel

    private val oneOnOne = OneOnOneFragment()
    private val myPage = MyPageFragment()
    private val history = HistoryFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
    }

    private fun setup() {
        mainViewModel = ViewModelProviders.of(
            this, mainViewModelFactory)[MainViewModel::class.java]
        myPageViewModel = ViewModelProviders.of(
            this, myPageViewModelFactory)[MyPageViewModel::class.java]
        historyViewModel = ViewModelProviders.of(
            this, historyViewModelFactory)[HistoryViewModel::class.java]

        vp_main.adapter = MainPagerAdapter(supportFragmentManager)

        val title = tv_title


        iv_one_on_one.setOnClickListener { vp_main.currentItem = 0 }
        iv_my_page.setOnClickListener { vp_main.currentItem = 1 }
        iv_history.setOnClickListener { vp_main.currentItem = 2 }

        vp_main.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when(position) {
                    0 -> {
                        title.text = "Co-eating"
                        iv_one_on_one.setImageResource(R.drawable.ic_group_24px_salmon)
                        iv_my_page.setImageResource(R.drawable.ic_my_page_light_salmon)
                        iv_history.setImageResource(R.drawable.ic_history_light_salmon)
                        mainViewModel.fetchFriends()
                    }
                    1 -> {
                        title.text = "My page"
                        iv_one_on_one.setImageResource(R.drawable.ic_group_light_salmon)
                        iv_my_page.setImageResource(R.drawable.ic_my_page_salmon)
                        iv_history.setImageResource(R.drawable.ic_history_light_salmon)
                        myPageViewModel.fetchRequest()
                    }
                    2 -> {
                        title.text = "History"
                        iv_one_on_one.setImageResource(R.drawable.ic_group_light_salmon)
                        iv_my_page.setImageResource(R.drawable.ic_my_page_light_salmon)
                        iv_history.setImageResource(R.drawable.ic_history_24px_salmon)
                        historyViewModel.fetchHistory()
                    }
                }
            }
            override fun onPageScrollStateChanged(state: Int) {}
        })
    }

    override fun onStart() {
        super.onStart()
        mainViewModel.fetchFriends()
        myPageViewModel.fetchRequest()
        historyViewModel.fetchHistory()
        openJoinIfNeeded()
    }

    override fun onBackPressed() {
        if(vp_main.currentItem != 0) vp_main.currentItem = 0
        else super.onBackPressed()
    }

    private inner class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = 3
        override fun getItem(position: Int): Fragment {
            return when(position) {
                0 -> { oneOnOne }
                1 -> { myPage }
                2 -> { history }
                else -> { Fragment() }
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
