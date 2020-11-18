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
import com.coeater.android.api.provideGalleryApi
import com.coeater.android.api.provideHistoryApi
import com.coeater.android.api.provideUserApi
import com.coeater.android.friends.AddFriendActivity
import com.coeater.android.gallery.GalleryViewModel
import com.coeater.android.gallery.GalleryViewModelFactory
import com.coeater.android.history.HistoryViewModel
import com.coeater.android.history.HistoryViewModelFactory
import com.coeater.android.join.JoinActivity
import com.coeater.android.kakaolink.KakaoLinkExecuter
import com.coeater.android.main.fragment.GalleryFragment
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
    private val galleryViewModelFactory by lazy {
        GalleryViewModelFactory(
            provideGalleryApi(this)
        )
    }
    private val historyViewModelFactory by lazy {
        HistoryViewModelFactory(
            provideHistoryApi(this)
        )
    }

    lateinit var mainViewModel: MainViewModel
    lateinit var myPageViewModel: MyPageViewModel
    lateinit var galleryViewModel: GalleryViewModel
    lateinit var historyViewModel: HistoryViewModel

    private val oneOnOne = OneOnOneFragment()
    private val myPage = MyPageFragment()
    private val history = HistoryFragment()
    private val gallery = GalleryFragment()

    private companion object Page {
        val ONE_ON_ONE = 0
        val MY_PAGE = 1
        val GALLERY = 2
        val HISTORY = 3
    }

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
        galleryViewModel = ViewModelProviders.of(
            this, galleryViewModelFactory)[GalleryViewModel::class.java]

        vp_main.adapter = MainPagerAdapter(supportFragmentManager)

        val title = tv_title


        iv_one_on_one.setOnClickListener { vp_main.currentItem = ONE_ON_ONE }
        iv_my_page.setOnClickListener { vp_main.currentItem = MY_PAGE }
        iv_gallery.setOnClickListener { vp_main.currentItem = GALLERY }
        iv_history.setOnClickListener { vp_main.currentItem = HISTORY }

        vp_main.addOnPageChangeListener(object: ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {
                when(position) {
                    ONE_ON_ONE -> {
                        title.text = "Co-eating"
                        iv_one_on_one.setImageResource(R.drawable.ic_group_24px_salmon)
                        iv_my_page.setImageResource(R.drawable.ic_my_page_light_salmon)
                        iv_gallery.setImageResource(R.drawable.ic_photo_library_light_salmon)
                        iv_history.setImageResource(R.drawable.ic_history_light_salmon)
                        mainViewModel.fetchFriends()
                    }
                    MY_PAGE -> {
                        title.text = "My page"
                        iv_one_on_one.setImageResource(R.drawable.ic_group_light_salmon)
                        iv_my_page.setImageResource(R.drawable.ic_my_page_salmon)
                        iv_gallery.setImageResource(R.drawable.ic_photo_library_light_salmon)
                        iv_history.setImageResource(R.drawable.ic_history_light_salmon)
                        myPageViewModel.fetchRequest()
                    }
                    GALLERY -> {
                        title.text = "Gallery"
                        iv_one_on_one.setImageResource(R.drawable.ic_group_light_salmon)
                        iv_my_page.setImageResource(R.drawable.ic_my_page_light_salmon)
                        iv_gallery.setImageResource(R.drawable.ic_photo_library_24px_salmon)
                        iv_history.setImageResource(R.drawable.ic_history_light_salmon)
                        galleryViewModel.fetchGallery()
                    }
                    HISTORY -> {
                        title.text = "History"
                        iv_one_on_one.setImageResource(R.drawable.ic_group_light_salmon)
                        iv_my_page.setImageResource(R.drawable.ic_my_page_light_salmon)
                        iv_gallery.setImageResource(R.drawable.ic_photo_library_light_salmon)
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
        galleryViewModel.fetchGallery()
        openJoinIfNeeded()
    }

    override fun onBackPressed() {
        if(vp_main.currentItem != 0) vp_main.currentItem = ONE_ON_ONE
        else super.onBackPressed()
    }

    private inner class MainPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = 4
        override fun getItem(position: Int): Fragment {
            return when(position) {
                ONE_ON_ONE -> { oneOnOne }
                MY_PAGE -> { myPage }
                GALLERY -> { gallery }
                HISTORY -> { history }
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
