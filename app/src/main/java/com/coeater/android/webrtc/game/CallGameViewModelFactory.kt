package com.coeater.android.webrtc.game

import com.coeater.android.mypage.MyPageViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.api.UserApi

class CallGameViewModelFactory (
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CallGameViewModel() as T
    }
}