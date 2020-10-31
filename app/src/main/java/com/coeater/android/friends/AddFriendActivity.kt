package com.coeater.android.friends

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.invitation.InvitationViewModel
import com.coeater.android.main.MainActivity
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.activity_add_friend.*
import kotlinx.android.synthetic.main.fragment_oneonone_code.*
import kotlinx.android.synthetic.main.fragment_oneonone_code.et_code_number

class AddFriendActivity : AppCompatActivity() {

    private val viewModelFactory by lazy {
        AddFriendViewModelFactory(
            provideUserApi(this)
        )
    }

    private lateinit var viewModel: AddFriendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend) //
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[AddFriendViewModel::class.java]
        viewModel.invitee.observe(this, Observer<User> { invitee ->
            finish()
        })
        setAddListener()
    }
    private fun setAddListener() {
        et_code_number.setOnEditorActionListener { v, actionId, event ->
            if(event.keyCode == KeyEvent.KEYCODE_ENTER) {
                addFriend(et_code_number.text.toString())
                true
            }
            else false
        }

        btn_add.setOnClickListener { addFriend(et_code_number.text.toString()) }
    }

    private fun addFriend(code: String) {
        viewModel.invite(code)
    }
}