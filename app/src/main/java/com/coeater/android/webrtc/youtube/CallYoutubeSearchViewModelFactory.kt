package com.coeater.android.webrtc.youtube

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.api.YoutubeSearchApi


class CallYoutubeSearchViewModelFactory (
    private val api: YoutubeSearchApi
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallYoutubeSearchViewModel(api) as T
    }
}