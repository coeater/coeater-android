package com.coeater.android.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.api.AuthApi
import com.coeater.android.api.UserManageProvider

class SplashViewModelFactory(
    private val api: AuthApi,
    private val userManageProvider: UserManageProvider
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SplashViewModel(api, userManageProvider) as T
    }
}
