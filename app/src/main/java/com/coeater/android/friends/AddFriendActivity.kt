package com.coeater.android.friends

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.R
import com.coeater.android.api.provideUserApi
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.activity_add_friend.*

class AddFriendActivity : AppCompatActivity() {


    companion object {
        const val USER_CODE = "USER_CODE"
    }


    private val viewModelFactory by lazy {
        AddFriendViewModelFactory(
            provideUserApi(this)
        )
    }

    private lateinit var viewModel: AddFriendViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friend)
        setup()
    }

    private fun setup() {
        viewModel = ViewModelProviders.of(
            this, viewModelFactory)[AddFriendViewModel::class.java]
        viewModel.invitee.observe(this, Observer<User> { invitee ->
            addFriendSuccess()
        })
        viewModel.addFriendFail.observe(this, Observer<Unit> { _ ->
            noSuchFriendError()
        })
        et_code_number.setText(intent?.extras?.getString(USER_CODE) ?: "", TextView.BufferType.EDITABLE)

        setAddListener()
    }
    private fun setAddListener() {
        et_code_number.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                addFriend(et_code_number.text.toString())
                return@OnKeyListener true
            }
            false
        })

        btn_add.setOnClickListener { addFriend(et_code_number.text.toString()) }
    }
    private fun noSuchFriendError() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Friend Code Error").setMessage("You entered a wrong friend code.")

        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, id ->
            })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }


    private fun addFriendSuccess() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Friend Request Sent").setMessage("Your Request was successfully sent! If the other person accpet your request, you will be friends.")
        builder.setCancelable(false)
        builder.setPositiveButton("OK",
            DialogInterface.OnClickListener { dialog, id ->
                finish()
            })
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun addFriend(code: String) {
        if (code.length != 6) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Friend Code Error").setMessage("Friend Code has 6 length.")

            builder.setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id ->
                })
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
            return
        }

        viewModel.invite(code.toUpperCase())
    }
}