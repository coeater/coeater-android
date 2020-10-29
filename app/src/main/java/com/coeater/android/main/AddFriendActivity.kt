package com.coeater.android.main

import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.coeater.android.R
import com.coeater.android.main.fragment.OneOnOneCodeFragment
import kotlinx.android.synthetic.main.fragment_oneonone_code.*

class AddFriendActivity : AppCompatActivity() {
    private val codeFragment = OneOnOneCodeFragment(OneOnOneCodeFragment.State.ADD)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) //TODO make activity
        setup()
    }

    private fun setup() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.f_main, codeFragment).commit()
    }
}