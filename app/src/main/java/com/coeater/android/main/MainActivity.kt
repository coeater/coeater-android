package com.coeater.android.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coeater.android.R
import com.coeater.android.main.fragment.OneOnOneFragment

class MainActivity : AppCompatActivity() {

    private val oneOnOneFragment = OneOnOneFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setup()
    }

    fun setup() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.add(R.id.f_main, oneOnOneFragment)
        fragmentTransaction.commit()
    }
}
