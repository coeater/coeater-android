package com.coeater.android.mypage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.coeater.android.R
import kotlinx.android.synthetic.main.activity_edit_profile.*

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setup()
    }

    private fun setup() {
        iv_profile.setImageURI(intent.data)
    }

}