package com.coeater.android.friends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.UserApi
import com.coeater.android.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddFriendViewModel (
    private val api: UserApi
) : ViewModel() {
    val requestsInfo: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>()
    }
}