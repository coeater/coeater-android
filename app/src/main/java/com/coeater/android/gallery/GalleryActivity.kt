package com.coeater.android.gallery

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.coeater.android.R
import com.coeater.android.model.DateTime
import com.coeater.android.model.Profile
import com.coeater.android.model.User
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.view_histoy_recycler_item.view.*

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        setup()
    }

    private fun setup() {
        val data = intent.getSerializableExtra("user") as User
        val image = intent.getStringExtra("gallery")
        include_history.bg_line.background = null
        include_history.tv_nickname.text = data.nickname
        include_history.tv_history.text = DateTime.getAgo(data.created)
        Glide.with(this)
            .load(Profile.getUrl(data.profile))
            .error(R.drawable.ic_dummy_circle_crop)
            .apply(RequestOptions.circleCropTransform())
            .into(include_history.iv_profile)
            .clearOnDetach()
        Glide.with(this)
            .load(Profile.getUrl(image))
            .into(iv_gallery)
            .clearOnDetach()
        iv_share.setOnClickListener { setShare(image) }
    }

    private fun setShare(image : String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "image/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, Profile.getUrl(image))
        startActivity(shareIntent)
    }
}