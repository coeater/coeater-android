package com.coeater.android.webrtc.youtube

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coeater.android.apprtc.WebSocketRTCClient
import com.coeater.android.webrtc.youtube.model.YoutubeSync

interface CallYoutubeSyncer {
    fun pushInfo(videoId: String, current: Float)
    fun requestInfo()
    fun responseInfo(videoId: String?, current: Float?)
}

class CallYoutubePlayerViewModel(private val client: WebSocketRTCClient) : ViewModel(),CallYoutubeSyncer {
    val resultData: MutableLiveData<YoutubeSync> by lazy {
        MutableLiveData<YoutubeSync>()
    }

    fun syncVideo(result: YoutubeSync) {
        resultData.postValue(result)
    }

    override fun pushInfo(videoId: String, current: Float) {
        client?.pushVideoTime(videoId, current)
    }

    override fun requestInfo() {
        client?.requestVideoTime()
    }
    override fun responseInfo(videoId: String?, current: Float?) {
        client?.responseVideoTime(videoId, current)
    }
}