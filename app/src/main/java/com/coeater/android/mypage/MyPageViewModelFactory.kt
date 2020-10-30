package com.coeater.android.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.api.UserApi

class MyPageViewModelFactory (
    private val api: UserApi
    ) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MyPageViewModel(api) as T
    }
}