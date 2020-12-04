package com.coeater.android.webrtc.youtube

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.apprtc.WebSocketRTCClient

class CallYoutubePlayerViewModelFactory (
    private val client: WebSocketRTCClient
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallYoutubePlayerViewModel(client) as T
    }
}