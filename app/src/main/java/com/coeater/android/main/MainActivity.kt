package com.coeater.android.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.main.fragment.OneOnOneCodeFragment
import com.coeater.android.main.fragment.OneOnOneConnectingFragment
import com.coeater.android.main.fragment.OneOnOneFragment
import com.coeater.android.main.fragment.OneOnOneMatchingFragment
import com.coeater.android.model.FriendsInfo
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
//    private val oneOnOneCodeFragment = OneOnOneCodeFragment(OneOnOneCodeFragment.State.SHARE)
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
        viewModel.friendsInfo.observe(this, Observer<FriendsInfo> { friends ->
            showFriends(friends)
        })

        fragmentTransaction.add(R.id.f_main, oneOnOneFragment)
        fragmentTransaction.commit()

        iv_menu.setOnClickListener { showMe() }
    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchFriends()
    }

    private fun showFriends(friendsInfo: FriendsInfo) {
        Toast.makeText(this, friendsInfo.toString(), Toast.LENGTH_LONG)
            .show()
    }

    private fun showMe() {
        Toast.makeText(this, viewModel.friendsInfo.value?.owner.toString(), Toast.LENGTH_SHORT).show()
        val fragmentManager = supportFragmentManager
    }


    // Fragment로부터 다른 Fragment로 아래와 같이 전환할 수 있습니다.
    // (activity as MainActivity).replaceFragment("OneOnOneCode", OneOnOneCodeFragment.State.SHARE)
    fun replaceFragment(fragment: String, state: OneOnOneCodeFragment.State?){
        Log.i(TAG, "replace fragment to $fragment")

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        when(fragment){
            ONEONONE_FRAG -> {
                fragmentTransaction.replace(R.id.f_main, oneOnOneFragment)
            }
            CONNECT_FRAG -> {
                fragmentTransaction.replace(R.id.f_main, oneOnOneConnectingFragment)
            }
            CODE_FRAG -> {
                if(state != null) {
                    oneOnOneCodeFragment = OneOnOneCodeFragment(state)
                    fragmentTransaction.replace(R.id.f_main, oneOnOneCodeFragment).addToBackStack(null).commit()
                } else {
                    Log.i(TAG, "Code Fragemt의 state가 필요합니다.")
                }
            }
            MATCH_FRAG -> {
                fragmentTransaction.replace(R.id.f_main, oneOnOneMatchingFragment)
            }
            else -> Log.i(TAG, "sth else came in")
        }

    }
}
