package com.coeater.android.webrtc.emoji

import com.coeater.android.api.AuthApi
import com.coeater.android.api.baseUrl
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun provideEmojiData(): List<String> {

    return  (1..15).map {
        "$it.json"
    }
}
