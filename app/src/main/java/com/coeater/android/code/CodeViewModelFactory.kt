package com.coeater.android.code

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.coeater.android.api.MatchApi
import com.coeater.android.api.UserApi
import com.coeater.android.api.UserManageProvider


class CodeViewModelFactory(
    private val api: MatchApi,
    private val userManageProvider: UserManageProvider
) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CodeViewModel(api, userManageProvider) as T
    }
}
