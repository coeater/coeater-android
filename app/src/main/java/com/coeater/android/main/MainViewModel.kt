package com.coeater.android.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coeater.android.api.UserApi
import com.coeater.android.model.FriendsInfo
import com.coeater.android.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

class MainViewModel(
    private val api: UserApi
) : ViewModel() {

    val friendsInfo: MutableLiveData<FriendsInfo> by lazy {
        MutableLiveData<FriendsInfo>()
    }

    fun fetchFriends() {
        viewModelScope.launch(Dispatchers.IO) {
            when (val friendsResult = getFriends()) {
                is Result.Success<FriendsInfo> -> {
                    friendsInfo.postValue(friendsResult.data)
                }
                is Error -> {
                }
            }
        }
    }

    private suspend fun getFriends(): Result<FriendsInfo> {
        return try {
            val response = api.getFriends()
            Result.Success(response)
        } catch (e: HttpException) {
            Result.Error(e)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
