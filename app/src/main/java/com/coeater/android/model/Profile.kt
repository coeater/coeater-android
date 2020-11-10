package com.coeater.android.model

class Profile {
    companion object {
        const val imageUrl = "http://ec2-52-78-98-130.ap-northeast-2.compute.amazonaws.com:8000"
        fun getUrl(profile: String?) = imageUrl + profile
    }
}