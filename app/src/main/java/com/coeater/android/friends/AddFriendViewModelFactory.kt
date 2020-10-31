package com.coeater.android.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.coeater.android.api.UserApi

class AddFriendViewModelFactory(
    private val api: UserApi
) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AddFriendViewModel(api) as T
    }
}