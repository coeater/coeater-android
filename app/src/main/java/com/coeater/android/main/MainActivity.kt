package com.coeater.android.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.main.fragment.OneOnOneFragment
import com.coeater.android.model.FriendsInfo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val viewModelFactory by lazy {
        MainViewModelFactory(
            provideUserApi(this)
        )
    }

    private lateinit var viewModel: MainViewModel

    private val oneOnOneFragment = OneOnOneFragment()

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
    }
}
