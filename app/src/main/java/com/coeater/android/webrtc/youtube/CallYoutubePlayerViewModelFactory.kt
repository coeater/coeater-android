package com.coeater.android.webrtc.youtube

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.apprtc.WebSocketRTCClient
import com.coeater.android.webrtc.game.CallGameViewModel

class CallYoutubePlayerViewModelFactory (
    private val client: WebSocketRTCClient
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallGameViewModel(client) as T
    }
}