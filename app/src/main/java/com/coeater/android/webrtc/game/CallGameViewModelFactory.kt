package com.coeater.android.webrtc.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.apprtc.WebSocketRTCClient

class CallGameViewModelFactory (
    private val client: WebSocketRTCClient
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallGameViewModel(client) as T
    }
}