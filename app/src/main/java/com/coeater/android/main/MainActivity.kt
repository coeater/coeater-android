package com.coeater.android.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.friends.AddFriendActivity
import com.coeater.android.join.JoinActivity
import com.coeater.android.kakaolink.KakaoLinkExecuter
import com.coeater.android.main.fragment.OneOnOneCodeFragment
import com.coeater.android.main.fragment.OneOnOneConnectingFragment
import com.coeater.android.main.fragment.OneOnOneFragment
import com.coeater.android.main.fragment.OneOnOneMatchingFragment
import com.coeater.android.model.FriendsInfo
import com.coeater.android.mypage.MyPageActivity
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {

    private val viewModelFactory by lazy {
        MainViewModelFactory(
            provideUserApi(this)
        )
    }

    private lateinit var viewModel: MainViewModel

    private val oneOnOneFragment = OneOnOneFragment()
    private val oneOnOneConnectingFragment = OneOnOneConnectingFragment()
    private lateinit var oneOnOneCodeFragment: OneOnOneCodeFragment
    private lateinit var oneOnOneMatchingFragment: OneOnOneMatchingFragment
    private val ONEONONE_FRAG = "OneOnOne"
    private val CONNECT_FRAG = "OneOnOneConnect"
    private val CODE_FRAG = "OneOnOneCode"
    private val MATCH_FRAG = "OneOnOneMatching"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
    }

    private fun setup() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[MainViewModel::class.java]

        fragmentTransaction.add(R.id.f_main, oneOnOneFragment)
        fragmentTransaction.commit()

        iv_menu.setOnClickListener {
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriends()
        openJoinIfNeeded()
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

    // Fragment로부터 다른 Fragment로 아래와 같이 전환할 수 있습니다.
    // (activity as MainActivity).replaceFragment("OneOnOneCode", OneOnOneCodeFragment.State.SHARE)
    fun replaceFragment(fragment: String, state: OneOnOneCodeFragment.State?) {
        Log.i(TAG, "replace fragment to $fragment")

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when (fragment) {
            ONEONONE_FRAG -> {
                fragmentTransaction.replace(R.id.f_main, oneOnOneFragment)
            }
            CONNECT_FRAG -> {
                fragmentTransaction.replace(R.id.f_main, oneOnOneConnectingFragment)
            }
            CODE_FRAG -> {
                if (state != null) {
                    oneOnOneCodeFragment = OneOnOneCodeFragment(state)
                    fragmentTransaction.replace(R.id.f_main, oneOnOneCodeFragment)
                } else {
                    Log.i(TAG, "Code Fragemt의 state가 필요합니다.")
                }
            }
            MATCH_FRAG -> {
                fragmentTransaction.replace(R.id.f_main, oneOnOneMatchingFragment)
            }
            else -> Log.i(TAG, "sth else came in")
        }

        fragmentTransaction.addToBackStack(null).commit()
    }
}
